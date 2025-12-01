package com.flyfish.learnsphere.model.dto;


import lombok.Data;

import java.util.List;

/**
 * 新增课程请求
 * @Author: FlyFish
 * @CreateTime: 2025/11/23
 */
@Data
public class AddCourseRequest {

    private String title;

    private String description;

    private String cover_url;

    private Integer category;

    private List<Integer> tags;
}
