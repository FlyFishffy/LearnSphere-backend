package com.flyfish.learnsphere.mapper;

import com.flyfish.learnsphere.model.entity.LearningRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学习记录 Mapper
 * @Author: FlyFish
 * @CreateTime: 2026/02/18
 */
@Mapper
public interface LearningRecordMapper {

    LearningRecord getByUserCourse(@Param("userId") Long userId, @Param("courseId") Long courseId);

    int insert(LearningRecord record);

    int update(LearningRecord record);

    List<LearningRecord> listByUser(@Param("userId") Long userId);
}
