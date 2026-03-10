package com.flyfish.learnsphere.model.dto;

import lombok.Data;

/**
 * Request body for submitting chat evaluation (thumbs up/down + rating)
 *
 * @Author: FlyFish
 */
@Data
public class ChatEvaluationRequest {

    /**
     * Session ID of the conversation
     */
    private String sessionId;

    /**
     * Associated course ID (nullable)
     */
    private Long courseId;

    /**
     * The user question
     */
    private String question;

    /**
     * The AI-generated answer being evaluated
     */
    private String aiAnswer;

    /**
     * Thumbs feedback: 1=thumbs up, -1=thumbs down
     */
    private Integer thumbs;

    /**
     * Star rating: 1-5 (optional)
     */
    private Integer rating;

    /**
     * Optional user comment
     */
    private String comment;
}
