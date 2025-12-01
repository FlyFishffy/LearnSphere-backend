package com.flyfish.learnsphere.service.impl;


import com.flyfish.learnsphere.exception.BusinessException;
import com.flyfish.learnsphere.mapper.CourseMapper;
import com.flyfish.learnsphere.model.dto.AddCourseRequest;
import com.flyfish.learnsphere.model.dto.UpdateCourseRequest;
import com.flyfish.learnsphere.model.entity.Course;
import com.flyfish.learnsphere.model.enums.CourseCategory;
import com.flyfish.learnsphere.model.enums.CourseTag;
import com.flyfish.learnsphere.model.enums.ErrorCode;
import com.flyfish.learnsphere.service.CourseService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: FlyFish
 * @CreateTime: 2025/11/23
 */
@Service
public class CourseServiceImpl implements CourseService{

    @Resource
    private CourseMapper courseMapper;

    /**
     * 添加课程
     * @param addCourseRequest
     * @return
     */
    @Override
    public Long addCourse(AddCourseRequest addCourseRequest, Long userId) {
        if(addCourseRequest.getTitle() == null || addCourseRequest.getTitle().equals("")){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Must hava course title.");
        }
        CourseCategory courseCategory = CourseCategory.getByValue(addCourseRequest.getCategory());
        if(courseCategory == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Must hava course category.");
        }
        List<Integer> tags  = addCourseRequest.getTags();
        if(tags == null || tags.isEmpty()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Must hava course tag.");
        }
        List<String> tagStr = new ArrayList<>();
        for(Integer tag : tags){
            CourseTag courseTag = CourseTag.getByValue(tag);
            if(courseTag == null){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Course tag Error.");
            }
            tagStr.add(courseTag.getDescription());
        }
        Course course = new Course();
        BeanUtils.copyProperties(addCourseRequest, course);
        course.setUploadUserId(userId);
        course.setTags(String.join(",", tagStr));
        course.setCreateTime(LocalDateTime.now());
        course.setUpdateTime(LocalDateTime.now());
        course.setStatus(1);
        course.setIsDeleted(0);
        return courseMapper.addCourse(course);
    }


    /**
     * 更新课程
     * @param updateCourseRequest
     * @param userId
     * @return
     */
    @Override
    public Boolean updateCourse(UpdateCourseRequest updateCourseRequest, Long userId) {
        if(!isAllowedChange(updateCourseRequest.getId(), userId)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "You are not allowed to update other's course.");
        }
        Course course = new Course();
        if(updateCourseRequest.getTitle() != null && updateCourseRequest.getTitle().equals("")){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Course title can not be empty.");
        }
        course.setTitle(updateCourseRequest.getTitle());
        if(updateCourseRequest.getCategory() != null){
            CourseCategory courseCategory = CourseCategory.getByValue(updateCourseRequest.getCategory());
            if(courseCategory == null){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Course category Error.");
            }
            course.setCategory(courseCategory.getDescription());
        }
        if(updateCourseRequest.getTags() != null && !updateCourseRequest.getTags().isEmpty()){
            List<Integer> tags  = updateCourseRequest.getTags();
            List<String> tagStr = new ArrayList<>();
            for(Integer tag : tags){
                CourseTag courseTag = CourseTag.getByValue(tag);
                if(courseTag == null){
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "Course tag Error.");
                }
                tagStr.add(courseTag.getDescription());
            }
            course.setTags(String.join(",", tagStr));
        }
        course.setId(updateCourseRequest.getId());
        course.setUpdateTime(LocalDateTime.now());
        course.setDescription(updateCourseRequest.getDescription());
        course.setCoverUrl(updateCourseRequest.getCoverUrl());
        courseMapper.updateCourse(course);
        return true;
    }


    /**
     * 判断当前用户是否可以修改对应课程信息
     * @param courseId
     * @param curUserId
     * @return
     */
    private Boolean isAllowedChange(Long courseId, Long curUserId){
        Course originalCourse = courseMapper.getCourseById(courseId);
        if(originalCourse == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Course not found.");
        }
        if(curUserId != originalCourse.getUploadUserId()){
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
}
