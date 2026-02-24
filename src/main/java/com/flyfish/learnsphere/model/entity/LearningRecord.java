package com.flyfish.learnsphere.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学习记录
 * @Author: FlyFish
 * @CreateTime: 2026/02/18
 */
@Data
public class LearningRecord {

    private Long id;

    private Long userId;

    private Long courseId;

    private Integer progressPercent;

    private Integer currentSecond;

    private Integer totalSeconds;

    private Integer totalStudySeconds;

    private LocalDateTime lastLearningTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
