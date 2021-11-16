package com.tesco.pma.feedback.service;

import com.tesco.pma.feedback.api.Feedback;
import com.tesco.pma.feedback.api.FeedbackItem;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.validation.ValidationGroup;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
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
    @Validated({ValidationGroup.OnCreate.class, Default.class})
    Feedback create(@NotNull @Valid Feedback feedback);

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
     * Update feedback.
     *
     * @param feedback the entity to update.
     * @return the persisted entity.
     */
    @Validated({ValidationGroup.OnUpdate.class, Default.class})
    Feedback update(@NotNull @Valid Feedback feedback);

    /**
     * Save a feedbackItem.
     *
     * @param feedbackItem the entity to save.
     * @return the persisted entity.
     */
    FeedbackItem save(FeedbackItem feedbackItem);

}
