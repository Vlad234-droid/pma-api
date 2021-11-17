package com.tesco.pma.review.service;

import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.review.domain.ColleagueTimeline;
import com.tesco.pma.review.domain.GroupObjective;
import com.tesco.pma.review.domain.PMCycleTimelinePoint;
import com.tesco.pma.review.domain.Review;
import com.tesco.pma.cycle.api.PMReviewStatus;
import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.review.domain.WorkingGroupObjective;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/**
 * Review service
 */
public interface ReviewService {

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
                     @NotNull PMReviewType type,
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
                            @NotNull PMReviewType type);

    /**
     * Finds list of colleagues reviews by managerUuid
     *
     * @param managerUuid an identifier of colleague
     * @return a list of colleagues reviews
     */
    List<ColleagueTimeline> getTeamReviews(@NotNull UUID managerUuid);

    /**
     * Creates review.
     *
     * @param review a review
     * @return created review.
     * @throws DatabaseConstraintViolationException review already exist.
     */
    Review createReview(Review review);

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
     * @param performanceCycleUuid an identifier of performance cycle
     * @param colleagueUuid        an identifier of colleague
     * @param type                 a review type
     * @param reviews              list of reviews.
     * @return created/updated reviews.
     * @throws DatabaseConstraintViolationException review already exist.
     */
    List<Review> updateReviews(@NotNull UUID performanceCycleUuid,
                               @NotNull UUID colleagueUuid,
                               @NotNull PMReviewType type,
                               List<Review> reviews);

    /**
     * Updates reviews status.
     *
     * @param performanceCycleUuid an identifier of performance cycle
     * @param colleagueUuid        an identifier of colleague
     * @param type                 a review type
     * @param reviews              list of review
     * @param status               a new review status
     * @param reason               a reason of changing status
     * @param loggedUserName       a logged user
     * @return a ObjectiveStatus
     * @throws NotFoundException if review doesn't exist.
     */
    PMReviewStatus updateReviewsStatus(@NotNull UUID performanceCycleUuid,
                                       @NotNull UUID colleagueUuid,
                                       @NotNull PMReviewType type,
                                       List<Review> reviews,
                                       @NotNull PMReviewStatus status,
                                       @Size(max = 250) String reason,
                                       @NotNull String loggedUserName);

    /**
     * Deletes review by business key.
     *
     * @param performanceCycleUuid an identifier of performance cycle
     * @param colleagueUuid        an identifier of colleague
     * @param type                 a review type
     * @param number               a sequence number of review
     * @throws NotFoundException if review doesn't exist.
     */
    void deleteReview(@NotNull UUID performanceCycleUuid,
                      @NotNull UUID colleagueUuid,
                      @NotNull PMReviewType type,
                      @NotNull Integer number);

    /**
     * Create group's objectives
     *
     * @param groupObjectives a list of group's objectives
     * @return Created group's objectives
     * @throws DatabaseConstraintViolationException group objective already exist.
     */
    List<GroupObjective> createGroupObjectives(List<GroupObjective> groupObjectives,
                                               @NotNull String loggedUserName);

    /**
     * Get all group's objectives
     *
     * @return a list of all group's objectives
     */
    List<GroupObjective> getAllGroupObjectives();

    /**
     * Publish the last version of group objectives
     *
     * @param loggedUserName a logged user
     * @return a working group objective
     */
    List<GroupObjective> publishGroupObjectives(@NotNull String loggedUserName);

    /**
     * Get published group's objectives
     *
     * @return a list of published group's objectives
     */
    List<GroupObjective> getPublishedGroupObjectives();

    /**
     * Finds timeline by colleagueUuid
     *
     * @param colleagueUuid an identifier of colleague
     * @return a list of timeline points
     * @throws NotFoundException if timeline doesn't exist.
     */
    List<PMCycleTimelinePoint> getCycleTimelineByColleague(UUID colleagueUuid);
}
