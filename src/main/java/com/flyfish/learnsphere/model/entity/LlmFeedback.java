package com.flyfish.learnsphere.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * LLM Feedback entity - stores teacher corrections and ratings on AI answers
 * @Author: FlyFish
 */
@Data
public class LlmFeedback {
    private Long id;

    /**
     * The user ID of the teacher who provided feedback
     */
    private Long teacherId;

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

    /**
     * Status: 0=pending, 1=approved, 2=rejected
     */
    private Integer status;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
