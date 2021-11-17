package com.tesco.pma.review.dao;

import com.tesco.pma.api.ActionType;
import com.tesco.pma.review.domain.Review;
import com.tesco.pma.cycle.api.PMReviewStatus;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;

import static java.time.Instant.now;

public interface ReviewAuditLogDAO {

    default int logReviewUpdating(Review review, PMReviewStatus newStatus, String changeReason, String updatedBy) {
        return intLogReviewUpdating(review, newStatus, changeReason, updatedBy, now());
    }

    default int logOrgObjectiveAction(ActionType actionType, String updatedBy) {
        return intLogOrgObjectiveAction(actionType, updatedBy, now());
    }

    int intLogReviewUpdating(@Param("review") Review review,
                             @Param("newStatus") PMReviewStatus newStatus,
                             @Param("changeReason") String changeReason,
                             @Param("updatedBy") String updatedBy,
                             @Param("updatedTime") Instant updatedTime);

    int intLogOrgObjectiveAction(@Param("actionType") ActionType actionType,
                                 @Param("updatedBy") String updatedBy,
                                 @Param("updatedTime") Instant updatedTime);
}
