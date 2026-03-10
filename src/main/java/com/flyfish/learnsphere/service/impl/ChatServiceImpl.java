package com.flyfish.learnsphere.service.impl;


import com.flyfish.learnsphere.exception.BusinessException;
import com.flyfish.learnsphere.memory.RedisChatMemoryStore;
import com.flyfish.learnsphere.model.dto.ChatRequest;
import com.flyfish.learnsphere.model.entity.Course;
import com.flyfish.learnsphere.model.enums.ErrorCode;
import com.flyfish.learnsphere.model.vo.MessageVO;
import com.flyfish.learnsphere.model.vo.RetrievalChunkVO;
import com.flyfish.learnsphere.service.ChatService;
import com.flyfish.learnsphere.service.CourseService;
import com.flyfish.learnsphere.service.RagService;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: FlyFish
 * @CreateTime: 2025/11/26
 */
@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatLanguageModel chatLanguageModel;

    private final RedisChatMemoryStore chatMemoryStore;
    private final StreamingChatLanguageModel streamingChatLanguageModel;
    private final RagService ragService;
    private final CourseService courseService;

    // Max characters of contentMd to inject directly when no RAG index exists
    private static final int MAX_DIRECT_CONTENT_LENGTH = 3000;

    public ChatServiceImpl(ChatLanguageModel chatLanguageModel,
                           RedisChatMemoryStore chatMemoryStore,
                           StreamingChatLanguageModel streamingChatLanguageModel,
                           RagService ragService,
                           CourseService courseService) {
        this.chatLanguageModel = chatLanguageModel;
        this.chatMemoryStore = chatMemoryStore;
        this.streamingChatLanguageModel = streamingChatLanguageModel;
        this.ragService = ragService;
        this.courseService = courseService;
    }


    /**
     * 处理用户提问
     * @param chatRequest
     * @param userId
     * @return
     */
    @Override
    public SseEmitter ask(ChatRequest chatRequest, Long userId) {
        String sessionId = chatRequest.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String question = chatRequest.getQuestion();
        if (question == null || question.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        chatMemoryStore.setUserId(userId);
        List<ChatMessage> history = chatMemoryStore.getMessages(sessionId);
        String systemPrompt = "你叫FLY FISH AI, 专门用来解决用户的各种问题, 当前网站的创作者是FLY FISH";
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(SystemMessage.systemMessage(systemPrompt));

        // Store retrieval results for source citation
        List<RetrievalChunkVO> retrievalResults = new ArrayList<>();

        Long courseId = chatRequest.getCourseId();
        if (courseId != null) {
            retrievalResults = ragService.retrieveRelevantChunksWithScore(courseId, question);
            if (!retrievalResults.isEmpty()) {
                // RAG retrieval succeeded — build context with source citation numbers
                StringBuilder contextBuilder = new StringBuilder();
                for (int i = 0; i < retrievalResults.size(); i++) {
                    RetrievalChunkVO chunk = retrievalResults.get(i);
                    contextBuilder.append("[来源").append(i + 1).append("] ");
                    if (chunk.getHeading() != null && !chunk.getHeading().isEmpty()) {
                        contextBuilder.append("(章节: ").append(chunk.getHeading()).append(") ");
                    }
                    contextBuilder.append("\n").append(chunk.getText()).append("\n---\n");
                }
                messages.add(SystemMessage.systemMessage(
                        "以下是课程知识库中与问题相关的内容片段（按相关性排序），请优先基于它们回答。"
                        + "回答中如果引用了某个片段的内容，请在对应句子末尾用 [来源N] 标注引用来源。"
                        + "如果涉及数学公式，请使用 LaTeX 格式（行内用 $...$，独立公式用 $$...$$）。\n\n"
                        + contextBuilder
                ));
            } else {
                // RAG index not found — fallback: inject course full text + async index build
                Course course = courseService.getCourseById(courseId);
                if (course != null && course.getContentMd() != null && !course.getContentMd().trim().isEmpty()) {
                    String contentMd = course.getContentMd();
                    String context = contentMd.length() > MAX_DIRECT_CONTENT_LENGTH
                            ? contentMd.substring(0, MAX_DIRECT_CONTENT_LENGTH) + "\n...(内容已截断)"
                            : contentMd;
                    messages.add(SystemMessage.systemMessage(
                            "以下是当前课程《" + course.getTitle() + "》的完整内容，请基于它回答用户问题。"
                            + "如果涉及数学公式，请使用 LaTeX 格式（行内用 $...$，独立公式用 $$...$$）。\n" + context
                    ));
                    final String finalContentMd = contentMd;
                    new Thread(() -> {
                        try {
                            ragService.indexCourseContent(courseId, finalContentMd);
                            log.info("Auto-indexed course {} in background", courseId);
                        } catch (Exception e) {
                            log.warn("Background indexing failed for courseId={}: {}", courseId, e.getMessage());
                        }
                    }).start();
                }
            }
        }

        messages.addAll(history);
        ChatMessage userMessage = UserMessage.userMessage(question);
        messages.add(userMessage);


        SseEmitter emitter = new SseEmitter(0L);

        // Send retrieval source info as a separate SSE event before streaming answer
        final List<RetrievalChunkVO> sources = retrievalResults;
        if (!sources.isEmpty()) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String sourcesJson = objectMapper.writeValueAsString(sources);
                emitter.send(SseEmitter.event().name("sources").data(sourcesJson));
            } catch (Exception e) {
                log.warn("Failed to send source info via SSE: {}", e.getMessage());
            }
        }

        StringBuilder fullAnswer = new StringBuilder();
        streamingChatLanguageModel.generate(messages, new StreamingResponseHandler<>() {
            @Override
            public void onNext(String token) {
                try{
                    fullAnswer.append(token);
                    emitter.send(SseEmitter.event().data(token));
                }catch (Exception e){
                    log.error("SEE send error", e);
                    emitter.completeWithError(new BusinessException(ErrorCode.SYSTEM_ERROR));
                }
            }

            @Override
            public void onError(Throwable error) {
                log.error("Stream LLM error", error);
                try{
                    emitter.send(SseEmitter.event().name("error").data("System Error"));
                }catch (Exception ignored){}
                emitter.completeWithError(new BusinessException(ErrorCode.SYSTEM_ERROR));
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                try{
                    ChatMessage aiMessage = AiMessage.aiMessage(fullAnswer.toString());
                    messages.add(aiMessage);
                    chatMemoryStore.updateMessages(sessionId, messages);
                    chatMemoryStore.addSession(userId, sessionId);
                    emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                    emitter.complete();
                }catch (Exception e){
                    log.error("SEE send error", e);
                    emitter.completeWithError(new BusinessException(ErrorCode.SYSTEM_ERROR));
                }
            }
        });
        return emitter;
    }


    /**
     * 获取用户会话历史
     * @param userId
     * @return
     */
    @Override
    public List<String> getSessions(Long userId) {
        return chatMemoryStore.listHistory(userId);
    }


    /**
     * 获取当前session下的历史聊天记录
     * @param sessionId
     * @return
     */
    @Override
    public List<MessageVO> getSessionHistory(Long userId, String sessionId) {
        chatMemoryStore.setUserId(userId);
        List<ChatMessage> messages = chatMemoryStore.getMessages(sessionId);
        if(messages==null||messages.isEmpty()){
            log.warn("messages is empty");
        }
        List<MessageVO> res = new ArrayList<>();
        for (ChatMessage message : messages) {
            MessageVO messageVO = new MessageVO();
            if(message instanceof AiMessage){
                messageVO.setType("AI");
                messageVO.setText(message.text());
            }else if(message instanceof UserMessage){
                messageVO.setType("USER");
                messageVO.setText(message.text());
            }else if(message instanceof SystemMessage){
                continue;
            }
            res.add(messageVO);
        }
        return res;
    }


    /**
     * Batch get session titles for given session IDs.
     * Title = first USER message text (truncated to 20 chars).
     */
    @Override
    public Map<String, String> getSessionTitles(Long userId, List<String> sessionIds) {
        chatMemoryStore.setUserId(userId);
        Map<String, String> titles = new LinkedHashMap<>();
        for (String sessionId : sessionIds) {
            List<ChatMessage> messages = chatMemoryStore.getMessages(sessionId);
            String title = null;
            for (ChatMessage message : messages) {
                if (message instanceof UserMessage) {
                    String text = message.text().trim().replaceAll("\\s+", " ");
                    title = text.length() > 20 ? text.substring(0, 20) + "..." : text;
                    break;
                }
            }
            titles.put(sessionId, title != null ? title : "新的对话");
        }
        return titles;
    }
}
