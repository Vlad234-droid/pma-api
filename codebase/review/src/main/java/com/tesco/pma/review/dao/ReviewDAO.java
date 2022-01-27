package com.tesco.pma.review.dao;

import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.review.domain.ColleagueView;
import com.tesco.pma.review.domain.Review;
import com.tesco.pma.review.domain.ReviewStats;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static java.time.Instant.now;

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
    default int create(Review review) {
        return intCreate(review, now());
    }

    int intCreate(@Param("review") Review review,
                  @Param("lastUpdatedTime") Instant lastUpdatedTime);

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
    default int update(Review review,
                       Collection<PMTimelinePointStatus> allowedStatuses) {
        return intUpdate(review, allowedStatuses, now());
    }

    int intUpdate(@Param("review") Review review,
                  @Param("allowedStatuses") Collection<PMTimelinePointStatus> allowedStatuses,
                  @Param("lastUpdatedTime") Instant lastUpdatedTime);

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
     * Returns list of colleague's view with active reviews, timeline points etc. by managerUuid
     * if some parameter is null it will be ignored
     *
     * @param managerUuid an identifier of colleague
     * @param depth       a level of colleague's tree
     * @return a list of colleague's view with active reviews, timeline points etc.
     */
    List<ColleagueView> getTeamView(@Param("managerUuid") UUID managerUuid,
                                    @Param("depth") Integer depth);

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
    default int updateStatusByParams(UUID tlPointUuid,
                                     PMReviewType type,
                                     Integer number,
                                     PMTimelinePointStatus newStatus,
                                     Collection<PMTimelinePointStatus> prevStatuses) {
        return intUpdateStatusByParams(
                tlPointUuid,
                type,
                number,
                newStatus,
                prevStatuses,
                now());
    }

    int intUpdateStatusByParams(@Param("tlPointUuid") UUID tlPointUuid,
                                @Param("type") PMReviewType type,
                                @Param("number") Integer number,
                                @Param("newStatus") PMTimelinePointStatus newStatus,
                                @Param("prevStatuses") Collection<PMTimelinePointStatus> prevStatuses,
                                @Param("lastUpdatedTime") Instant lastUpdatedTime);

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
     * @return a PMCycleReviewTypeProperties
     */
    ReviewStats getReviewStats(@Param("tlPointUuid") UUID tlPointUuid);

}
