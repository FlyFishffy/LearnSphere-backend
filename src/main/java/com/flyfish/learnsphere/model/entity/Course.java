package com.flyfish.learnsphere.model.entity;


import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: FlyFish
 * @CreateTime: 2025/11/23
 */
@Data
public class Course {

    private Long id;

    private String title;

    private String description;

    private Long uploadUserId;

    private String coverUrl;

    private String category;

    private String tags;

    private String contentMd;

    private String videoUrl;

    private Integer videoDuration;

    private Integer status;



    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDeleted;
}
