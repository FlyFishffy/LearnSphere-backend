package com.flyfish.learnsphere.model.dto;

import lombok.Data;

/**
 * Request to update a knowledge chunk
 * @Author: FlyFish
 */
@Data
public class UpdateChunkRequest {
    private Long id;
    private String text;
    private String heading;
}
