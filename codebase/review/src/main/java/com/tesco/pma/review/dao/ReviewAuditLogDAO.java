package com.tesco.pma.review.dao;

import com.tesco.pma.api.ActionType;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.review.domain.AuditOrgObjectiveReport;
import com.tesco.pma.review.domain.Review;
import com.tesco.pma.cycle.api.PMReviewStatus;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.time.Instant.now;

public interface ReviewAuditLogDAO {

    default int logReviewUpdating(Review review, PMReviewStatus newStatus, String changeReason, UUID updatedBy) {
        return intLogReviewUpdating(review, newStatus, changeReason, updatedBy, now());
    }

    default int logOrgObjectiveAction(ActionType actionType, UUID updatedBy) {
        return intLogOrgObjectiveAction(actionType, updatedBy, now());
    }

    int intLogReviewUpdating(@Param("review") Review review,
                             @Param("newStatus") PMReviewStatus newStatus,
                             @Param("changeReason") String changeReason,
                             @Param("updatedBy") UUID updatedBy,
                             @Param("updatedTime") Instant updatedTime);

    int intLogOrgObjectiveAction(@Param("actionType") ActionType actionType,
                                 @Param("updatedBy") UUID updatedBy,
                                 @Param("updatedTime") Instant updatedTime);

    List<AuditOrgObjectiveReport> getAuditOrgObjectiveReport(@Param("requestQuery") RequestQuery requestQuery);
}
