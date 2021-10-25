package com.tesco.pma.feedback.dao;

import com.tesco.pma.feedback.api.Feedback;
import com.tesco.pma.pagination.RequestQuery;
import org.apache.ibatis.annotations.Param;

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
     * @param requestQuery filtering, sorting and pagination
     */
    List<Feedback> findAll(@Param("requestQuery") RequestQuery requestQuery);


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
     * @param id a Feedback identifier
     * @return number of updated entities
     */
    int markAsRead(@Param("id") Long id);

    /**
     * Update feedback.
     *
     * @param feedback a Feedback
     * @return number of updated entities
     */
    int update(@Param("feedback") Feedback feedback);
}
