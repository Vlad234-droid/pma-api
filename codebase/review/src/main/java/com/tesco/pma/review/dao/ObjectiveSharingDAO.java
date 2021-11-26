package com.tesco.pma.review.dao;

import com.tesco.pma.cycle.api.PMReviewStatus;
import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.review.domain.Review;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

/**
 * Interface to perform database operation on objectives
 */
public interface ObjectiveSharingDAO {

    /**
     * Creates record into shared_objective table
     *
     * @param colleagueUuid - colleague identifier
     * @param cycleUuid   - performance cycle identifier
     * @return number of inserted rows
     */
    int shareObjectives(@Param("colleagueUuid") UUID colleagueUuid, @Param("cycleUuid") UUID cycleUuid);

    /**
     * Deletes record from shared_objective table
     *
     * @param colleagueUuid - colleague identifier
     * @param cycleUuid   - performance cycle identifier
     * @return number of deleted rows
     */
    int stopSharingObjectives(@Param("colleagueUuid") UUID colleagueUuid, @Param("cycleUuid") UUID cycleUuid);

    /**
     * Check if record exists into shared_objective table
     *
     * @param colleagueUuid - colleague identifier
     * @param cycleUuid   - performance cycle identifier
     * @return true/false
     */
    boolean isColleagueShareObjectives(@Param("colleagueUuid") UUID colleagueUuid, @Param("cycleUuid") UUID cycleUuid);

    /**
     * Get list of approved objectives by colleague and performance cycle
     *
     * @param colleagueUuid - colleague identifier
     * @param cycleUuid   - performance cycle identifier
     * @return list of objectives
     */
    default List<Review> getColleagueSharedObjectives(UUID colleagueUuid, UUID cycleUuid) {
        return getReviewsByParams(colleagueUuid, cycleUuid, PMReviewType.OBJECTIVE, PMReviewStatus.APPROVED);
    }

    /**
     * Returns a review by performance cycle, colleague, review type and status.
     *
     * @param colleagueUuid an identifier of colleague
     * @param cycleUuid     an identifier of performance cycle
     * @param reviewType    a review type
     * @param reviewStatus  a review status
     * @return a list of reviews
     */
    List<Review> getReviewsByParams(@Param("colleagueUuid") UUID colleagueUuid, @Param("cycleUuid") UUID cycleUuid,
                                    @Param("type") PMReviewType reviewType, @Param("status") PMReviewStatus reviewStatus);

}
