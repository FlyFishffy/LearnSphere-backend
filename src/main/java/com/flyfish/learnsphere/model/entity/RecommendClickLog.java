package com.flyfish.learnsphere.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Recommendation click log entity - tracks when a user clicks a recommended course.
 * Used to calculate recommendation click-through rate (CTR) and effectiveness metrics.
 *
 * @Author: FlyFish
 */
@Data
public class RecommendClickLog {
    private Long id;

    /**
     * The user who clicked
     */
    private Long userId;

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

    private LocalDateTime createTime;
}
