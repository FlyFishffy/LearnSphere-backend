package com.flyfish.learnsphere.service;


import com.flyfish.learnsphere.model.vo.ChunkVO;
import com.flyfish.learnsphere.model.vo.KnowledgeIndexStatusVO;

import java.util.List;

/**
 * @Author: FlyFish
 * @CreateTime: 2026/1/20
 */
public interface RagService {
    /**
     * Build course knowledge index from markdown content (heading-aware splitting)
     */
    void indexCourseContent(Long courseId, String contentMd);

    /**
     * Build course knowledge index from uploaded document text
     * @param courseId  course id
     * @param text      parsed document text
     * @param source    source type: "pdf", "docx", "txt"
     */
    void indexDocumentContent(Long courseId, String text, String source);

    /**
     * Retrieve relevant chunks for a question
     */
    List<String> retrieveRelevantChunks(Long courseId, String question);

    /**
     * Delete course knowledge index
     */
    void deleteCourseIndex(Long courseId);

    /**
     * List all chunks for a course (for preview/management)
     */
    List<ChunkVO> listChunks(Long courseId);

    /**
     * Get knowledge index status for a course
     */
    KnowledgeIndexStatusVO getIndexStatus(Long courseId);

    /**
     * Add a single manual chunk
     */
    ChunkVO addManualChunk(Long courseId, String text, String heading);

    /**
     * Update a chunk's text and heading, re-compute embedding
     */
    ChunkVO updateChunk(Long chunkId, String text, String heading);

    /**
     * Delete a single chunk by id
     */
    void deleteChunk(Long chunkId);
}


