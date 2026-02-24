package com.flyfish.learnsphere.component;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flyfish.learnsphere.model.entity.CourseChunkEmbedding;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * @Author: FlyFish
 * @CreateTime: 2026/1/20
 */
@Component
public class SimpleVectorStore {

    private final List<CourseChunkEmbedding> store = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();


    public void add(CourseChunkEmbedding embedding) {
        store.add(embedding);
    }

    public List<CourseChunkEmbedding> search(float[] queryVector, int topK, Long courseId) {

        PriorityQueue<CourseChunkEmbedding> pq =
                new PriorityQueue<>(Comparator.comparingDouble(
                        e -> -cosineSimilarity(queryVector, parseVector(e.getVectorJson()))
                ));

        for (CourseChunkEmbedding e : store) {
            if (courseId != null && !courseId.equals(e.getCourseId())) {
                continue;
            }
            pq.offer(e);
        }


        List<CourseChunkEmbedding> result = new ArrayList<>();
        for (int i = 0; i < topK && !pq.isEmpty(); i++) {
            result.add(pq.poll());
        }
        return result;
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
}