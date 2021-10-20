package com.tesco.pma.objective.service;

import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.objective.domain.GroupObjective;
import com.tesco.pma.objective.domain.Review;
import com.tesco.pma.objective.domain.ReviewStatus;
import com.tesco.pma.objective.domain.ReviewType;
import com.tesco.pma.objective.domain.WorkingGroupObjective;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/**
 * Review service
 */
public interface ReviewService {

    /**
     * Finds review by uuid.
     *
     * @param reviewUuid an identifier
     * @return review
     * @throws NotFoundException if review doesn't exist.
     */
    Review getReviewByUuid(@NotNull UUID reviewUuid);

    /**
     * Finds review by performanceCycleUuid, colleagueUuid, review type and number.
     *
     * @param performanceCycleUuid an identifier of performance cycle
     * @param colleagueUuid        an identifier of colleague
     * @param type                 a review type
     * @param number               a sequence number of review
     * @return review
     * @throws NotFoundException if review doesn't exist.
     */
    Review getReview(@NotNull UUID performanceCycleUuid,
                     @NotNull UUID colleagueUuid,
                     @NotNull ReviewType type,
                     @NotNull Integer number);

    /**
     * Finds reviews by performanceCycleUuid, colleagueUuid and review type
     *
     * @param performanceCycleUuid an identifier of performance cycle
     * @param colleagueUuid        an identifier of colleague
     * @param type                 a review type
     * @return a list of reviews
     * @throws NotFoundException if reviews don't exist.
     */
    List<Review> getReviews(@NotNull UUID performanceCycleUuid,
                            @NotNull UUID colleagueUuid,
                            @NotNull ReviewType type);

    /**
     * Creates review.
     *
     * @param review a review
     * @return created review.
     * @throws DatabaseConstraintViolationException review already exist.
     */
    Review createReview(Review review);

    /**
     * Creates reviews.
     *
     * @param reviews list of review
     * @return created reviews.
     * @throws DatabaseConstraintViolationException review already exist.
     */
    List<Review> createReviews(List<Review> reviews);

    /**
     * Updates existing review.
     *
     * @param review a review
     * @return updated review.
     * @throws NotFoundException if review doesn't exist.
     */
    Review updateReview(Review review);

    /**
     * Create/update reviews.
     *
     * @param reviews list of reviews.
     * @return created/updated reviews.
     * @throws DatabaseConstraintViolationException review already exist.
     */
    List<Review> updateReviews(List<Review> reviews);

    /**
     * Updates review status.
     *
     * @param performanceCycleUuid an identifier of performance cycle
     * @param colleagueUuid        an identifier of colleague
     * @param type                 a review type
     * @param number               a sequence number of review
     * @param status               a new review status
     * @param reason               a reason of changing status
     * @param loggedUserName       a logged user
     * @return a ObjectiveStatus
     * @throws NotFoundException if review doesn't exist.
     */
    ReviewStatus updateReviewStatus(@NotNull UUID performanceCycleUuid,
                                    @NotNull UUID colleagueUuid,
                                    @NotNull ReviewType type,
                                    @NotNull Integer number,
                                    @NotNull ReviewStatus status,
                                    @Size(max = 250) String reason,
                                    @NotNull String loggedUserName);

    /**
     * Deletes review.
     *
     * @param reviewUuid an identifier.
     * @throws NotFoundException if review doesn't exist.
     */
    void deleteReview(@NotNull UUID reviewUuid);

    /**
     * Create group's objectives
     *
     * @param businessUnitUuid business unit an identifier, not null
     * @param groupObjectives  a list of group's objectives
     * @return Created group's objectives
     * @throws NotFoundException                    if business unit or performance cycle doesn't exist.
     * @throws DatabaseConstraintViolationException group objective already exist.
     */
    List<GroupObjective> createGroupObjectives(@NotNull UUID businessUnitUuid,
                                               List<GroupObjective> groupObjectives);

    /**
     * Get all group's objectives
     *
     * @param businessUnitUuid business unit an identifier, not null
     * @return a list of all group's objectives
     */
    List<GroupObjective> getAllGroupObjectives(@NotNull UUID businessUnitUuid);

    /**
     * Publish the last version of group objectives
     *
     * @param businessUnitUuid business unit an identifier, not null
     * @param loggedUserName   a logged user
     * @return a working group objective
     */
    WorkingGroupObjective publishGroupObjectives(@NotNull UUID businessUnitUuid,
                                                 @NotNull String loggedUserName);

    /**
     * Un-publish group objectives
     *
     * @param businessUnitUuid business unit an identifier, not null
     */
    void unpublishGroupObjectives(@NotNull UUID businessUnitUuid);

    /**
     * Get published group's objectives
     *
     * @param businessUnitUuid business unit an identifier, not null
     * @return a list of published group's objectives
     */
    List<GroupObjective> getPublishedGroupObjectives(@NotNull UUID businessUnitUuid);
}
