package com.flyfish.learnsphere.model.vo;

import lombok.Data;

/**
 * VO for recommendation performance metrics
 *
 * @Author: FlyFish
 */
@Data
public class RecommendStatsVO {

    /**
     * Total recommendation impressions (number of times courses were shown as recommendations)
     */
    private Integer totalImpressions;

    /**
     * Total clicks on recommended courses
     */
    private Integer totalClicks;

    /**
     * Click-through rate (CTR) = totalClicks / totalImpressions * 100
     */
    private Double clickThroughRate;

    /**
     * Number of unique users who saw recommendations
     */
    private Integer uniqueUsers;

    /**
     * Number of unique users who clicked at least one recommendation
     */
    private Integer clickedUsers;

    /**
     * User click-through rate = clickedUsers / uniqueUsers * 100
     */
    private Double userClickRate;

    /**
     * Most clicked course ID
     */
    private Long topClickedCourseId;

    /**
     * Most clicked course title
     */
    private String topClickedCourseTitle;

    /**
     * Click count of the most clicked course
     */
    private Integer topClickedCount;
}
