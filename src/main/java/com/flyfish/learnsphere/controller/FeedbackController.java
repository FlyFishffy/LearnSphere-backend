package com.flyfish.learnsphere.controller;

import com.flyfish.learnsphere.annotaion.AuthCheck;
import com.flyfish.learnsphere.exception.BusinessException;
import com.flyfish.learnsphere.mapper.LlmFeedbackMapper;
import com.flyfish.learnsphere.model.dto.LlmFeedbackRequest;
import com.flyfish.learnsphere.model.entity.LlmFeedback;
import com.flyfish.learnsphere.model.entity.Result;
import com.flyfish.learnsphere.model.entity.User;
import com.flyfish.learnsphere.model.enums.ErrorCode;
import com.flyfish.learnsphere.service.UserService;
import com.flyfish.learnsphere.utils.ResultUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * LLM Feedback Controller - Teacher correction & feedback on AI answers
 *
 * @Author: FlyFish
 */
@RestController
@RequestMapping("/feedback")
@Slf4j
public class FeedbackController {

    @Resource
    private LlmFeedbackMapper llmFeedbackMapper;

    @Resource
    private UserService userService;

    /**
     * Submit feedback (teacher correction / rating)
     */
    @PostMapping("/submit")
    @AuthCheck(value = {"Teacher", "Admin"})
    public Result<LlmFeedback> submitFeedback(@RequestBody LlmFeedbackRequest req,
                                               HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        if (req.getQuestion() == null || req.getQuestion().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "question is required");
        }
        if (req.getOriginalAnswer() == null || req.getOriginalAnswer().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "originalAnswer is required");
        }

        LlmFeedback feedback = new LlmFeedback();
        feedback.setTeacherId(user.getId());
        feedback.setCourseId(req.getCourseId());
        feedback.setSessionId(req.getSessionId());
        feedback.setQuestion(req.getQuestion());
        feedback.setOriginalAnswer(req.getOriginalAnswer());
        feedback.setCorrectedAnswer(req.getCorrectedAnswer());
        feedback.setRating(req.getRating());
        feedback.setComment(req.getComment());
        feedback.setStatus(0); // pending

        llmFeedbackMapper.insert(feedback);
        log.info("Teacher {} submitted feedback for question: {}", user.getId(),
                req.getQuestion().substring(0, Math.min(50, req.getQuestion().length())));

        return ResultUtils.success(feedback);
    }

    /**
     * Update feedback
     */
    @PutMapping("/update")
    @AuthCheck(value = {"Teacher", "Admin"})
    public Result<LlmFeedback> updateFeedback(@RequestBody LlmFeedback req,
                                               HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (req.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id is required");
        }

        LlmFeedback existing = llmFeedbackMapper.getById(req.getId());
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "Feedback not found");
        }

        llmFeedbackMapper.update(req);
        LlmFeedback updated = llmFeedbackMapper.getById(req.getId());
        return ResultUtils.success(updated);
    }

    /**
     * Get feedback by ID
     */
    @GetMapping("/{id}")
    @AuthCheck(value = {"Teacher", "Admin"})
    public Result<LlmFeedback> getFeedback(@PathVariable Long id) {
        LlmFeedback feedback = llmFeedbackMapper.getById(id);
        if (feedback == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "Feedback not found");
        }
        return ResultUtils.success(feedback);
    }

    /**
     * List feedback by course
     */
    @GetMapping("/course/{courseId}")
    @AuthCheck(value = {"Teacher", "Admin"})
    public Result<List<LlmFeedback>> listByCourse(@PathVariable Long courseId) {
        List<LlmFeedback> list = llmFeedbackMapper.listByCourse(courseId);
        return ResultUtils.success(list);
    }

    /**
     * List feedback by current teacher
     */
    @GetMapping("/my")
    @AuthCheck(value = {"Teacher", "Admin"})
    public Result<List<LlmFeedback>> listMyFeedback(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        List<LlmFeedback> list = llmFeedbackMapper.listByTeacher(user.getId());
        return ResultUtils.success(list);
    }

    /**
     * List all feedback (admin)
     */
    @GetMapping("/all")
    @AuthCheck(value = {"Admin"})
    public Result<List<LlmFeedback>> listAll() {
        List<LlmFeedback> list = llmFeedbackMapper.listAll();
        return ResultUtils.success(list);
    }

    /**
     * Delete feedback
     */
    @DeleteMapping("/{id}")
    @AuthCheck(value = {"Teacher", "Admin"})
    public Result<Boolean> deleteFeedback(@PathVariable Long id, HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        LlmFeedback existing = llmFeedbackMapper.getById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "Feedback not found");
        }
        llmFeedbackMapper.deleteById(id);
        return ResultUtils.success(true);
    }
}
