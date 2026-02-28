package com.flyfish.learnsphere.model.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: FlyFish
 * @CreateTime: 2026/1/20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseChunkEmbedding {
    private Long id;
    private Long courseId;
    private Integer chunkIndex;
    private String text;
    private String vectorJson;
    private java.time.LocalDateTime createTime;
}

