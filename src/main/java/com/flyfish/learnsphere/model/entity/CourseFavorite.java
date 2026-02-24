package com.flyfish.learnsphere.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程收藏
 * @Author: FlyFish
 * @CreateTime: 2026/02/18
 */
@Data
public class CourseFavorite {

    private Long id;

    private Long userId;

    private Long courseId;

    private LocalDateTime createTime;
}
