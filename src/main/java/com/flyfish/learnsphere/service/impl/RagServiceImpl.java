package com.flyfish.learnsphere.service.impl;

import com.flyfish.learnsphere.exception.BusinessException;
import com.flyfish.learnsphere.model.enums.ErrorCode;
import com.flyfish.learnsphere.service.RagService;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * RAG 服务实现 —— 基于 PostgreSQL + pgvector
 * @Author: FlyFish
 * @CreateTime: 2026/1/20
 */
@Slf4j
@Service
public class RagServiceImpl implements RagService {

    private static final int CHUNK_SIZE = 600;
    private static final int CHUNK_OVERLAP = 80;
    private static final int TOP_K = 5;

    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate vectorJdbcTemplate;

    public RagServiceImpl(EmbeddingModel embeddingModel,
                          @Qualifier("vectorJdbcTemplate") JdbcTemplate vectorJdbcTemplate) {
        this.embeddingModel = embeddingModel;
        this.vectorJdbcTemplate = vectorJdbcTemplate;
    }

    @Override
    @Transactional
    public void indexCourseContent(Long courseId, String contentMd) {
        if (courseId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "courseId is required");
        }
        if (contentMd == null || contentMd.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "contentMd is empty");
        }
        List<String> chunks = splitContent(contentMd);
        if (chunks.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "contentMd is empty after splitting");
        }

        // 先删除旧索引
        vectorJdbcTemplate.update("DELETE FROM course_chunk_embedding WHERE course_id = ?", courseId);

        // 批量插入新向量
        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            float[] vector = embeddingModel.embed(chunk).content().vector();
            String pgVector = toPgVectorLiteral(vector);
            vectorJdbcTemplate.update(
                    "INSERT INTO course_chunk_embedding(course_id, chunk_index, text, embedding, create_time) " +
                    "VALUES (?, ?, ?, ?::vector, NOW())",
                    courseId, i, chunk, pgVector
            );
        }
        log.info("Indexed {} chunks for courseId={}", chunks.size(), courseId);
    }

    @Override
    public List<String> retrieveRelevantChunks(Long courseId, String question) {
        if (courseId == null || question == null || question.trim().isEmpty()) {
            return List.of();
        }

        // 检查是否有索引
        Integer count = vectorJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM course_chunk_embedding WHERE course_id = ?",
                Integer.class, courseId);
        if (count == null || count == 0) {
            log.warn("No embedding index found for courseId={}, falling back to no context", courseId);
            return List.of();
        }

        float[] queryVector = embeddingModel.embed(question).content().vector();
        String pgVector = toPgVectorLiteral(queryVector);

        // 使用 pgvector 的余弦距离操作符 <=> 进行相似度检索
        List<String> results = vectorJdbcTemplate.queryForList(
                "SELECT text FROM course_chunk_embedding " +
                "WHERE course_id = ? " +
                "ORDER BY embedding <=> ?::vector " +
                "LIMIT ?",
                String.class,
                courseId, pgVector, TOP_K
        );
        log.info("Retrieved {} relevant chunks for courseId={}", results.size(), courseId);
        return results;
    }

    @Override
    public void deleteCourseIndex(Long courseId) {
        if (courseId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "courseId is required");
        }
        int deleted = vectorJdbcTemplate.update(
                "DELETE FROM course_chunk_embedding WHERE course_id = ?", courseId);
        log.info("Deleted {} chunks for courseId={}", deleted, courseId);
    }

    /**
     * 将 float[] 转换为 pgvector 字面量格式，如 [0.1,0.2,0.3]
     */
    private String toPgVectorLiteral(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 按段落滑动窗口切分 Markdown 内容
     */
    private List<String> splitContent(String content) {
        String normalized = content.replace("\r\n", "\n").trim();
        if (normalized.isEmpty()) {
            return List.of();
        }
        String[] paragraphs = normalized.split("\n\\s*\n");
        List<String> chunks = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String paragraph : paragraphs) {
            String p = paragraph.trim();
            if (p.isEmpty()) continue;
            if (current.length() + p.length() + 1 > CHUNK_SIZE) {
                if (current.length() > 0) {
                    chunks.add(current.toString());
                }
                String overlap = current.substring(Math.max(0, current.length() - CHUNK_OVERLAP));
                current = new StringBuilder(overlap);
            }
            if (current.length() > 0) {
                current.append("\n\n");
            }
            current.append(p);
        }
        if (current.length() > 0) {
            chunks.add(current.toString());
        }
        return chunks;
    }
}
