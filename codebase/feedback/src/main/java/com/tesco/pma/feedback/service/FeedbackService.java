package com.tesco.pma.feedback.service;

import com.tesco.pma.feedback.api.Feedback;
import com.tesco.pma.feedback.api.FeedbackItem;
import com.tesco.pma.pagination.RequestQuery;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/**
 * Service Interface for managing {@link Feedback} with {@link FeedbackItem}.
 */
public interface FeedbackService {

    /**
     * Save a feedback.
     *
     * @param feedback the entity to save.
     * @return the persisted entity.
     */
    Feedback create(Feedback feedback);

    /**
     * Partially updates a feedback.
     *
     * @param uuid the uuid of the entity.
     */
    void markAsRead(UUID uuid);

    /**
     * Get all the feedbacks.
     *
     * @param requestQuery filter, sort, offset
     * @return the list of entities.
     */
    List<Feedback> findAll(RequestQuery requestQuery);

    /**
     * Get the "uuid" feedback.
     *
     * @param uuid the uuid of the entity.
     * @return the entity.
     */
    Feedback findOne(UUID uuid);

    /**
     * Get given feedbacks count
     * @param colleagueUuid an identifier of colleague
     * @return requested feedbacks count
     */
    int getGivenFeedbackCount(@NotNull UUID colleagueUuid);

    /**
     * Get requested feedbacks count
     * @param colleagueUuid an identifier of colleague
     * @return requested feedbacks count
     */
    int getRequestedFeedbackCount(@NotNull UUID colleagueUuid);

    /**
     * Update feedback.
     *
     * @param feedback the entity to update.
     * @return the persisted entity.
     */
    Feedback update(Feedback feedback);

    /**
     * Save a feedbackItem.
     *
     * @param feedbackItem the entity to save.
     * @return the persisted entity.
     */
    FeedbackItem save(FeedbackItem feedbackItem);

}
