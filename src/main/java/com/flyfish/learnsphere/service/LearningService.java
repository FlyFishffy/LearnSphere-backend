package com.flyfish.learnsphere.service;

import com.flyfish.learnsphere.model.dto.UpdateLearningRecordRequest;
import com.flyfish.learnsphere.model.entity.Course;
import com.flyfish.learnsphere.model.entity.LearningRecord;
import com.flyfish.learnsphere.model.vo.LearningAnalysisVO;
import com.flyfish.learnsphere.model.vo.LearningReportVO;

import java.util.List;

/**
 * 学习记录与分析服务
 * @Author: FlyFish
 * @CreateTime: 2026/02/18
 */
public interface LearningService {

    boolean updateRecord(UpdateLearningRecordRequest request, Long userId);

    List<LearningRecord> listRecords(Long userId);

    boolean addFavorite(Long courseId, Long userId);

    boolean removeFavorite(Long courseId, Long userId);

    List<Course> listFavorites(Long userId);

    LearningAnalysisVO getAnalysis(Long userId);

    LearningReportVO getReport(Long userId);

    List<Course> getRecommendations(Long userId, Integer limit);
}
