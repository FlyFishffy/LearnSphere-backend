package com.flyfish.learnsphere.service.impl;


import com.flyfish.learnsphere.exception.BusinessException;
import com.flyfish.learnsphere.mapper.CourseMapper;
import com.flyfish.learnsphere.model.dto.AddCourseRequest;
import com.flyfish.learnsphere.model.dto.UpdateCourseRequest;
import com.flyfish.learnsphere.model.entity.Course;
import com.flyfish.learnsphere.model.entity.User;
import com.flyfish.learnsphere.model.enums.CourseCategory;
import com.flyfish.learnsphere.model.enums.CourseTag;
import com.flyfish.learnsphere.model.enums.ErrorCode;
import com.flyfish.learnsphere.model.enums.RoleType;
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
        course.setCategory(courseCategory.getDescription());
        course.setContentMd(addCourseRequest.getContentMd());
        course.setVideoUrl(addCourseRequest.getVideoUrl());
        course.setVideoDuration(addCourseRequest.getVideoDuration());
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
    public Boolean updateCourse(UpdateCourseRequest updateCourseRequest, User user) {
        if(!canManageCourse(updateCourseRequest.getId(), user)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "You are not allowed to update other's course.");
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
        course.setContentMd(updateCourseRequest.getContentMd());
        course.setVideoUrl(updateCourseRequest.getVideoUrl());
        course.setVideoDuration(updateCourseRequest.getVideoDuration());

        courseMapper.updateCourse(course);
        return true;
    }


    @Override
    public com.flyfish.learnsphere.model.vo.CourseVideoVO getCourseVideo(Long courseId) {
        Course course = getCourseById(courseId);
        com.flyfish.learnsphere.model.vo.CourseVideoVO vo = new com.flyfish.learnsphere.model.vo.CourseVideoVO();
        vo.setCourseId(course.getId());
        vo.setTitle(course.getTitle());
        vo.setVideoUrl(course.getVideoUrl());
        vo.setVideoDuration(course.getVideoDuration());
        return vo;
    }



    @Override
    public List<Course> listCourses(String keyword, Integer category, Integer tag) {
        String categoryDesc = null;
        if(category != null){
            CourseCategory courseCategory = CourseCategory.getByValue(category);
            if(courseCategory == null){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Course category Error.");
            }
            categoryDesc = courseCategory.getDescription();
        }
        String tagDesc = null;
        if(tag != null){
            CourseTag courseTag = CourseTag.getByValue(tag);
            if(courseTag == null){
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Course tag Error.");
            }
            tagDesc = courseTag.getDescription();
        }
        return courseMapper.listCourses(keyword, categoryDesc, tagDesc);
    }


    @Override
    public Course getCourseById(Long courseId) {
        if(courseId == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Course course = courseMapper.getCourseById(courseId);
        if(course == null || course.getIsDeleted() != null && course.getIsDeleted() == 1){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "Course not found.");
        }
        return course;
    }


    @Override
    public Boolean deleteCourse(Long courseId, User user) {
        if(courseId == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if(!canManageCourse(courseId, user)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "You are not allowed to delete other's course.");
        }
        return courseMapper.deleteCourse(courseId) > 0;
    }


    /**
     * 判断当前用户是否可以修改/删除对应课程信息
     * 管理员可操作所有课程，老师仅可操作自己发布的课程
     * @param courseId
     * @param user
     * @return
     */
    private Boolean canManageCourse(Long courseId, User user){
        if(user == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        RoleType roleType = RoleType.getByValue(user.getRoleType());
        if(roleType == null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        if(roleType == RoleType.ADMIN){
            return Boolean.TRUE;
        }
        if(roleType != RoleType.TEACHER){
            return Boolean.FALSE;
        }
        Course originalCourse = courseMapper.getCourseById(courseId);
        if(originalCourse == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Course not found.");
        }
        return user.getId().equals(originalCourse.getUploadUserId());
    }
}
