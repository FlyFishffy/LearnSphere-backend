package com.flyfish.learnsphere.model.vo;

import lombok.Data;

/**
 * VO for chat evaluation statistics
 *
 * @Author: FlyFish
 */
@Data
public class ChatEvaluationStatsVO {

    /**
     * Total number of evaluations
     */
    private Integer totalCount;

    /**
     * Thumbs up count
     */
    private Integer thumbsUpCount;

    /**
     * Thumbs down count
     */
    private Integer thumbsDownCount;

    /**
     * Average star rating (1.0 - 5.0)
     */
    private Double averageRating;

    /**
     * Satisfaction rate = thumbsUp / (thumbsUp + thumbsDown) * 100
     */
    private Double satisfactionRate;

    /**
     * Count of evaluations with star rating
     */
    private Integer ratedCount;
}
