package com.flyfish.learnsphere.mapper;


import com.flyfish.learnsphere.model.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


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


    /**
     * 查询课程列表
     * @param keyword
     * @param category
     * @param tag
     * @return
     */
    List<Course> listCourses(@Param("keyword") String keyword,
                             @Param("category") String category,
                             @Param("tag") String tag);


    /**
     * 删除课程（逻辑删除）
     * @param courseId
     * @return
     */
    int deleteCourse(@Param("courseId") Long courseId);


    /**
     * 批量查询课程
     * @param ids
     * @return
     */
    List<Course> listByIds(@Param("ids") List<Long> ids);
}
