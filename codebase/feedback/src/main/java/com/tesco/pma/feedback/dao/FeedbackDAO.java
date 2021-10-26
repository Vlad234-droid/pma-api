package com.tesco.pma.feedback.dao;

import com.tesco.pma.feedback.api.Feedback;
import com.tesco.pma.pagination.RequestQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

public interface FeedbackDAO {

    /**
     * Get by uuid
     *
     * @param uuid - unique identifier
     * @return feedback
     */
    Feedback getByUuid(@Param("uuid") UUID uuid);

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
     * @param uuid a Feedback identifier
     * @return number of updated entities
     */
    int markAsRead(@Param("uuid") UUID uuid);

    /**
     * Update feedback.
     *
     * @param feedback a Feedback
     * @return number of updated entities
     */
    int update(@Param("feedback") Feedback feedback);
}
