package com.tesco.pma.review.service;

import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.review.domain.AuditOrgObjectiveReport;
import com.tesco.pma.review.domain.ColleagueView;
import com.tesco.pma.review.domain.OrgObjective;
import com.tesco.pma.review.domain.Review;
import com.tesco.pma.review.domain.TimelinePoint;

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
     * Returns a review by an identifier of review
     *
     * @param uuid an identifier of review
     * @return a Review
     */
    Review getReview(@NotNull UUID uuid);

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
                            @NotNull PMReviewType type,
                            @NotNull PMTimelinePointStatus status);

    /**
     * Finds reviews by performanceCycleUuid, colleagueUuid
     *
     * @param performanceCycleUuid an identifier of performance cycle
     * @param colleagueUuid        an identifier of colleague
     * @return a list of reviews
     * @throws NotFoundException if reviews don't exist.
     */
    List<Review> getReviewsByColleague(@NotNull UUID performanceCycleUuid,
                                       @NotNull UUID colleagueUuid);

    /**
     * Finds list of colleague's view with active reviews, timeline points etc. by managerUuid
     *
     * @param managerUuid an identifier of colleague
     * @param depth       a level of colleague's tree
     * @return a list of colleague's view with active reviews, timeline points etc.
     */
    List<ColleagueView> getTeamView(@NotNull UUID managerUuid,
                                    @NotNull Integer depth);

    /**
     * Creates review.
     *
     * @param review               a review
     * @param performanceCycleUuid an identifier of performance cycle
     * @param colleagueUuid        an identifier of colleague
     * @return created review.
     * @throws DatabaseConstraintViolationException review already exist.
     */
    Review createReview(@NotNull Review review, @NotNull UUID performanceCycleUuid, @NotNull UUID colleagueUuid);

    /**
     * Updates existing review.
     *
     * @param review               a review
     * @param performanceCycleUuid an identifier of performance cycle
     * @param colleagueUuid        an identifier of colleague
     * @return updated review.
     * @throws NotFoundException if review doesn't exist.
     */
    Review updateReview(@NotNull Review review, @NotNull UUID performanceCycleUuid, @NotNull UUID colleagueUuid);

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
     * @param loggedUserUuid       an identifier of logged user
     * @return a ObjectiveStatus
     * @throws NotFoundException if review doesn't exist.
     */
    PMTimelinePointStatus updateReviewsStatus(@NotNull UUID performanceCycleUuid,
                                              @NotNull UUID colleagueUuid,
                                              @NotNull PMReviewType type,
                                              List<Review> reviews,
                                              @NotNull PMTimelinePointStatus status,
                                              @Size(max = 250) String reason,
                                              @NotNull UUID loggedUserUuid);

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
     * Create organisation objectives
     *
     * @param orgObjectives  a list of organisation objectives
     * @param loggedUserUuid an identifier of logged user
     * @return Created organisation objectives
     * @throws DatabaseConstraintViolationException organisation objective already exist.
     */
    List<OrgObjective> createOrgObjectives(List<OrgObjective> orgObjectives,
                                           @NotNull UUID loggedUserUuid);

    /**
     * Get all organisation objectives
     *
     * @return a list of all organisation objectives
     * @throws NotFoundException if organisation objective doesn't exist.
     */
    List<OrgObjective> getAllOrgObjectives();

    /**
     * Publish the last version of organisation objectives
     *
     * @param loggedUserUuid an identifier of logged user
     * @return Published organisation objectives
     * @throws NotFoundException if organisation objective doesn't exist.
     */
    List<OrgObjective> publishOrgObjectives(@NotNull UUID loggedUserUuid);

    /**
     * Create and publish the last version of organisation objectives
     *
     * @param orgObjectives  a list of organisation objectives
     * @param loggedUserUuid an identifier of logged user
     * @return Published organisation objectives
     * @throws DatabaseConstraintViolationException organisation objective already exist.
     */
    List<OrgObjective> createAndPublishOrgObjectives(List<OrgObjective> orgObjectives,
                                                     @NotNull UUID loggedUserUuid);

    /**
     * Get published organisation objectives
     *
     * @return a list of published organisation objectives
     * @throws NotFoundException if published organisation objective doesn't exist.
     */
    List<OrgObjective> getPublishedOrgObjectives();

    /**
     * Finds timeline by colleagueUuid
     *
     * @param colleagueUuid an identifier of colleague
     * @return a list of timeline points
     * @throws NotFoundException if timeline doesn't exist.
     */
    List<TimelinePoint> getCycleTimelineByColleague(UUID colleagueUuid);

    /**
     * Get report of organisation objective actions
     *
     * @param requestQuery a request query
     * @return a list of AuditOrgObjectiveReport
     */
    List<AuditOrgObjectiveReport> getAuditOrgObjectiveReport(@NotNull RequestQuery requestQuery);
}
