package com.tesco.pma.feedback.dao;

import com.tesco.pma.feedback.api.Feedback;
import com.tesco.pma.feedback.api.FeedbackStatus;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface FeedbackDAO {

    /**
     * Get by id
     *
     * @param id - unique identifier
     * @return feedback
     */
    Feedback getById(@Param("id") Long id);

    /**
     * Find all
     *
     * @return a list of Feedbacks
     */
    List<Feedback> findAll();


    /**
     * Insert feedback without items
     *
     * @param feedback a Feedback
     * @return number of inserted entities
     */
    int insert(@Param("feedback") Feedback feedback);

    /**
     * Mark feedback as read.
     *
     * @param feedbackId a Feedback identifier
     * @return number of updated entities
     */
    int markAsRead(@Param("feedbackId") Long feedbackId);

    /**
     * Update feedback status.
     *
     * @param feedbackId a Feedback identifier
     * @param status a Feedback status
     * @return number of updated entities
     */
    int updateStatus(@Param("feedbackId") Long feedbackId, @Param("status") FeedbackStatus status);

}
