package com.flyfish.learnsphere.service;


import com.flyfish.learnsphere.model.dto.AddCourseRequest;
import com.flyfish.learnsphere.model.dto.UpdateCourseRequest;
import com.flyfish.learnsphere.model.entity.Course;
import com.flyfish.learnsphere.model.entity.User;

import java.util.List;



/**
 * @Author: FlyFish
 * @CreateTime: 2025/11/23
 */
public interface CourseService {

    /**
     * 添加课程
     * @param addCourseRequest
     * @return
     */
    Long addCourse(AddCourseRequest addCourseRequest, Long userId);


    /**
     * 更新课程
     * @param updateCourseRequest
     * @param userId
     * @return
     */
    Boolean updateCourse(UpdateCourseRequest updateCourseRequest, User user);


    /**
     * 课程列表（可选条件筛选）
     * @param keyword
     * @param category
     * @param tag
     * @return
     */
    List<Course> listCourses(String keyword, Integer category, Integer tag);


    /**
     * 获取课程详情
     * @param courseId
     * @return
     */
    Course getCourseById(Long courseId);


    /**
     * 删除课程（逻辑删除）
     * @param courseId
     * @param user
     * @return
     */
    Boolean deleteCourse(Long courseId, User user);


    /**
     * 获取课程视频信息
     * @param courseId
     * @return
     */
    com.flyfish.learnsphere.model.vo.CourseVideoVO getCourseVideo(Long courseId);

}

