package com.tesco.pma.review.dao;

import com.tesco.pma.api.ActionType;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.review.domain.AuditOrgObjectiveReport;
import com.tesco.pma.review.domain.Review;
import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.time.Instant.now;

public interface ReviewAuditLogDAO {

    /**
     * Log review status changes
     *
     * @param review       a review
     * @param newStatus    a new review status
     * @param changeReason a reason of changing status
     * @param updatedBy    an identifier of user who made changes
     * @return number of inserted rows
     */
    default int logReviewUpdating(Review review, PMTimelinePointStatus newStatus, String changeReason, UUID updatedBy) {
        return intLogReviewUpdating(review, newStatus, changeReason, updatedBy, now());
    }

    /**
     * Log organisation objective actions
     *
     * @param actionType a type of action (SAVE, PUBLISH etc.)
     * @param updatedBy  an identifier of user who made actions
     * @return number of inserted rows
     */
    default int logOrgObjectiveAction(ActionType actionType, UUID updatedBy) {
        return intLogOrgObjectiveAction(actionType, updatedBy, now());
    }

    int intLogReviewUpdating(@Param("review") Review review,
                             @Param("newStatus") PMTimelinePointStatus newStatus,
                             @Param("changeReason") String changeReason,
                             @Param("updatedBy") UUID updatedBy,
                             @Param("updatedTime") Instant updatedTime);

    int intLogOrgObjectiveAction(@Param("actionType") ActionType actionType,
                                 @Param("updatedBy") UUID updatedBy,
                                 @Param("updatedTime") Instant updatedTime);

    /**
     * Get report of organisation objective actions
     *
     * @param requestQuery a request query
     * @return a list of AuditOrgObjectiveReport
     */
    List<AuditOrgObjectiveReport> getAuditOrgObjectiveReport(@Param("requestQuery") RequestQuery requestQuery);
}
