package com.flyfish.learnsphere.mapper;

import com.flyfish.learnsphere.model.entity.RecommendClickLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Recommendation Click Log Mapper
 *
 * @Author: FlyFish
 */
@Mapper
public interface RecommendClickLogMapper {

    int insert(RecommendClickLog log);

    /**
     * Total click count (optionally filtered by source)
     */
    int countClicks(@Param("source") String source);

    /**
     * Count distinct users who clicked
     */
    int countClickedUsers(@Param("source") String source);

    /**
     * List click counts grouped by courseId, ordered by count desc
     */
    List<Map<String, Object>> listClickCountByCourse(@Param("source") String source,
                                                     @Param("limit") int limit);

    /**
     * List all click logs
     */
    List<RecommendClickLog> listAll();

    /**
     * Count clicks by a specific user
     */
    int countByUser(@Param("userId") Long userId);
}
