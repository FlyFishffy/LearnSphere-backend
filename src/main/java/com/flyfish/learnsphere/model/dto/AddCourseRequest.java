package com.flyfish.learnsphere.model.dto;


import com.fasterxml.jackson.annotation.JsonAlias;
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

    @JsonAlias({"cover_url"})
    private String coverUrl;


    private Integer category;

    private List<Integer> tags;

    private String contentMd;

    private String videoUrl;

    private Integer videoDuration;

}
