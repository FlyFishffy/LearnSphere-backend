package com.flyfish.learnsphere.controller;

import com.flyfish.learnsphere.annotaion.AuthCheck;
import com.flyfish.learnsphere.exception.BusinessException;
import com.flyfish.learnsphere.mapper.CourseChunkEmbeddingMapper;
import com.flyfish.learnsphere.model.entity.Course;
import com.flyfish.learnsphere.model.entity.Result;
import com.flyfish.learnsphere.model.enums.ErrorCode;
import com.flyfish.learnsphere.service.CourseService;
import com.flyfish.learnsphere.service.RagService;
import com.flyfish.learnsphere.utils.ResultUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 知识库构建与管理
 * @Author: FlyFish
 * @CreateTime: 2026/02/14
 */
@RestController
@RequestMapping("/rag")
@Slf4j
public class RagController {

    @Resource
    private RagService ragService;

    @Resource
    private CourseService courseService;

    @Resource
    private CourseChunkEmbeddingMapper embeddingMapper;

    /**
     * 重建课程知识库索引
     * @param courseId
     * @return
     */
    @PostMapping("/index/{courseId}")
    @AuthCheck(value = {"Teacher", "Admin"})
    public Result<Boolean> buildCourseIndex(@PathVariable Long courseId) {
        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "Course not found.");
        }
        ragService.indexCourseContent(courseId, course.getContentMd());
        return ResultUtils.success(true);
    }


    /**
     * 查看课程知识库切片数量
     * @param courseId
     * @return
     */
    @GetMapping("/status/{courseId}")
    @AuthCheck(value = {"Teacher", "Admin"})
    public Result<Integer> getCourseIndexStatus(@PathVariable Long courseId) {
        int count = embeddingMapper.countByCourseId(courseId);
        return ResultUtils.success(count);
    }


    /**
     * 删除课程知识库索引
     * @param courseId
     * @return
     */
    @DeleteMapping("/index/{courseId}")
    @AuthCheck(value = {"Teacher", "Admin"})
    public Result<Boolean> deleteCourseIndex(@PathVariable Long courseId) {
        ragService.deleteCourseIndex(courseId);
        return ResultUtils.success(true);
    }
}
