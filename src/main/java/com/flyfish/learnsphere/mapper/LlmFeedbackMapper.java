package com.flyfish.learnsphere.mapper;

import com.flyfish.learnsphere.model.entity.LlmFeedback;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * LLM Feedback Mapper
 * @Author: FlyFish
 */
@Mapper
public interface LlmFeedbackMapper {

    int insert(LlmFeedback feedback);

    int update(LlmFeedback feedback);

    LlmFeedback getById(@Param("id") Long id);

    List<LlmFeedback> listByCourse(@Param("courseId") Long courseId);

    List<LlmFeedback> listByTeacher(@Param("teacherId") Long teacherId);

    List<LlmFeedback> listAll();

    int deleteById(@Param("id") Long id);
}
