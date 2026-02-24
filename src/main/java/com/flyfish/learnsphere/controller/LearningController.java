package com.flyfish.learnsphere.controller;

import com.flyfish.learnsphere.exception.BusinessException;
import com.flyfish.learnsphere.model.dto.UpdateLearningRecordRequest;
import com.flyfish.learnsphere.model.entity.Course;
import com.flyfish.learnsphere.model.entity.LearningRecord;
import com.flyfish.learnsphere.model.entity.Result;
import com.flyfish.learnsphere.model.entity.User;
import com.flyfish.learnsphere.model.enums.ErrorCode;
import com.flyfish.learnsphere.model.vo.LearningAnalysisVO;
import com.flyfish.learnsphere.model.vo.LearningReportVO;
import com.flyfish.learnsphere.service.LearningService;
import com.flyfish.learnsphere.service.UserService;
import com.flyfish.learnsphere.utils.ResultUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学习记录与收藏
 * @Author: FlyFish
 * @CreateTime: 2026/02/18
 */
@RestController
@RequestMapping("/learning")
public class LearningController {

    @Resource
    private LearningService learningService;

    @Resource
    private UserService userService;

    @PostMapping("/record/update")
    public Result<Boolean> updateRecord(@RequestBody UpdateLearningRecordRequest request, HttpServletRequest httpRequest) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(httpRequest);
        return ResultUtils.success(learningService.updateRecord(request, user.getId()));
    }

    @GetMapping("/record/list")
    public Result<List<LearningRecord>> listRecords(HttpServletRequest httpRequest) {
        User user = userService.getLoginUser(httpRequest);
        return ResultUtils.success(learningService.listRecords(user.getId()));
    }

    @PostMapping("/favorite/{courseId}")
    public Result<Boolean> addFavorite(@PathVariable Long courseId, HttpServletRequest httpRequest) {
        User user = userService.getLoginUser(httpRequest);
        return ResultUtils.success(learningService.addFavorite(courseId, user.getId()));
    }

    @DeleteMapping("/favorite/{courseId}")
    public Result<Boolean> removeFavorite(@PathVariable Long courseId, HttpServletRequest httpRequest) {
        User user = userService.getLoginUser(httpRequest);
        return ResultUtils.success(learningService.removeFavorite(courseId, user.getId()));
    }

    @GetMapping("/favorite/list")
    public Result<List<Course>> listFavorites(HttpServletRequest httpRequest) {
        User user = userService.getLoginUser(httpRequest);
        return ResultUtils.success(learningService.listFavorites(user.getId()));
    }

    @GetMapping("/analysis")
    public Result<LearningAnalysisVO> getAnalysis(HttpServletRequest httpRequest) {
        User user = userService.getLoginUser(httpRequest);
        return ResultUtils.success(learningService.getAnalysis(user.getId()));
    }

    @GetMapping("/report")
    public Result<LearningReportVO> getReport(HttpServletRequest httpRequest) {
        User user = userService.getLoginUser(httpRequest);
        return ResultUtils.success(learningService.getReport(user.getId()));
    }

    @GetMapping("/recommend")
    public Result<List<Course>> getRecommendations(@RequestParam(required = false) Integer limit,
                                                   HttpServletRequest httpRequest) {
        User user = userService.getLoginUser(httpRequest);
        return ResultUtils.success(learningService.getRecommendations(user.getId(), limit));
    }
}
