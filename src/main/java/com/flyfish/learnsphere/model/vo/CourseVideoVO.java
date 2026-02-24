package com.flyfish.learnsphere.model.vo;

import lombok.Data;

/**
 * 课程视频信息
 * @Author: FlyFish
 * @CreateTime: 2026/02/18
 */
@Data
public class CourseVideoVO {

    private Long courseId;

    private String title;

    private String videoUrl;

    private Integer videoDuration;
}
