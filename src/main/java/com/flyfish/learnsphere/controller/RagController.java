package com.flyfish.learnsphere.controller;

import com.flyfish.learnsphere.annotaion.AuthCheck;
import com.flyfish.learnsphere.exception.BusinessException;
import com.flyfish.learnsphere.model.dto.AddChunkRequest;
import com.flyfish.learnsphere.model.dto.UpdateChunkRequest;
import com.flyfish.learnsphere.model.entity.Course;
import com.flyfish.learnsphere.model.entity.Result;
import com.flyfish.learnsphere.model.enums.ErrorCode;
import com.flyfish.learnsphere.model.vo.ChunkVO;
import com.flyfish.learnsphere.model.vo.KnowledgeIndexStatusVO;
import com.flyfish.learnsphere.service.CourseService;
import com.flyfish.learnsphere.service.RagService;
import com.flyfish.learnsphere.utils.DocumentParser;
import com.flyfish.learnsphere.utils.ResultUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Knowledge Base Management Controller
 * Provides APIs for building/deleting course indexes, uploading documents,
 * previewing/editing/deleting chunks, and querying index status.
 *
 * @Author: FlyFish
 * @CreateTime: 2026/02/14
 */
@RestController
@RequestMapping("/rag")
@Slf4j
public class RagController {

    @Resource
    private RagService ragService;

    @Resource
    private CourseService courseService;

    // ===================== Index Build / Delete =====================

    /**
     * Rebuild course knowledge index from course markdown content
     */
    @PostMapping("/index/{courseId}")
    @AuthCheck(value = {"Teacher", "Admin"})
    public Result<Boolean> buildCourseIndex(@PathVariable Long courseId) {
        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "Course not found.");
        }
        ragService.indexCourseContent(courseId, course.getContentMd());
        return ResultUtils.success(true);
    }

    /**
     * Upload document file (PDF/DOCX/TXT/MD) and index into knowledge base
     */
    @PostMapping("/upload/{courseId}")
    @AuthCheck(value = {"Teacher", "Admin"})
    public Result<Boolean> uploadDocument(@PathVariable Long courseId,
                                          @RequestParam("file") MultipartFile file) {
        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "Course not found.");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "File is empty.");
        }
        try {
            String text = DocumentParser.parse(file);
            String source = DocumentParser.detectSourceType(file);
            ragService.indexDocumentContent(courseId, text, source);
            return ResultUtils.success(true);
        } catch (Exception e) {
            log.error("Failed to parse uploaded document for courseId={}: {}", courseId, e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Failed to parse document: " + e.getMessage());
        }
    }

    /**
     * Delete entire course knowledge index
     */
    @DeleteMapping("/index/{courseId}")
    @AuthCheck(value = {"Teacher", "Admin"})
    public Result<Boolean> deleteCourseIndex(@PathVariable Long courseId) {
        ragService.deleteCourseIndex(courseId);
        return ResultUtils.success(true);
    }

    // ===================== Index Status =====================

    /**
     * Get knowledge index status for a course
     */
    @GetMapping("/status/{courseId}")
    @AuthCheck(value = {"Teacher", "Admin"})
    public Result<KnowledgeIndexStatusVO> getCourseIndexStatus(@PathVariable Long courseId) {
        Course course = courseService.getCourseById(courseId);
        KnowledgeIndexStatusVO status = ragService.getIndexStatus(courseId);
        status.setCourseTitle(course.getTitle());
        return ResultUtils.success(status);
    }

    // ===================== Chunk Preview / Management =====================

    /**
     * List all chunks for a course (for preview)
     */
    @GetMapping("/chunks/{courseId}")
    @AuthCheck(value = {"Teacher", "Admin"})
    public Result<List<ChunkVO>> listChunks(@PathVariable Long courseId) {
        List<ChunkVO> chunks = ragService.listChunks(courseId);
        return ResultUtils.success(chunks);
    }

    /**
     * Add a manual knowledge chunk
     */
    @PostMapping("/chunk")
    @AuthCheck(value = {"Teacher", "Admin"})
    public Result<ChunkVO> addChunk(@RequestBody AddChunkRequest req) {
        if (req.getCourseId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "courseId is required");
        }
        if (req.getText() == null || req.getText().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "text is required");
        }
        ChunkVO chunk = ragService.addManualChunk(req.getCourseId(), req.getText(), req.getHeading());
        return ResultUtils.success(chunk);
    }

    /**
     * Update a knowledge chunk (text and heading)
     */
    @PutMapping("/chunk")
    @AuthCheck(value = {"Teacher", "Admin"})
    public Result<ChunkVO> updateChunk(@RequestBody UpdateChunkRequest req) {
        if (req.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "id is required");
        }
        if (req.getText() == null || req.getText().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "text is required");
        }
        ChunkVO chunk = ragService.updateChunk(req.getId(), req.getText(), req.getHeading());
        return ResultUtils.success(chunk);
    }

    /**
     * Delete a single knowledge chunk
     */
    @DeleteMapping("/chunk/{chunkId}")
    @AuthCheck(value = {"Teacher", "Admin"})
    public Result<Boolean> deleteChunk(@PathVariable Long chunkId) {
        ragService.deleteChunk(chunkId);
        return ResultUtils.success(true);
    }
}
