package com.flyfish.learnsphere.service.impl;


import com.flyfish.learnsphere.exception.BusinessException;
import com.flyfish.learnsphere.memory.RedisChatMemoryStore;
import com.flyfish.learnsphere.model.dto.ChatRequest;
import com.flyfish.learnsphere.model.entity.Course;
import com.flyfish.learnsphere.model.enums.ErrorCode;
import com.flyfish.learnsphere.model.vo.MessageVO;
import com.flyfish.learnsphere.service.ChatService;
import com.flyfish.learnsphere.service.CourseService;
import com.flyfish.learnsphere.service.RagService;

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
import java.util.List;

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

        Long courseId = chatRequest.getCourseId();
        if (courseId != null) {
            List<String> chunks = ragService.retrieveRelevantChunks(courseId, question);
            if (!chunks.isEmpty()) {
                // RAG 检索成功，使用向量检索结果
                String context = String.join("\n---\n", chunks);
                messages.add(SystemMessage.systemMessage(
                        "以下是课程知识库中与问题相关的内容片段，请优先基于它们回答：\n" + context
                ));
            } else {
                // RAG 索引不存在，fallback：直接将课程全文注入上下文，并异步触发索引构建
                Course course = courseService.getCourseById(courseId);
                if (course != null && course.getContentMd() != null && !course.getContentMd().trim().isEmpty()) {
                    String contentMd = course.getContentMd();
                    // 内容过长时截取前 MAX_DIRECT_CONTENT_LENGTH 字符，避免超出 token 限制
                    String context = contentMd.length() > MAX_DIRECT_CONTENT_LENGTH
                            ? contentMd.substring(0, MAX_DIRECT_CONTENT_LENGTH) + "\n...(内容已截断)"
                            : contentMd;
                    messages.add(SystemMessage.systemMessage(
                            "以下是当前课程《" + course.getTitle() + "》的完整内容，请基于它回答用户问题：\n" + context
                    ));
                    // 异步触发索引构建，下次提问时可使用向量检索
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
}
