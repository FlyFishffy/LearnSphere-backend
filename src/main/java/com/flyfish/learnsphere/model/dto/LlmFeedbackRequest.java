package com.flyfish.learnsphere.model.dto;

import lombok.Data;

/**
 * Request body for submitting LLM feedback (teacher correction)
 * @Author: FlyFish
 */
@Data
public class LlmFeedbackRequest {

    /**
     * Associated course ID (optional)
     */
    private Long courseId;

    /**
     * Session ID of the conversation
     */
    private String sessionId;

    /**
     * The original user question
     */
    private String question;

    /**
     * The original AI-generated answer
     */
    private String originalAnswer;

    /**
     * The corrected answer by teacher (nullable if only rating)
     */
    private String correctedAnswer;

    /**
     * Rating: 1-5 stars
     */
    private Integer rating;

    /**
     * Teacher's comment / feedback note
     */
    private String comment;
}
