package com.flyfish.learnsphere.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Learning record entity for document-based progress tracking
 * @Author: FlyFish
 * @CreateTime: 2026/02/18
 */
@Data
public class LearningRecord {

    private Long id;

    private Long userId;

    private Long courseId;

    /**
     * Reading progress percentage (0-100), calculated by scrollPosition / contentLength * 100
     */
    private Integer progressPercent;

    /**
     * Current scroll position in the document (characters read)
     */
    private Integer scrollPosition;

    /**
     * Total content length of the document (total characters)
     */
    private Integer contentLength;

    /**
     * Total accumulated study duration in seconds
     */
    private Integer totalStudySeconds;

    private LocalDateTime lastLearningTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
