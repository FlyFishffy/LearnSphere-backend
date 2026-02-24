package com.flyfish.learnsphere.controller;


import com.flyfish.learnsphere.annotaion.AuthCheck;
import com.flyfish.learnsphere.exception.BusinessException;
import com.flyfish.learnsphere.model.dto.AddCourseRequest;
import com.flyfish.learnsphere.model.dto.UpdateCourseRequest;
import com.flyfish.learnsphere.model.entity.Course;
import com.flyfish.learnsphere.model.entity.Result;
import com.flyfish.learnsphere.model.entity.User;
import com.flyfish.learnsphere.model.enums.ErrorCode;
import com.flyfish.learnsphere.service.CourseService;
import com.flyfish.learnsphere.service.UserService;
import com.flyfish.learnsphere.utils.ResultUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @Author: FlyFish
 * @CreateTime: 2025/11/23
 */
@RestController
@RequestMapping("/course")
@Slf4j
public class CourseController {

    @Resource
    private CourseService courseService;

    @Resource
    private UserService userService;

    /**
     * 获取课程列表（可选条件筛选）
     * @param keyword
     * @param category
     * @param tag
     * @return
     */
    @GetMapping("/list")
    public Result<List<Course>> listCourses(@RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) Integer category,
                                            @RequestParam(required = false) Integer tag) {
        List<Course> courses = courseService.listCourses(keyword, category, tag);
        return ResultUtils.success(courses);
    }


    /**
     * 获取课程详情
     * @param courseId
     * @return
     */
    @GetMapping("/get/{courseId}")
    public Result<Course> getCourse(@PathVariable Long courseId) {
        Course course = courseService.getCourseById(courseId);
        return ResultUtils.success(course);
    }


    /**
     * 获取课程视频信息
     * @param courseId
     * @return
     */
    @GetMapping("/video/{courseId}")
    public Result<com.flyfish.learnsphere.model.vo.CourseVideoVO> getCourseVideo(@PathVariable Long courseId) {
        return ResultUtils.success(courseService.getCourseVideo(courseId));
    }


    /**
     * 添加课程

     * @param addCourseRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(value = {"Teacher", "Admin"})
    public Result<Long> addCourse(@RequestBody AddCourseRequest addCourseRequest, HttpServletRequest request){
        log.info("<=====================================================>");
        log.info("Add course. Request info:{}", addCourseRequest);
        if(addCourseRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        if(user == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long userId = user.getId();
        Long courseId = courseService.addCourse(addCourseRequest, userId);
        return ResultUtils.success(courseId);
    }



    @PutMapping("/update")
    @AuthCheck(value = {"Teacher", "Admin"})
    public Result<Boolean> updateCourse(@RequestBody UpdateCourseRequest updateCourseRequest, HttpServletRequest request){
        log.info("<=====================================================>");
        log.info("Update course. Request info:{}", updateCourseRequest);
        if(updateCourseRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        if(user == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        boolean res = courseService.updateCourse(updateCourseRequest, user);
        return ResultUtils.success(res);
    }



    /**
     * 删除课程（逻辑删除）
     * @param courseId
     * @param request
     * @return
     */
    @DeleteMapping("/delete/{courseId}")
    @AuthCheck(value = {"Teacher", "Admin"})
    public Result<Boolean> deleteCourse(@PathVariable Long courseId, HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        if(user == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Boolean res = courseService.deleteCourse(courseId, user);
        return ResultUtils.success(res);
    }
}
