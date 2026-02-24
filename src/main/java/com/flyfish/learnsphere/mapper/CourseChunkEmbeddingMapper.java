package com.flyfish.learnsphere.mapper;

import com.flyfish.learnsphere.model.entity.CourseChunkEmbedding;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author: FlyFish
 * @CreateTime: 2026/02/14
 */
@Mapper
public interface CourseChunkEmbeddingMapper {

    int deleteByCourseId(@Param("courseId") Long courseId);

    int insertBatch(@Param("list") List<CourseChunkEmbedding> list);

    List<CourseChunkEmbedding> listByCourseId(@Param("courseId") Long courseId);

    int countByCourseId(@Param("courseId") Long courseId);
}

