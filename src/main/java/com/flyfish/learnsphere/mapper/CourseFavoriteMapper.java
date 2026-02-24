package com.flyfish.learnsphere.mapper;

import com.flyfish.learnsphere.model.entity.CourseFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 课程收藏 Mapper
 * @Author: FlyFish
 * @CreateTime: 2026/02/18
 */
@Mapper
public interface CourseFavoriteMapper {

    boolean exists(@Param("userId") Long userId, @Param("courseId") Long courseId);

    int insert(CourseFavorite favorite);

    int delete(@Param("userId") Long userId, @Param("courseId") Long courseId);

    List<Long> listCourseIdsByUser(@Param("userId") Long userId);

    int countByUser(@Param("userId") Long userId);
}
