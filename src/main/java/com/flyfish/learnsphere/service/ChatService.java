package com.flyfish.learnsphere.service;


import com.flyfish.learnsphere.model.dto.ChatRequest;
import com.flyfish.learnsphere.model.vo.MessageVO;
import dev.langchain4j.data.message.ChatMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * @Author: FlyFish
 * @CreateTime: 2025/11/26
 */
public interface ChatService {

    /**
     * 处理用户提问
     * @param chatRequest
     * @param userId
     * @return
     */
    SseEmitter ask(ChatRequest chatRequest, Long userId);


    /**
     * 获取用户会话历史
     * @param userId
     * @return
     */
    List<String> getSessions(Long userId);


    /**
     * 查询当前session下的历史聊天记录
     * @param sessionId
     * @return
     */
    List<MessageVO> getSessionHistory(Long userId, String sessionId);

    /**
     * Batch get session titles (first user message as title) for all given session IDs
     * @param userId
     * @param sessionIds
     * @return Map: sessionId -> title
     */
    Map<String, String> getSessionTitles(Long userId, List<String> sessionIds);
}
