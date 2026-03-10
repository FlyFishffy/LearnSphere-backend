package com.flyfish.learnsphere.model.dto;

import lombok.Data;

/**
 * Request body for logging a recommendation click event
 *
 * @Author: FlyFish
 */
@Data
public class RecommendClickRequest {

    /**
     * The course that was clicked
     */
    private Long courseId;

    /**
     * Source of the recommendation: "home" / "learning_center"
     */
    private String source;

    /**
     * Total number of recommended courses shown at the time
     */
    private Integer recommendCount;

    /**
     * Position index of the clicked course in the recommendation list (0-based)
     */
    private Integer position;
}
