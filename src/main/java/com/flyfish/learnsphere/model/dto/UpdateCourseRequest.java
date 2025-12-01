package com.flyfish.learnsphere.model.dto;


import lombok.Data;

import java.util.List;

/**
 * @Author: FlyFish
 * @CreateTime: 2025/11/24
 */
@Data
public class UpdateCourseRequest {

    private Long id;

    private String title;

    private String description;

    private String coverUrl;

    private Integer category;

    private List<Integer> tags;
}
