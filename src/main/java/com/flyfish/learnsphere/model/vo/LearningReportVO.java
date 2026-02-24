package com.flyfish.learnsphere.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学习报告
 * @Author: FlyFish
 * @CreateTime: 2026/02/18
 */
@Data
public class LearningReportVO {

    private Integer totalStudySeconds;

    private Integer learningCourseCount;

    private Integer favoriteCourseCount;

    private String topCategory;

    private String topTag;

    private LocalDateTime lastLearningTime;
}
