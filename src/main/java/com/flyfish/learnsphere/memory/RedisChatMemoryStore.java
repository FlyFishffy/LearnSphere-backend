package com.flyfish.learnsphere.memory;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flyfish.learnsphere.constant.CommonConstant;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.Setter;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.*;

/**
 * @Author: FlyFish
 * @CreateTime: 2025/11/28
 */
public class RedisChatMemoryStore implements ChatMemoryStore {

    private final StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper;

    @Setter
    private Long userId;

    public RedisChatMemoryStore(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 生成存储用户每个对话具体内容的key
     * @param id    sessionId
     * @return
     */
    private String genUserSessionKey(String id){
        return CommonConstant.CHAT_MEMORY + this.userId + ":session:" + id;
    }


    /**
     * 生成存储用户所有历史会话的key
     * @param id    userId
     * @return
     */
    private String genUserHistoryKey(String id){
        return CommonConstant.CHAT_MEMORY + id;
    }

    @Override
    public List<ChatMessage> getMessages(Object id) {
        List<String> jsonList = stringRedisTemplate.opsForList().range(genUserSessionKey(String.valueOf(id)), 0, -1);
        List<ChatMessage> chatMessages = new ArrayList<>();
        if(jsonList != null && !jsonList.isEmpty()) {
            for(String json : jsonList) {
                try {
                    Map<String, String> map = objectMapper.readValue(json, Map.class);
                    String type = map.get("type");
                    String text = map.get("text");

                    ChatMessage message = switch (type) {
                        case "SYSTEM" -> SystemMessage.systemMessage(text);
                        case "USER" -> UserMessage.userMessage(text);
                        case "AI" -> AiMessage.aiMessage(text);
                        default -> throw new IllegalArgumentException("Unknown message type: " + type);
                    };
                    chatMessages.add(message);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
        return chatMessages;
    }

    @Override
    public void updateMessages(Object id, List<ChatMessage> messages) {
        String redisKey = genUserSessionKey(String.valueOf(id));

        stringRedisTemplate.delete(redisKey);

        for (ChatMessage msg : messages) {
            try {
                Map<String, String> map = new HashMap<>();

                // 根据消息类型提取文本内容
                if (msg instanceof SystemMessage) {
                    map.put("type", "SYSTEM");
                    map.put("text", ((SystemMessage) msg).text());
                } else if (msg instanceof UserMessage) {
                    map.put("type", "USER");
                    map.put("text", ((UserMessage) msg).text());
                } else if (msg instanceof AiMessage) {
                    map.put("type", "AI");
                    map.put("text", ((AiMessage) msg).text());
                }

                String json = objectMapper.writeValueAsString(map);
                stringRedisTemplate.opsForList().rightPush(redisKey, json);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void deleteMessages(Object id) {
        stringRedisTemplate.delete(genUserSessionKey(String.valueOf(id)));
    }


    /**
     * 获取用户所有会话id
     * @param userId
     * @return
     */
    public List<String> listHistory(Long userId) {
        String key = genUserHistoryKey(String.valueOf(userId));
        Set<String> res = stringRedisTemplate.opsForZSet().reverseRange(key, 0, -1);
        return res == null ? List.of() : new ArrayList<>(res);
    }


    /**
     * 将
     * @param userId
     * @param sessionId
     */
    public void addSession(Long userId, String sessionId) {
        this.setUserId(userId);
        String key = genUserHistoryKey(String.valueOf(userId));
        double score = System.currentTimeMillis();
        stringRedisTemplate.opsForZSet().add(key, sessionId, score);
    }
}
