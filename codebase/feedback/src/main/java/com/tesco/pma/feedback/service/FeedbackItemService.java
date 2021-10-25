package com.tesco.pma.feedback.service;

import com.tesco.pma.feedback.api.FeedbackItem;

/**
 * Service Interface for managing {@link com.tesco.pma.feedback.api.FeedbackItem}.
 */
public interface FeedbackItemService {
    /**
     * Save a feedbackItem.
     *
     * @param feedbackItem the entity to save.
     * @return the persisted entity.
     */
    FeedbackItem create(FeedbackItem feedbackItem);

    /**
     * Update a feedbackItem.
     *
     * @param feedbackItem the entity to save.
     * @return the persisted entity.
     */
    FeedbackItem update(FeedbackItem feedbackItem);
}
