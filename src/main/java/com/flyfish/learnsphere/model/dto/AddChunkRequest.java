package com.flyfish.learnsphere.model.dto;

import lombok.Data;

/**
 * Request to add a manual knowledge chunk
 * @Author: FlyFish
 */
@Data
public class AddChunkRequest {
    private Long courseId;
    private String text;
    private String heading;
}
