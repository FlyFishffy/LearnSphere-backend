package com.flyfish.learnsphere.service.impl;

import com.flyfish.learnsphere.exception.BusinessException;
import com.flyfish.learnsphere.model.enums.ErrorCode;
import com.flyfish.learnsphere.model.vo.ChunkVO;
import com.flyfish.learnsphere.model.vo.KnowledgeIndexStatusVO;
import com.flyfish.learnsphere.service.RagService;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RAG Service Implementation — PostgreSQL + pgvector
 * Supports heading-aware Markdown splitting, document file indexing,
 * chunk preview/edit/delete, and manual chunk management.
 *
 * @Author: FlyFish
 * @CreateTime: 2026/1/20
 */
@Slf4j
@Service
public class RagServiceImpl implements RagService {

    private static final int CHUNK_SIZE = 600;
    private static final int CHUNK_OVERLAP = 80;
    private static final int TOP_K = 5;

    /**
     * Regex to match Markdown headings (# to ######)
     */
    private static final Pattern HEADING_PATTERN = Pattern.compile("^(#{1,6})\\s+(.+)$");

    private final EmbeddingModel embeddingModel;
    private final JdbcTemplate vectorJdbcTemplate;

    public RagServiceImpl(EmbeddingModel embeddingModel,
                          @Qualifier("vectorJdbcTemplate") JdbcTemplate vectorJdbcTemplate) {
        this.embeddingModel = embeddingModel;
        this.vectorJdbcTemplate = vectorJdbcTemplate;
    }

    // ===================== Indexing =====================

    @Override
    @Transactional
    public void indexCourseContent(Long courseId, String contentMd) {
        if (courseId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "courseId is required");
        }
        if (contentMd == null || contentMd.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "contentMd is empty");
        }
        List<ChunkWithHeading> chunks = splitMarkdownWithHeadings(contentMd);
        if (chunks.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "contentMd is empty after splitting");
        }
        insertChunks(courseId, chunks, "markdown");
    }

    @Override
    @Transactional
    public void indexDocumentContent(Long courseId, String text, String source) {
        if (courseId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "courseId is required");
        }
        if (text == null || text.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Document text is empty");
        }
        List<ChunkWithHeading> chunks = splitPlainText(text);
        if (chunks.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Document text is empty after splitting");
        }
        insertChunks(courseId, chunks, source);
    }

    /**
     * Common method to delete old chunks and insert new ones for a course
     */
    private void insertChunks(Long courseId, List<ChunkWithHeading> chunks, String source) {
        // Delete old chunks with the same source (allows multiple sources to coexist)
        vectorJdbcTemplate.update(
                "DELETE FROM course_chunk_embedding WHERE course_id = ? AND (source = ? OR source IS NULL)",
                courseId, source);

        for (int i = 0; i < chunks.size(); i++) {
            ChunkWithHeading chunk = chunks.get(i);
            float[] vector = embeddingModel.embed(chunk.text).content().vector();
            String pgVector = toPgVectorLiteral(vector);
            vectorJdbcTemplate.update(
                    "INSERT INTO course_chunk_embedding(course_id, chunk_index, text, heading, source, embedding, create_time) "
                            + "VALUES (?, ?, ?, ?, ?, ?::vector, NOW())",
                    courseId, i, chunk.text, chunk.heading, source, pgVector
            );
        }
        log.info("Indexed {} chunks for courseId={} source={}", chunks.size(), courseId, source);
    }

    // ===================== Retrieval =====================

    @Override
    public List<String> retrieveRelevantChunks(Long courseId, String question) {
        if (courseId == null || question == null || question.trim().isEmpty()) {
            return List.of();
        }

        Integer count = vectorJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM course_chunk_embedding WHERE course_id = ?",
                Integer.class, courseId);
        if (count == null || count == 0) {
            log.warn("No embedding index found for courseId={}, falling back to no context", courseId);
            return List.of();
        }

        float[] queryVector = embeddingModel.embed(question).content().vector();
        String pgVector = toPgVectorLiteral(queryVector);

        List<String> results = vectorJdbcTemplate.queryForList(
                "SELECT text FROM course_chunk_embedding "
                        + "WHERE course_id = ? "
                        + "ORDER BY embedding <=> ?::vector "
                        + "LIMIT ?",
                String.class,
                courseId, pgVector, TOP_K
        );
        log.info("Retrieved {} relevant chunks for courseId={}", results.size(), courseId);
        return results;
    }

    // ===================== Delete Index =====================

    @Override
    public void deleteCourseIndex(Long courseId) {
        if (courseId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "courseId is required");
        }
        int deleted = vectorJdbcTemplate.update(
                "DELETE FROM course_chunk_embedding WHERE course_id = ?", courseId);
        log.info("Deleted {} chunks for courseId={}", deleted, courseId);
    }

    // ===================== Chunk Management =====================

    @Override
    public List<ChunkVO> listChunks(Long courseId) {
        if (courseId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "courseId is required");
        }
        return vectorJdbcTemplate.query(
                "SELECT id, course_id, chunk_index, text, heading, source, create_time "
                        + "FROM course_chunk_embedding WHERE course_id = ? ORDER BY chunk_index ASC",
                (rs, rowNum) -> {
                    ChunkVO vo = new ChunkVO();
                    vo.setId(rs.getLong("id"));
                    vo.setCourseId(rs.getLong("course_id"));
                    vo.setChunkIndex(rs.getInt("chunk_index"));
                    vo.setText(rs.getString("text"));
                    vo.setHeading(rs.getString("heading"));
                    vo.setSource(rs.getString("source"));
                    vo.setCreateTime(rs.getTimestamp("create_time") != null
                            ? rs.getTimestamp("create_time").toLocalDateTime() : null);
                    return vo;
                },
                courseId
        );
    }

    @Override
    public KnowledgeIndexStatusVO getIndexStatus(Long courseId) {
        if (courseId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "courseId is required");
        }
        KnowledgeIndexStatusVO status = new KnowledgeIndexStatusVO();
        status.setCourseId(courseId);

        Integer count = vectorJdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM course_chunk_embedding WHERE course_id = ?",
                Integer.class, courseId);
        status.setChunkCount(count == null ? 0 : count);
        status.setIndexed(count != null && count > 0);

        if (Boolean.TRUE.equals(status.getIndexed())) {
            try {
                String lastTime = vectorJdbcTemplate.queryForObject(
                        "SELECT MAX(create_time) FROM course_chunk_embedding WHERE course_id = ?",
                        String.class, courseId);
                status.setLastIndexTime(lastTime);
            } catch (Exception e) {
                status.setLastIndexTime(null);
            }
        }
        return status;
    }

    @Override
    @Transactional
    public ChunkVO addManualChunk(Long courseId, String text, String heading) {
        if (courseId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "courseId is required");
        }
        if (text == null || text.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "text is required");
        }

        // Determine next chunk_index
        Integer maxIndex = vectorJdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(chunk_index), -1) FROM course_chunk_embedding WHERE course_id = ?",
                Integer.class, courseId);
        int nextIndex = (maxIndex == null ? 0 : maxIndex + 1);

        float[] vector = embeddingModel.embed(text).content().vector();
        String pgVector = toPgVectorLiteral(vector);

        vectorJdbcTemplate.update(
                "INSERT INTO course_chunk_embedding(course_id, chunk_index, text, heading, source, embedding, create_time) "
                        + "VALUES (?, ?, ?, ?, 'manual', ?::vector, NOW())",
                courseId, nextIndex, text.trim(), heading, pgVector
        );

        log.info("Added manual chunk for courseId={}, index={}", courseId, nextIndex);

        // Return the newly created chunk
        List<ChunkVO> chunks = vectorJdbcTemplate.query(
                "SELECT id, course_id, chunk_index, text, heading, source, create_time "
                        + "FROM course_chunk_embedding WHERE course_id = ? AND chunk_index = ? ORDER BY id DESC LIMIT 1",
                (rs, rowNum) -> {
                    ChunkVO vo = new ChunkVO();
                    vo.setId(rs.getLong("id"));
                    vo.setCourseId(rs.getLong("course_id"));
                    vo.setChunkIndex(rs.getInt("chunk_index"));
                    vo.setText(rs.getString("text"));
                    vo.setHeading(rs.getString("heading"));
                    vo.setSource(rs.getString("source"));
                    vo.setCreateTime(rs.getTimestamp("create_time") != null
                            ? rs.getTimestamp("create_time").toLocalDateTime() : null);
                    return vo;
                },
                courseId, nextIndex
        );
        return chunks.isEmpty() ? null : chunks.get(0);
    }

    @Override
    @Transactional
    public ChunkVO updateChunk(Long chunkId, String text, String heading) {
        if (chunkId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "chunkId is required");
        }
        if (text == null || text.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "text is required");
        }

        // Re-compute embedding
        float[] vector = embeddingModel.embed(text.trim()).content().vector();
        String pgVector = toPgVectorLiteral(vector);

        int updated = vectorJdbcTemplate.update(
                "UPDATE course_chunk_embedding SET text = ?, heading = ?, embedding = ?::vector WHERE id = ?",
                text.trim(), heading, pgVector, chunkId
        );
        if (updated == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "Chunk not found");
        }

        log.info("Updated chunk id={}", chunkId);

        List<ChunkVO> chunks = vectorJdbcTemplate.query(
                "SELECT id, course_id, chunk_index, text, heading, source, create_time "
                        + "FROM course_chunk_embedding WHERE id = ?",
                (rs, rowNum) -> {
                    ChunkVO vo = new ChunkVO();
                    vo.setId(rs.getLong("id"));
                    vo.setCourseId(rs.getLong("course_id"));
                    vo.setChunkIndex(rs.getInt("chunk_index"));
                    vo.setText(rs.getString("text"));
                    vo.setHeading(rs.getString("heading"));
                    vo.setSource(rs.getString("source"));
                    vo.setCreateTime(rs.getTimestamp("create_time") != null
                            ? rs.getTimestamp("create_time").toLocalDateTime() : null);
                    return vo;
                },
                chunkId
        );
        return chunks.isEmpty() ? null : chunks.get(0);
    }

    @Override
    public void deleteChunk(Long chunkId) {
        if (chunkId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "chunkId is required");
        }
        int deleted = vectorJdbcTemplate.update(
                "DELETE FROM course_chunk_embedding WHERE id = ?", chunkId);
        if (deleted == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "Chunk not found");
        }
        log.info("Deleted chunk id={}", chunkId);
    }

    // ===================== Splitting Strategies =====================

    /**
     * Heading-aware Markdown splitting.
     * Tracks current heading hierarchy (H1 > H2 > H3...) and attaches it to each chunk.
     */
    private List<ChunkWithHeading> splitMarkdownWithHeadings(String content) {
        String normalized = content.replace("\r\n", "\n").trim();
        if (normalized.isEmpty()) {
            return List.of();
        }

        String[] lines = normalized.split("\n");
        List<ChunkWithHeading> chunks = new ArrayList<>();

        // Track heading hierarchy: headings[0] = H1, headings[1] = H2, etc.
        String[] headings = new String[6];
        StringBuilder currentChunk = new StringBuilder();
        String currentHeadingPath = "";

        for (String line : lines) {
            Matcher matcher = HEADING_PATTERN.matcher(line.trim());
            if (matcher.matches()) {
                // Flush current chunk before starting a new section
                if (currentChunk.length() > 0) {
                    chunks.add(new ChunkWithHeading(currentChunk.toString().trim(), currentHeadingPath));
                    currentChunk = new StringBuilder();
                }

                int level = matcher.group(1).length(); // 1-6
                String headingText = matcher.group(2).trim();
                headings[level - 1] = headingText;
                // Clear sub-headings
                for (int i = level; i < 6; i++) {
                    headings[i] = null;
                }
                // Build heading path: "H1 > H2 > H3"
                currentHeadingPath = buildHeadingPath(headings);
                // Include heading line in the chunk
                currentChunk.append(line).append("\n");
                continue;
            }

            // Check if adding this line exceeds chunk size
            if (currentChunk.length() + line.length() + 1 > CHUNK_SIZE && currentChunk.length() > 0) {
                chunks.add(new ChunkWithHeading(currentChunk.toString().trim(), currentHeadingPath));
                // Overlap: carry last CHUNK_OVERLAP chars
                String overlap = currentChunk.substring(Math.max(0, currentChunk.length() - CHUNK_OVERLAP));
                currentChunk = new StringBuilder(overlap);
            }
            currentChunk.append(line).append("\n");
        }

        // Don't forget the last chunk
        if (currentChunk.length() > 0 && !currentChunk.toString().trim().isEmpty()) {
            chunks.add(new ChunkWithHeading(currentChunk.toString().trim(), currentHeadingPath));
        }

        return chunks;
    }

    /**
     * Plain text splitting (for uploaded PDF/DOCX/TXT documents).
     * Uses paragraph-based sliding window without heading awareness.
     */
    private List<ChunkWithHeading> splitPlainText(String content) {
        String normalized = content.replace("\r\n", "\n").trim();
        if (normalized.isEmpty()) {
            return List.of();
        }
        String[] paragraphs = normalized.split("\n\\s*\n");
        List<ChunkWithHeading> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String paragraph : paragraphs) {
            String p = paragraph.trim();
            if (p.isEmpty()) continue;
            if (current.length() + p.length() + 1 > CHUNK_SIZE) {
                if (current.length() > 0) {
                    chunks.add(new ChunkWithHeading(current.toString(), null));
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
            chunks.add(new ChunkWithHeading(current.toString(), null));
        }
        return chunks;
    }

    /**
     * Build heading path string like "Chapter 1 > Section 1.1 > Subsection"
     */
    private String buildHeadingPath(String[] headings) {
        StringBuilder sb = new StringBuilder();
        for (String h : headings) {
            if (h != null) {
                if (sb.length() > 0) sb.append(" > ");
                sb.append(h);
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    /**
     * Convert float[] to pgvector literal format: [0.1,0.2,0.3]
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
     * Internal DTO to hold chunk text with its heading context
     */
    private static class ChunkWithHeading {
        final String text;
        final String heading;

        ChunkWithHeading(String text, String heading) {
            this.text = text;
            this.heading = heading;
        }
    }
}
