package com.tesco.pma.objective.dao;

import com.tesco.pma.objective.domain.GroupObjective;
import com.tesco.pma.objective.domain.Review;
import com.tesco.pma.objective.domain.ReviewStatus;
import com.tesco.pma.objective.domain.ReviewType;
import com.tesco.pma.objective.domain.WorkingGroupObjective;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Interface to perform database operation on objective
 */
public interface ReviewDAO {

    /**
     * Returns a group objective
     *
     * @param groupObjectiveUuid an identifier
     * @return a GroupObjective
     */
    GroupObjective getGroupObjective(@Param("groupObjectiveUuid") UUID groupObjectiveUuid);

    /**
     * Returns a list of group objectives for max version
     *
     * @param businessUnitUuid an identifier of business unit
     * @return a list of Group Objectives for max version
     */
    List<GroupObjective> getGroupObjectivesByBusinessUnitUuid(@Param("businessUnitUuid") UUID businessUnitUuid);

    /**
     * Returns a list of working group objectives
     *
     * @param businessUnitUuid an identifier of business unit
     * @return a list of Group Objectives
     */
    List<GroupObjective> getWorkingGroupObjectivesByBusinessUnitUuid(@Param("businessUnitUuid") UUID businessUnitUuid);

    /**
     * Creates a group objective
     *
     * @param groupObjective a GroupObjective
     * @return number of created group objectives
     */
    int createGroupObjective(@Param("groupObjective") GroupObjective groupObjective);

    /**
     * Delete a group objective
     *
     * @param groupObjectiveUuid an identifier
     * @return number of deleted group objectives
     */
    int deleteGroupObjective(@Param("groupObjectiveUuid") UUID groupObjectiveUuid);

    /**
     * Returns max version of group objectives
     *
     * @param businessUnitUuid an identifier of business unit
     * @return max version of group objectives
     */
    int getMaxVersionGroupObjective(@Param("businessUnitUuid") UUID businessUnitUuid);

    /**
     * Returns a review
     *
     * @param reviewUuid an identifier
     * @return a Review
     */
    Review getReviewByUuid(@Param("reviewUuid") UUID reviewUuid);

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
                     @Param("reviewType") ReviewType type,
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
                            @Param("reviewType") ReviewType type);

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
    int updateReview(@Param("review") Review review);

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
                           @Param("type") ReviewType type,
                           @Param("number") Integer number,
                           @Param("newStatus") ReviewStatus newStatus,
                           @Param("prevReviewStatuses") Collection<ReviewStatus> prevReviewStatuses);

    /**
     * Delete a review
     *
     * @param reviewUuid an identifier of review
     * @return number of deleted reviews
     */
    int deleteReview(@Param("reviewUuid") UUID reviewUuid);

    /**
     * Insert or update a working group objective
     *
     * @param workingGroupObjective a WorkingGroupObjective
     * @return number of inserted or updated working group objectives
     */
    int insertOrUpdateWorkingGroupObjective(@Param("workingGroupObjective") WorkingGroupObjective workingGroupObjective);

    /**
     * Returns a working group objective
     *
     * @param businessUnitUuid an identifier of business unit
     * @return a WorkingGroupObjective
     */
    WorkingGroupObjective getWorkingGroupObjective(@Param("businessUnitUuid") UUID businessUnitUuid);

    /**
     * Delete a working group objective
     *
     * @param businessUnitUuid an identifier of business unit
     * @return number of deleted working group objectives
     */
    int deleteWorkingGroupObjective(@Param("businessUnitUuid") UUID businessUnitUuid);

}
