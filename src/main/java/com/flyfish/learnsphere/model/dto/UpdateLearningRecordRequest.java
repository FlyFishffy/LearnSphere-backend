package com.flyfish.learnsphere.model.dto;

import lombok.Data;

/**
 * 学习记录更新请求
 * @Author: FlyFish
 * @CreateTime: 2026/02/18
 */
@Data
public class UpdateLearningRecordRequest {

    private Long courseId;

    private Integer progressPercent;

    private Integer currentSecond;

    private Integer totalSeconds;

    /**
     * 本次学习新增时长（秒）
     */
    private Integer studySecondsIncrement;
}
