package com.flyfish.learnsphere.model.vo;

import lombok.Data;

/**
 * Retrieval result chunk with similarity score and source info
 * @Author: FlyFish
 */
@Data
public class RetrievalChunkVO {
    /**
     * Chunk text content
     */
    private String text;

    /**
     * Heading hierarchy path (e.g. "Chapter 1 > Section 1.1")
     */
    private String heading;

    /**
     * Source type (e.g. "markdown", "pdf", "docx", "txt", "manual")
     */
    private String source;

    /**
     * Cosine similarity score (0~1, higher is more relevant)
     */
    private Double score;

    /**
     * Chunk index within the document
     */
    private Integer chunkIndex;
}
