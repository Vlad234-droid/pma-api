package com.tesco.pma.review.dao;

import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.review.domain.ColleagueTimeline;
import com.tesco.pma.review.domain.Review;
import com.tesco.pma.review.domain.ReviewStats;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Interface to perform database operation on reviews
 */
public interface ReviewDAO {

    /**
     * Creates a review
     *
     * @param review a Review
     * @return number of created reviews
     */
    int create(@Param("review") Review review);

    /**
     * Returns a review by an identifier of review
     *
     * @param uuid an identifier of review
     * @return a Review
     */
    Review read(@Param("uuid") UUID uuid);

    /**
     * Update a review
     *
     * @param review          a Review
     * @param allowedStatuses allowed statuses for updating review
     * @return number of updated reviews
     */
    int update(@Param("review") Review review,
               @Param("allowedStatuses") Collection<PMTimelinePointStatus> allowedStatuses);

    /**
     * Delete a review
     *
     * @param uuid            an identifier of review
     * @param allowedStatuses allowed statuses for deleting review
     * @return number of deleted reviews
     */
    int delete(@Param("uuid") UUID uuid,
               @Param("allowedStatuses") Collection<PMTimelinePointStatus> allowedStatuses);

    /**
     * Returns reviews by parameters.
     * if some parameter is null it will be ignored
     *
     * @param tlPointUuid an identifier of timeline point
     * @param type        a review type
     * @param status      a review status
     * @param number      a sequence number of review
     * @return a list of reviews
     */
    List<Review> getByParams(@Param("tlPointUuid") UUID tlPointUuid,
                             @Param("type") PMReviewType type,
                             @Param("status") PMTimelinePointStatus status,
                             @Param("number") Integer number);

    /**
     * Get reviews by performanceCycleUuid, colleagueUuid
     *
     * @param cycleUuid     an identifier of performance cycle
     * @param colleagueUuid an identifier of colleague
     * @return a list of reviews
     */
    List<Review> getReviewsByColleague(@Param("cycleUuid") UUID cycleUuid,
                                       @Param("colleagueUuid") UUID colleagueUuid);

    /**
     * Returns list of colleagues reviews by managerUuid
     * if some parameter is null it will be ignored
     *
     * @param managerUuid an identifier of colleague
     * @return a list of colleagues reviews with active reviews
     */
    List<ColleagueTimeline> getTeamReviews(@Param("managerUuid") UUID managerUuid);

    /**
     * Updates a review status
     * if some parameter is null it will be ignored
     *
     * @param tlPointUuid  an identifier of timeline point
     * @param type         a review type
     * @param number       a sequence number of review
     * @param newStatus    a new review status
     * @param prevStatuses previous review statuses
     * @return number of updated review statuses
     */
    int updateStatusByParams(@Param("tlPointUuid") UUID tlPointUuid,
                             @Param("type") PMReviewType type,
                             @Param("number") Integer number,
                             @Param("newStatus") PMTimelinePointStatus newStatus,
                             @Param("prevStatuses") Collection<PMTimelinePointStatus> prevStatuses);

    /**
     * Delete reviews by parameters
     * if some parameter is null it will be ignored
     *
     * @param tlPointUuid     an identifier of timeline point
     * @param type            a review type
     * @param status          a review status
     * @param number          a sequence number of review
     * @param allowedStatuses allowed statuses for deleting review
     * @return number of deleted reviews
     */
    int deleteByParams(@Param("tlPointUuid") UUID tlPointUuid,
                       @Param("type") PMReviewType type,
                       @Param("status") PMTimelinePointStatus status,
                       @Param("number") Integer number,
                       @Param("allowedStatuses") Collection<PMTimelinePointStatus> allowedStatuses);

    /**
     * Re-numerate reviews with number >= startNumber using the following formula:
     * number=number-1
     *
     * @param tlPointUuid an identifier of timeline point
     * @param type        a review type
     * @param startNumber a start sequence number of review
     * @return number of updated reviews
     */
    int renumerateReviews(@Param("tlPointUuid") UUID tlPointUuid,
                          @Param("type") PMReviewType type,
                          @Param("startNumber") Integer startNumber);

    /**
     * Returns review stats by tlPointUuid and review type
     *
     * @param tlPointUuid an identifier of timeline point
     * @param type        a review type
     * @return a PMCycleReviewTypeProperties
     */
    ReviewStats getReviewStats(@Param("tlPointUuid") UUID tlPointUuid,
                               @Param("type") PMReviewType type);

}
