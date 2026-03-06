package com.flyfish.learnsphere.model.dto;

import lombok.Data;

/**
 * Learning record update request for document-based progress
 * @Author: FlyFish
 * @CreateTime: 2026/02/18
 */
@Data
public class UpdateLearningRecordRequest {

    private Long courseId;

    /**
     * Current scroll position in the document (characters the user has read up to)
     */
    private Integer scrollPosition;

    /**
     * Total content length of the document (total characters)
     */
    private Integer contentLength;

    /**
     * Incremental study duration for this session (seconds)
     */
    private Integer studySecondsIncrement;
}
