package com.flyfish.learnsphere.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Knowledge chunk detail returned to the frontend
 * @Author: FlyFish
 */
@Data
public class ChunkVO {
    private Long id;
    private Long courseId;
    private Integer chunkIndex;
    private String text;
    private String heading;
    private String source;
    private LocalDateTime createTime;
}
