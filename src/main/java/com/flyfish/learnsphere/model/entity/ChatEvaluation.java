package com.flyfish.learnsphere.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Chat evaluation entity - stores user satisfaction ratings on AI answers
 * (thumbs up/down, 1-5 star rating, optional comment)
 *
 * @Author: FlyFish
 */
@Data
public class ChatEvaluation {
    private Long id;

    /**
     * The user who submitted the evaluation
     */
    private Long userId;

    /**
     * Session ID of the conversation
     */
    private String sessionId;

    /**
     * Associated course ID (nullable)
     */
    private Long courseId;

    /**
     * The user question that triggered the AI answer
     */
    private String question;

    /**
     * The AI-generated answer being evaluated
     */
    private String aiAnswer;

    /**
     * Thumbs feedback: 1=thumbs up, -1=thumbs down, 0=none
     */
    private Integer thumbs;

    /**
     * Star rating: 1-5 (nullable if only thumbs)
     */
    private Integer rating;

    /**
     * Optional user comment on the answer quality
     */
    private String comment;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
