package com.flyfish.learnsphere.service;


import com.flyfish.learnsphere.model.dto.AddCourseRequest;
import com.flyfish.learnsphere.model.dto.UpdateCourseRequest;

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
    Boolean updateCourse(UpdateCourseRequest updateCourseRequest, Long userId);
}
