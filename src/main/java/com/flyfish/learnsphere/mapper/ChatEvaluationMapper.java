package com.flyfish.learnsphere.mapper;

import com.flyfish.learnsphere.model.entity.ChatEvaluation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Chat Evaluation Mapper
 *
 * @Author: FlyFish
 */
@Mapper
public interface ChatEvaluationMapper {

    int insert(ChatEvaluation evaluation);

    int update(ChatEvaluation evaluation);

    ChatEvaluation getById(@Param("id") Long id);

    /**
     * Find evaluation by user + session + question (to check if already evaluated)
     */
    ChatEvaluation getByUserSessionQuestion(@Param("userId") Long userId,
                                            @Param("sessionId") String sessionId,
                                            @Param("question") String question);

    List<ChatEvaluation> listByUser(@Param("userId") Long userId);

    List<ChatEvaluation> listBySession(@Param("sessionId") String sessionId);

    List<ChatEvaluation> listByCourse(@Param("courseId") Long courseId);

    List<ChatEvaluation> listAll();

    int deleteById(@Param("id") Long id);

    /**
     * Count evaluations with thumbs=1
     */
    int countThumbsUp(@Param("courseId") Long courseId);

    /**
     * Count evaluations with thumbs=-1
     */
    int countThumbsDown(@Param("courseId") Long courseId);

    /**
     * Average star rating (only for records that have a rating)
     */
    Double averageRating(@Param("courseId") Long courseId);

    /**
     * Total evaluation count
     */
    int countAll(@Param("courseId") Long courseId);

    /**
     * Count evaluations that have a non-null rating
     */
    int countRated(@Param("courseId") Long courseId);
}
