package com.tesco.pma.review.dao;

import com.tesco.pma.cycle.api.PMReviewStatus;
import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.review.domain.ColleagueTimeline;
import com.tesco.pma.review.domain.OrgObjective;
import com.tesco.pma.review.domain.PMCycleReviewTypeProperties;
import com.tesco.pma.review.domain.PMCycleTimelinePoint;
import com.tesco.pma.review.domain.Review;
import com.tesco.pma.review.domain.ReviewStats;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Interface to perform database operation on objective
 */
public interface ReviewDAO {

    /**
     * Returns an organisation objective
     *
     * @param orgObjectiveUuid an identifier
     * @return an OrgObjective
     */
    OrgObjective getOrgObjective(@Param("orgObjectiveUuid") UUID orgObjectiveUuid);

    /**
     * Returns a list of organisation objectives for max version
     *
     * @return a list of organisation objectives for max version
     */
    List<OrgObjective> getOrgObjectives();

    /**
     * Returns a list of published organisation objectives
     *
     * @return a list of organisation objectives
     */
    List<OrgObjective> getPublishedOrgObjectives();

    /**
     * Creates an organisation objective
     *
     * @param orgObjective an organisation objective
     * @return number of created organisation objectives
     */
    int createOrgObjective(@Param("orgObjective") OrgObjective orgObjective);

    /**
     * Delete an organisation objective
     *
     * @param orgObjectiveUuid an identifier
     * @return number of deleted organisation objectives
     */
    int deleteOrgObjective(@Param("orgObjectiveUuid") UUID orgObjectiveUuid);

    /**
     * Returns max version of organisation objectives
     *
     * @return max version of organisation objectives
     */
    int getMaxVersionOrgObjective();

    /**
     * Publish organisation objectives
     *
     * @return number of published organisation objectives
     */
    int publishOrgObjectives();

    /**
     * Un-publish organisation objectives
     *
     * @return number of un-published organisation objectives
     */
    int unpublishOrgObjectives();

    /**
     * Returns a review by performance cycle, colleague, review type and sequence number.
     *
     * @param performanceCycleUuid an identifier of performance cycle
     * @param colleagueUuid        an identifier of colleague
     * @param type                 a review type
     * @param number               a sequence number of review
     * @return a Review
     */
    Review getReview(@Param("performanceCycleUuid") UUID performanceCycleUuid,
                     @Param("colleagueUuid") UUID colleagueUuid,
                     @Param("type") PMReviewType type,
                     @Param("number") Integer number);

    /**
     * Returns a review by performance cycle, colleague and review type.
     *
     * @param performanceCycleUuid an identifier of performance cycle
     * @param colleagueUuid        an identifier of colleague
     * @param type                 a review type
     * @return a list of reviews
     */
    List<Review> getReviews(@Param("performanceCycleUuid") UUID performanceCycleUuid,
                            @Param("colleagueUuid") UUID colleagueUuid,
                            @Param("type") PMReviewType type);

    /**
     * Returns list of colleagues reviews by managerUuid
     *
     * @param managerUuid an identifier of colleague
     * @return a list of colleagues reviews with active reviews
     */
    List<ColleagueTimeline> getTeamReviews(@Param("managerUuid") UUID managerUuid);

    /**
     * Creates a review
     *
     * @param review a Review
     * @return number of created reviews
     */
    int createReview(@Param("review") Review review);

    /**
     * Update a review
     *
     * @param review a Review
     * @return number of updated reviews
     */
    int updateReview(@Param("review") Review review,
                     @Param("allowedReviewStatuses") Collection<PMReviewStatus> allowedReviewStatuses);

    /**
     * Updates a review status
     *
     * @param performanceCycleUuid an identifier of performance cycle
     * @param colleagueUuid        an identifier of colleague
     * @param type                 a review type
     * @param number               a sequence number of review
     * @param newStatus            a new review status
     * @param prevReviewStatuses   previous review statuses
     * @return number of updated review statuses
     */
    int updateReviewStatus(@Param("performanceCycleUuid") UUID performanceCycleUuid,
                           @Param("colleagueUuid") UUID colleagueUuid,
                           @Param("type") PMReviewType type,
                           @Param("number") Integer number,
                           @Param("newStatus") PMReviewStatus newStatus,
                           @Param("prevReviewStatuses") Collection<PMReviewStatus> prevReviewStatuses);

    /**
     * Delete a review by business key
     *
     * @param performanceCycleUuid an identifier of performance cycle
     * @param colleagueUuid        an identifier of colleague
     * @param type                 a review type
     * @param number               a sequence number of review
     * @return number of deleted reviews
     */
    int deleteReview(@Param("performanceCycleUuid") UUID performanceCycleUuid,
                     @Param("colleagueUuid") UUID colleagueUuid,
                     @Param("type") PMReviewType type,
                     @Param("number") Integer number,
                     @Param("allowedReviewStatuses") Collection<PMReviewStatus> allowedReviewStatuses);

    /**
     * Re-numerate reviews with number >= startNumber using the following formula:
     * number=number-1
     *
     * @param performanceCycleUuid an identifier of performance cycle
     * @param colleagueUuid        an identifier of colleague
     * @param type                 a review type
     * @param startNumber          a start sequence number of review
     * @return number of updated reviews
     */
    int renumerateReviews(@Param("performanceCycleUuid") UUID performanceCycleUuid,
                          @Param("colleagueUuid") UUID colleagueUuid,
                          @Param("type") PMReviewType type,
                          @Param("startNumber") Integer startNumber);

    /**
     * Returns properties of review type by PM cycleUuid and review type
     *
     * @param cycleUuid an identifier of performance cycle
     * @param type      a review type
     * @return a PMCycleReviewTypeProperties
     */
    PMCycleReviewTypeProperties getPMCycleReviewTypeProperties(@Param("cycleUuid") UUID cycleUuid,
                                                               @Param("type") PMReviewType type);

    /**
     * Returns review stats by PM cycleUuid, colleagueUuid and review type
     *
     * @param cycleUuid     an identifier of performance cycle
     * @param colleagueUuid an identifier of colleague
     * @param type          a review type
     * @return a PMCycleReviewTypeProperties
     */
    ReviewStats getReviewStats(@Param("cycleUuid") UUID cycleUuid,
                               @Param("colleagueUuid") UUID colleagueUuid,
                               @Param("type") PMReviewType type);


    /**
     * Returns time line by PM cycleUuid
     *
     * @param cycleUuid an identifier of performance cycle
     * @return a PMCycleReviewTypeProperties
     */
    List<PMCycleTimelinePoint> getTimeline(@Param("cycleUuid") UUID cycleUuid);

}
