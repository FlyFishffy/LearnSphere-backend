package com.flyfish.learnsphere.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flyfish.learnsphere.exception.BusinessException;
import com.flyfish.learnsphere.mapper.CourseChunkEmbeddingMapper;
import com.flyfish.learnsphere.model.entity.CourseChunkEmbedding;
import com.flyfish.learnsphere.model.enums.ErrorCode;
import com.flyfish.learnsphere.service.RagService;
import dev.langchain4j.model.embedding.EmbeddingModel;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * @Author: FlyFish
 * @CreateTime: 2026/1/20
 */
@Service
public class RagServiceImpl implements RagService {

    private static final int CHUNK_SIZE = 600;
    private static final int CHUNK_OVERLAP = 80;
    private static final int TOP_K = 5;

    private final EmbeddingModel embeddingModel;

    @Resource
    private CourseChunkEmbeddingMapper embeddingMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public RagServiceImpl(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @Override
    public void indexCourseContent(Long courseId, String contentMd) {
        if (courseId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "courseId is required");
        }
        if (contentMd == null || contentMd.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "contentMd is empty");
        }
        List<String> chunks = splitContent(contentMd);
        if (chunks.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "contentMd is empty");
        }
        embeddingMapper.deleteByCourseId(courseId);

        List<CourseChunkEmbedding> records = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            float[] vector = embeddingModel.embed(chunk).content().vector();
            String vectorJson = toJson(vector);
            CourseChunkEmbedding embedding = new CourseChunkEmbedding(
                    null,
                    courseId,
                    i,
                    chunk,
                    vectorJson,
                    LocalDateTime.now()
            );
            records.add(embedding);
        }
        if (!records.isEmpty()) {
            embeddingMapper.insertBatch(records);
        }
    }

    @Override
    public List<String> retrieveRelevantChunks(Long courseId, String question) {
        if (courseId == null || question == null || question.trim().isEmpty()) {
            return List.of();
        }
        float[] queryVector = embeddingModel.embed(question).content().vector();
        List<CourseChunkEmbedding> embeddings = embeddingMapper.listByCourseId(courseId);
        if (embeddings == null || embeddings.isEmpty()) {
            return List.of();
        }
        List<ScoredChunk> scored = new ArrayList<>();
        for (CourseChunkEmbedding embedding : embeddings) {
            float[] vector = parseVector(embedding.getVectorJson());
            double score = cosineSimilarity(queryVector, vector);
            scored.add(new ScoredChunk(embedding.getText(), score));
        }
        return scored.stream()
                .sorted(Comparator.comparingDouble(ScoredChunk::score).reversed())
                .limit(TOP_K)
                .map(ScoredChunk::text)
                .toList();
    }

    @Override
    public void deleteCourseIndex(Long courseId) {
        if (courseId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "courseId is required");
        }
        embeddingMapper.deleteByCourseId(courseId);
    }


    private List<String> splitContent(String content) {
        String normalized = content.replace("\r\n", "\n").trim();
        if (normalized.isEmpty()) {
            return List.of();
        }
        String[] paragraphs = normalized.split("\n\\s*\\n");
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String paragraph : paragraphs) {
            String p = paragraph.trim();
            if (p.isEmpty()) {
                continue;
            }
            if (current.length() + p.length() + 1 > CHUNK_SIZE) {
                chunks.add(current.toString());
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

    private String toJson(float[] vector) {
        try {
            return objectMapper.writeValueAsString(vector);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "vector json error");
        }
    }

    private float[] parseVector(String vectorJson) {
        if (vectorJson == null || vectorJson.isEmpty()) {
            return new float[0];
        }
        try {
            return objectMapper.readValue(vectorJson, float[].class);
        } catch (JsonProcessingException e) {
            return new float[0];
        }
    }

    private double cosineSimilarity(float[] a, float[] b) {
        if (a.length == 0 || b.length == 0) {
            return 0.0;
        }
        double dot = 0.0, normA = 0.0, normB = 0.0;
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB) + 1e-10);
    }

    private record ScoredChunk(String text, double score) {}
}

