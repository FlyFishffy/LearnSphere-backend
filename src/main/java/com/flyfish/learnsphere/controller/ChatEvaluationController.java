package com.flyfish.learnsphere.controller;

import com.flyfish.learnsphere.annotaion.AuthCheck;
import com.flyfish.learnsphere.exception.BusinessException;
import com.flyfish.learnsphere.mapper.ChatEvaluationMapper;
import com.flyfish.learnsphere.model.dto.ChatEvaluationRequest;
import com.flyfish.learnsphere.model.entity.ChatEvaluation;
import com.flyfish.learnsphere.model.entity.Result;
import com.flyfish.learnsphere.model.entity.User;
import com.flyfish.learnsphere.model.enums.ErrorCode;
import com.flyfish.learnsphere.model.enums.RoleType;
import com.flyfish.learnsphere.model.vo.ChatEvaluationStatsVO;
import com.flyfish.learnsphere.service.UserService;
import com.flyfish.learnsphere.utils.ResultUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Chat Evaluation Controller - user satisfaction rating on AI answers
 * Supports thumbs up/down and 1-5 star rating
 *
 * @Author: FlyFish
 */
@RestController
@RequestMapping("/evaluation")
@Slf4j
public class ChatEvaluationController {

    @Resource
    private ChatEvaluationMapper chatEvaluationMapper;

    @Resource
    private UserService userService;

    /**
     * Submit or update an evaluation (thumbs up/down + optional star rating)
     * If the user already evaluated this question in this session, update it.
     */
    @PostMapping("/submit")
    public Result<ChatEvaluation> submitEvaluation(@RequestBody ChatEvaluationRequest req,
                                                    HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (req.getSessionId() == null || req.getSessionId().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "sessionId is required");
        }
        if (req.getQuestion() == null || req.getQuestion().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "question is required");
        }
        if (req.getAiAnswer() == null || req.getAiAnswer().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "aiAnswer is required");
        }
        if (req.getThumbs() == null && req.getRating() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "thumbs or rating is required");
        }
        if (req.getThumbs() != null && req.getThumbs() != 1 && req.getThumbs() != -1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "thumbs must be 1 or -1");
        }
        if (req.getRating() != null && (req.getRating() < 1 || req.getRating() > 5)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "rating must be between 1 and 5");
        }

        // Check if already evaluated — if so, update
        ChatEvaluation existing = chatEvaluationMapper.getByUserSessionQuestion(
                user.getId(), req.getSessionId(), req.getQuestion());

        if (existing != null) {
            if (req.getThumbs() != null) {
                existing.setThumbs(req.getThumbs());
            }
            if (req.getRating() != null) {
                existing.setRating(req.getRating());
            }
            if (req.getComment() != null) {
                existing.setComment(req.getComment());
            }
            chatEvaluationMapper.update(existing);
            ChatEvaluation updated = chatEvaluationMapper.getById(existing.getId());
            return ResultUtils.success(updated);
        }

        // New evaluation
        ChatEvaluation evaluation = new ChatEvaluation();
        evaluation.setUserId(user.getId());
        evaluation.setSessionId(req.getSessionId());
        evaluation.setCourseId(req.getCourseId());
        evaluation.setQuestion(req.getQuestion());
        evaluation.setAiAnswer(req.getAiAnswer());
        evaluation.setThumbs(req.getThumbs() != null ? req.getThumbs() : 0);
        evaluation.setRating(req.getRating());
        evaluation.setComment(req.getComment());
        chatEvaluationMapper.insert(evaluation);

        log.info("User {} submitted evaluation (thumbs={}, rating={}) for session {}",
                user.getId(), req.getThumbs(), req.getRating(), req.getSessionId());
        return ResultUtils.success(evaluation);
    }

    /**
     * Get evaluation statistics (overall or per-course)
     */
    @GetMapping("/stats")
    public Result<ChatEvaluationStatsVO> getStats(
            @RequestParam(required = false) Long courseId) {
        ChatEvaluationStatsVO stats = new ChatEvaluationStatsVO();
        int thumbsUp = chatEvaluationMapper.countThumbsUp(courseId);
        int thumbsDown = chatEvaluationMapper.countThumbsDown(courseId);
        int total = chatEvaluationMapper.countAll(courseId);
        int rated = chatEvaluationMapper.countRated(courseId);
        Double avgRating = chatEvaluationMapper.averageRating(courseId);

        stats.setTotalCount(total);
        stats.setThumbsUpCount(thumbsUp);
        stats.setThumbsDownCount(thumbsDown);
        stats.setRatedCount(rated);
        stats.setAverageRating(avgRating != null ? Math.round(avgRating * 100.0) / 100.0 : null);
        if (thumbsUp + thumbsDown > 0) {
            stats.setSatisfactionRate(
                    Math.round((double) thumbsUp / (thumbsUp + thumbsDown) * 10000.0) / 100.0);
        } else {
            stats.setSatisfactionRate(null);
        }
        return ResultUtils.success(stats);
    }

    /**
     * List all evaluations for current user
     */
    @GetMapping("/my")
    public Result<List<ChatEvaluation>> listMyEvaluations(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return ResultUtils.success(chatEvaluationMapper.listByUser(user.getId()));
    }

    /**
     * List all evaluations by course (teacher/admin only)
     */
    @GetMapping("/course/{courseId}")
    @AuthCheck(value = {"Teacher", "Admin"})
    public Result<List<ChatEvaluation>> listByCourse(@PathVariable Long courseId) {
        return ResultUtils.success(chatEvaluationMapper.listByCourse(courseId));
    }

    /**
     * List all evaluations (admin only)
     */
    @GetMapping("/all")
    @AuthCheck(value = {"Admin"})
    public Result<List<ChatEvaluation>> listAll() {
        return ResultUtils.success(chatEvaluationMapper.listAll());
    }

    /**
     * Delete an evaluation
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteEvaluation(@PathVariable Long id, HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        ChatEvaluation existing = chatEvaluationMapper.getById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "Evaluation not found");
        }
        // Only the owner or admin can delete
        if (!existing.getUserId().equals(user.getId())
                && RoleType.getByValue(user.getRoleType()) != RoleType.ADMIN) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        chatEvaluationMapper.deleteById(id);
        return ResultUtils.success(true);
    }
}
