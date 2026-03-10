package com.flyfish.learnsphere.controller;

import com.flyfish.learnsphere.annotaion.AuthCheck;
import com.flyfish.learnsphere.exception.BusinessException;
import com.flyfish.learnsphere.mapper.CourseMapper;
import com.flyfish.learnsphere.mapper.RecommendClickLogMapper;
import com.flyfish.learnsphere.model.dto.RecommendClickRequest;
import com.flyfish.learnsphere.model.entity.Course;
import com.flyfish.learnsphere.model.entity.RecommendClickLog;
import com.flyfish.learnsphere.model.entity.Result;
import com.flyfish.learnsphere.model.entity.User;
import com.flyfish.learnsphere.model.enums.ErrorCode;
import com.flyfish.learnsphere.model.vo.RecommendStatsVO;
import com.flyfish.learnsphere.service.UserService;
import com.flyfish.learnsphere.utils.ResultUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Recommendation Performance Controller - tracks and reports recommendation effectiveness metrics
 *
 * @Author: FlyFish
 */
@RestController
@RequestMapping("/recommend")
@Slf4j
public class RecommendStatsController {

    @Resource
    private RecommendClickLogMapper recommendClickLogMapper;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private UserService userService;

    /**
     * Log a recommendation click event.
     * Called when a user clicks a recommended course.
     */
    @PostMapping("/click")
    public Result<Boolean> logClick(@RequestBody RecommendClickRequest req,
                                    HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (req.getCourseId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "courseId is required");
        }

        RecommendClickLog clickLog = new RecommendClickLog();
        clickLog.setUserId(user.getId());
        clickLog.setCourseId(req.getCourseId());
        clickLog.setSource(req.getSource() != null ? req.getSource() : "home");
        clickLog.setRecommendCount(req.getRecommendCount());
        clickLog.setPosition(req.getPosition());
        recommendClickLogMapper.insert(clickLog);

        log.info("User {} clicked recommended course {} from source {}, position {}",
                user.getId(), req.getCourseId(), req.getSource(), req.getPosition());
        return ResultUtils.success(true);
    }

    /**
     * Get recommendation performance statistics (admin/teacher only)
     */
    @GetMapping("/stats")
    @AuthCheck(value = {"Teacher", "Admin"})
    public Result<RecommendStatsVO> getStats(
            @RequestParam(required = false) String source) {
        RecommendStatsVO stats = new RecommendStatsVO();

        int totalClicks = recommendClickLogMapper.countClicks(source);
        int clickedUsers = recommendClickLogMapper.countClickedUsers(source);

        stats.setTotalClicks(totalClicks);
        stats.setClickedUsers(clickedUsers);

        // Calculate impression count (estimate based on clicks * average recommend list size)
        // For accurate impressions, we'd need a separate impression log.
        // Here we use click log recommend_count as a proxy for impressions.
        stats.setTotalImpressions(null); // Will be set from frontend impression tracking
        stats.setClickThroughRate(null);
        stats.setUniqueUsers(null);
        stats.setUserClickRate(null);

        // Get top clicked course
        List<Map<String, Object>> topCourses = recommendClickLogMapper.listClickCountByCourse(source, 1);
        if (!topCourses.isEmpty()) {
            Map<String, Object> top = topCourses.get(0);
            Object courseIdObj = top.get("courseId");
            Object clickCountObj = top.get("clickCount");
            if (courseIdObj != null) {
                Long topCourseId = ((Number) courseIdObj).longValue();
                stats.setTopClickedCourseId(topCourseId);
                stats.setTopClickedCount(clickCountObj != null ? ((Number) clickCountObj).intValue() : 0);
                try {
                    Course course = courseMapper.getCourseById(topCourseId);
                    if (course != null) {
                        stats.setTopClickedCourseTitle(course.getTitle());
                    }
                } catch (Exception e) {
                    log.warn("Failed to get course title for top clicked course {}", topCourseId);
                }
            }
        }

        return ResultUtils.success(stats);
    }

    /**
     * Get detailed click count per course (admin/teacher only)
     */
    @GetMapping("/stats/courses")
    @AuthCheck(value = {"Teacher", "Admin"})
    public Result<List<Map<String, Object>>> getClickCountByCourse(
            @RequestParam(required = false) String source,
            @RequestParam(required = false, defaultValue = "20") int limit) {
        List<Map<String, Object>> result = recommendClickLogMapper.listClickCountByCourse(source, limit);
        // Enrich with course titles
        for (Map<String, Object> item : result) {
            Object courseIdObj = item.get("courseId");
            if (courseIdObj != null) {
                Long cid = ((Number) courseIdObj).longValue();
                try {
                    Course course = courseMapper.getCourseById(cid);
                    if (course != null) {
                        item.put("courseTitle", course.getTitle());
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return ResultUtils.success(result);
    }
}
