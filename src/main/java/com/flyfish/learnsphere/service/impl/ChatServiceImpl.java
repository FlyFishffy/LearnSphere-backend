package com.flyfish.learnsphere.service.impl;


import com.flyfish.learnsphere.exception.BusinessException;
import com.flyfish.learnsphere.memory.RedisChatMemoryStore;
import com.flyfish.learnsphere.model.dto.ChatRequest;
import com.flyfish.learnsphere.model.enums.ErrorCode;
import com.flyfish.learnsphere.model.vo.MessageVO;
import com.flyfish.learnsphere.service.ChatService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    public ChatServiceImpl(ChatLanguageModel chatLanguageModel,  RedisChatMemoryStore chatMemoryStore) {
        this.chatLanguageModel = chatLanguageModel;
        this.chatMemoryStore = chatMemoryStore;
    }

    /**
     * 处理用户提问
     * @param chatRequest
     * @param userId
     * @return
     */
    @Override
    public String ask(ChatRequest chatRequest, Long userId) {
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
        String systemPrompt = "你叫FLYFISH AI, 专门用来解决用户的各种问题";
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(SystemMessage.systemMessage(systemPrompt));
        messages.addAll(history);
        ChatMessage userMessage = UserMessage.userMessage(question);
        messages.add(userMessage);
        String answer = chatLanguageModel.generate(messages).content().text();
        ChatMessage aiMessage =  AiMessage.aiMessage(answer);
        messages.add(aiMessage);

        chatMemoryStore.updateMessages(sessionId, messages);
        chatMemoryStore.addSession(userId, sessionId);
        return answer;
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
