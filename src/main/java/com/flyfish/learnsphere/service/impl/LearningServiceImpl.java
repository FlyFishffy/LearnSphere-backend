package com.flyfish.learnsphere.service.impl;

import com.flyfish.learnsphere.exception.BusinessException;
import com.flyfish.learnsphere.mapper.CourseFavoriteMapper;
import com.flyfish.learnsphere.mapper.CourseMapper;
import com.flyfish.learnsphere.mapper.LearningRecordMapper;
import com.flyfish.learnsphere.model.dto.UpdateLearningRecordRequest;
import com.flyfish.learnsphere.model.entity.Course;
import com.flyfish.learnsphere.model.entity.CourseFavorite;
import com.flyfish.learnsphere.model.entity.LearningRecord;
import com.flyfish.learnsphere.model.enums.ErrorCode;
import com.flyfish.learnsphere.model.vo.LearningAnalysisVO;
import com.flyfish.learnsphere.model.vo.LearningReportVO;
import com.flyfish.learnsphere.service.LearningService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: FlyFish
 * @CreateTime: 2026/02/18
 */
@Service
public class LearningServiceImpl implements LearningService {

    @Resource
    private LearningRecordMapper learningRecordMapper;

    @Resource
    private CourseFavoriteMapper courseFavoriteMapper;

    @Resource
    private CourseMapper courseMapper;

    @Override
    public boolean updateRecord(UpdateLearningRecordRequest request, Long userId) {
        if (request == null || request.getCourseId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "courseId required");
        }
        validateProgress(request.getProgressPercent());
        LearningRecord record = learningRecordMapper.getByUserCourse(userId, request.getCourseId());
        LocalDateTime now = LocalDateTime.now();
        if (record == null) {
            LearningRecord newRecord = new LearningRecord();
            newRecord.setUserId(userId);
            newRecord.setCourseId(request.getCourseId());
            newRecord.setProgressPercent(defaultIfNull(request.getProgressPercent(), 0));
            newRecord.setCurrentSecond(request.getCurrentSecond());
            newRecord.setTotalSeconds(request.getTotalSeconds());
            newRecord.setTotalStudySeconds(defaultIfNull(request.getStudySecondsIncrement(), 0));
            newRecord.setLastLearningTime(now);
            newRecord.setCreateTime(now);
            newRecord.setUpdateTime(now);
            learningRecordMapper.insert(newRecord);
        } else {
            if (request.getProgressPercent() != null) {
                record.setProgressPercent(request.getProgressPercent());
            }
            if (request.getCurrentSecond() != null) {
                record.setCurrentSecond(request.getCurrentSecond());
            }
            if (request.getTotalSeconds() != null) {
                record.setTotalSeconds(request.getTotalSeconds());
            }
            if (request.getStudySecondsIncrement() != null) {
                record.setTotalStudySeconds(safeAdd(record.getTotalStudySeconds(), request.getStudySecondsIncrement()));
            }
            record.setLastLearningTime(now);
            record.setUpdateTime(now);
            learningRecordMapper.update(record);
        }
        return true;
    }

    @Override
    public List<LearningRecord> listRecords(Long userId) {
        return learningRecordMapper.listByUser(userId);
    }

    @Override
    public boolean addFavorite(Long courseId, Long userId) {
        if (courseId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "courseId required");
        }
        if (courseFavoriteMapper.exists(userId, courseId)) {
            return true;
        }
        CourseFavorite favorite = new CourseFavorite();
        favorite.setUserId(userId);
        favorite.setCourseId(courseId);
        favorite.setCreateTime(LocalDateTime.now());
        return courseFavoriteMapper.insert(favorite) > 0;
    }

    @Override
    public boolean removeFavorite(Long courseId, Long userId) {
        if (courseId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "courseId required");
        }
        return courseFavoriteMapper.delete(userId, courseId) > 0;
    }

    @Override
    public List<Course> listFavorites(Long userId) {
        List<Long> courseIds = courseFavoriteMapper.listCourseIdsByUser(userId);
        if (courseIds == null || courseIds.isEmpty()) {
            return List.of();
        }
        return courseMapper.listByIds(courseIds);
    }

    @Override
    public LearningAnalysisVO getAnalysis(Long userId) {
        List<LearningRecord> records = learningRecordMapper.listByUser(userId);
        LearningAnalysisVO vo = new LearningAnalysisVO();
        vo.setTotalStudySeconds(sumStudySeconds(records));
        vo.setLearningCourseCount(records.size());
        vo.setActiveDaysLast30(countActiveDays(records, 30));
        fillTopCategoryTag(vo, records);
        return vo;
    }

    @Override
    public LearningReportVO getReport(Long userId) {
        List<LearningRecord> records = learningRecordMapper.listByUser(userId);
        LearningReportVO report = new LearningReportVO();
        report.setTotalStudySeconds(sumStudySeconds(records));
        report.setLearningCourseCount(records.size());
        report.setFavoriteCourseCount(courseFavoriteMapper.countByUser(userId));
        report.setLastLearningTime(records.stream()
                .map(LearningRecord::getLastLearningTime)
                .filter(v -> v != null)
                .max(LocalDateTime::compareTo)
                .orElse(null));
        fillTopCategoryTag(report, records);
        return report;
    }

    @Override
    public List<Course> getRecommendations(Long userId, Integer limit) {
        int size = (limit == null || limit <= 0) ? 10 : Math.min(limit, 50);
        List<LearningRecord> records = learningRecordMapper.listByUser(userId);
        List<Long> favoriteIds = courseFavoriteMapper.listCourseIdsByUser(userId);
        Set<Long> excludeIds = new HashSet<>(favoriteIds == null ? List.of() : favoriteIds);
        excludeIds.addAll(records.stream().map(LearningRecord::getCourseId).collect(Collectors.toSet()));

        Map<String, Integer> tagCount = new HashMap<>();
        Map<String, Integer> categoryCount = new HashMap<>();
        List<Course> learnedCourses = listCoursesByIds(records.stream().map(LearningRecord::getCourseId).toList());
        List<Course> favoriteCourses = listCoursesByIds(favoriteIds == null ? List.of() : favoriteIds);
        List<Course> historyCourses = new ArrayList<>();
        historyCourses.addAll(learnedCourses);
        historyCourses.addAll(favoriteCourses);
        for (Course course : historyCourses) {
            if (course == null) {
                continue;
            }
            if (course.getCategory() != null) {
                categoryCount.merge(course.getCategory(), 1, Integer::sum);
            }
            for (String tag : splitTags(course.getTags())) {
                tagCount.merge(tag, 1, Integer::sum);
            }
        }

        List<String> topTags = sortTopKeys(tagCount, 3);
        List<String> topCategories = sortTopKeys(categoryCount, 2);

        LinkedHashMap<Long, Course> result = new LinkedHashMap<>();
        for (String tag : topTags) {
            List<Course> courses = courseMapper.listCourses(null, null, tag);
            for (Course course : courses) {
                if (result.size() >= size) {
                    break;
                }
                if (course != null && !excludeIds.contains(course.getId())) {
                    result.putIfAbsent(course.getId(), course);
                }
            }
            if (result.size() >= size) {
                break;
            }
        }
        for (String category : topCategories) {
            if (result.size() >= size) {
                break;
            }
            List<Course> courses = courseMapper.listCourses(null, category, null);
            for (Course course : courses) {
                if (result.size() >= size) {
                    break;
                }
                if (course != null && !excludeIds.contains(course.getId())) {
                    result.putIfAbsent(course.getId(), course);
                }
            }
        }
        return new ArrayList<>(result.values());
    }

    private void validateProgress(Integer progressPercent) {
        if (progressPercent == null) {
            return;
        }
        if (progressPercent < 0 || progressPercent > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "progressPercent 0-100");
        }
    }

    private int defaultIfNull(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private int safeAdd(Integer base, Integer delta) {
        int b = base == null ? 0 : base;
        int d = delta == null ? 0 : delta;
        return Math.max(0, b + d);
    }

    private int sumStudySeconds(List<LearningRecord> records) {
        int total = 0;
        for (LearningRecord record : records) {
            if (record.getTotalStudySeconds() != null) {
                total += record.getTotalStudySeconds();
            }
        }
        return total;
    }

    private int countActiveDays(List<LearningRecord> records, int days) {
        LocalDate cutoff = LocalDate.now().minusDays(days);
        Set<LocalDate> dates = new HashSet<>();
        for (LearningRecord record : records) {
            if (record.getLastLearningTime() == null) {
                continue;
            }
            LocalDate date = record.getLastLearningTime().toLocalDate();
            if (!date.isBefore(cutoff)) {
                dates.add(date);
            }
        }
        return dates.size();
    }

    private void fillTopCategoryTag(LearningAnalysisVO vo, List<LearningRecord> records) {
        List<Course> courses = listCoursesByIds(records.stream().map(LearningRecord::getCourseId).toList());
        Map<String, Integer> categoryCount = new HashMap<>();
        Map<String, Integer> tagCount = new HashMap<>();
        for (Course course : courses) {
            if (course == null) {
                continue;
            }
            if (course.getCategory() != null) {
                categoryCount.merge(course.getCategory(), 1, Integer::sum);
            }
            for (String tag : splitTags(course.getTags())) {
                tagCount.merge(tag, 1, Integer::sum);
            }
        }
        vo.setTopCategory(topKey(categoryCount));
        vo.setTopTag(topKey(tagCount));
    }

    private void fillTopCategoryTag(LearningReportVO vo, List<LearningRecord> records) {
        List<Course> courses = listCoursesByIds(records.stream().map(LearningRecord::getCourseId).toList());
        Map<String, Integer> categoryCount = new HashMap<>();
        Map<String, Integer> tagCount = new HashMap<>();
        for (Course course : courses) {
            if (course == null) {
                continue;
            }
            if (course.getCategory() != null) {
                categoryCount.merge(course.getCategory(), 1, Integer::sum);
            }
            for (String tag : splitTags(course.getTags())) {
                tagCount.merge(tag, 1, Integer::sum);
            }
        }
        vo.setTopCategory(topKey(categoryCount));
        vo.setTopTag(topKey(tagCount));
    }

    private List<Course> listCoursesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return courseMapper.listByIds(ids);
    }

    private List<String> splitTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return List.of();
        }
        String[] arr = tags.split(",");
        List<String> list = new ArrayList<>();
        for (String item : arr) {
            String trimmed = item.trim();
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }
        return list;
    }

    private String topKey(Map<String, Integer> counts) {
        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private List<String> sortTopKeys(Map<String, Integer> counts, int size) {
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(size)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
