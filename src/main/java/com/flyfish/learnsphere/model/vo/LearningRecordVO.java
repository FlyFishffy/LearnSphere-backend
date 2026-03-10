package com.flyfish.learnsphere.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Learning record VO with course title for display
 * @Author: FlyFish
 */
@Data
public class LearningRecordVO {

    private Long id;

    private Long userId;

    private Long courseId;

    /**
     * Course title fetched from the course table
     */
    private String courseTitle;

    private Integer progressPercent;

    private Integer scrollPosition;

    private Integer contentLength;

    private Integer totalStudySeconds;

    private LocalDateTime lastLearningTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
