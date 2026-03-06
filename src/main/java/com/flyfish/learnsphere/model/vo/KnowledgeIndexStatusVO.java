package com.flyfish.learnsphere.model.vo;

import lombok.Data;

/**
 * Knowledge base index status summary
 * @Author: FlyFish
 */
@Data
public class KnowledgeIndexStatusVO {
    private Long courseId;
    private String courseTitle;
    private Integer chunkCount;
    private String lastIndexTime;
    private Boolean indexed;
}
