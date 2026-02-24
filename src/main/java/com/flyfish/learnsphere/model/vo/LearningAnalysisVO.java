package com.flyfish.learnsphere.model.vo;

import lombok.Data;

/**
 * 学习习惯分析
 * @Author: FlyFish
 * @CreateTime: 2026/02/18
 */
@Data
public class LearningAnalysisVO {

    private Integer totalStudySeconds;

    private Integer activeDaysLast30;

    private Integer learningCourseCount;

    private String topCategory;

    private String topTag;
}
