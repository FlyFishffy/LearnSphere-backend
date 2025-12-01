package com.flyfish.learnsphere.controller;


import com.flyfish.learnsphere.annotaion.AuthCheck;
import com.flyfish.learnsphere.exception.BusinessException;
import com.flyfish.learnsphere.model.dto.AddCourseRequest;
import com.flyfish.learnsphere.model.dto.UpdateCourseRequest;
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
        Long userId = user.getId();
        boolean res = courseService.updateCourse(updateCourseRequest, userId);
        return ResultUtils.success(res);
    }
}
