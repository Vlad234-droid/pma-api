package com.tesco.pma.feedback.dao;

import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.feedback.api.Feedback;
import com.tesco.pma.feedback.api.FeedbackItem;
import com.tesco.pma.feedback.api.FeedbackStatus;
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
     * @param requestQuery filtering, sorting and pagination
     * @return a list of Feedbacks
     */
    List<Feedback> findAll(@Param("requestQuery") RequestQuery requestQuery);

    /**
     * Get given feedbacks count
     * @param colleagueUuid an identifier of colleague
     * @return given feedbacks count
     */
    int getGivenFeedbackCount(@Param("colleagueUuid") UUID colleagueUuid);

    /**
     * Get requested feedbacks count
     * @param colleagueUuid an identifier of colleague
     * @return requested feedbacks count
     */
    int getRequestedFeedbackCount(@Param("colleagueUuid") UUID colleagueUuid);

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
     * @param statusFilter previous status filter
     * @return number of updated entities
     */
    int update(@Param("feedback") Feedback feedback, @Param("statusFilter") DictionaryFilter<FeedbackStatus> statusFilter);

    /**
     * Insert or update feedback item
     *
     * @param feedbackItem a FeedbackItem
     * @return number of inserted entities
     */
    int insertOrUpdateFeedbackItem(@Param("feedbackItem") FeedbackItem feedbackItem);

}
