package com.flyfish.learnsphere.model.dto;


import lombok.Data;

/**
 * AI对话请求
 * @Author: FlyFish
 * @CreateTime: 2025/11/26
 */
@Data
public class ChatRequest {

    private String question;

    // todo 用于保存上下文记忆
    private String sessionId;

    // 可选：课程ID，用于课程知识库问答
    private Long courseId;

}
