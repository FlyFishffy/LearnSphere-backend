package com.flyfish.learnsphere.mapper;


import com.flyfish.learnsphere.model.entity.Course;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: FlyFish
 * @CreateTime: 2025/11/23
 */
@Mapper
public interface CourseMapper {

    /**
     * 添加课程
     * @param course
     * @return
     */
    Long addCourse(Course course);


    /**
     * 根据id获取课程信息
     * @param courseId
     * @return
     */
    Course getCourseById(Long courseId);


    /**
     * 更新课程信息
     * @param course
     */
    void updateCourse(Course course);
}
