package com.flyfish.learnsphere.service;


import java.util.List;

/**
 * @Author: FlyFish
 * @CreateTime: 2026/1/20
 */
public interface RagService {
    /**
     * 构建课程知识库索引
     * @param courseId
     * @param contentMd
     */
    void indexCourseContent(Long courseId, String contentMd);


    /**
     * 根据 courseId + 用户问题，检索最相关的课程内容
     */
    List<String> retrieveRelevantChunks(Long courseId, String question);


    /**
     * 删除课程知识库索引
     * @param courseId
     */
    void deleteCourseIndex(Long courseId);
}


