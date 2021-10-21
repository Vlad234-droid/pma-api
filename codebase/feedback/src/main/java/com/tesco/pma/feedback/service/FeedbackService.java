package com.tesco.pma.feedback.service;

import com.tesco.pma.feedback.api.Feedback;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.tesco.pma.feedback.api.Feedback}.
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
     * @param feedbackId
     */
    void markAsRead(Long feedbackId);

    /**
     * Get all the feedbacks.
     *
     * @return the list of entities.
     */
    List<Feedback> findAll();

    /**
     * Get the "id" feedback.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Feedback> findOne(Long id);

}
