package com.tesco.pma.objective.dao;

import com.tesco.pma.objective.domain.ObjectiveStatus;
import com.tesco.pma.objective.domain.PersonalObjective;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;

import static java.time.Instant.now;

public interface ReviewAuditLogDAO {
    default int logLogReviewUpdating(PersonalObjective review, ObjectiveStatus newStatus, String changeReason, String updatedBy) {
        return intLogReviewUpdating(review, newStatus, changeReason, updatedBy, now());
    }

    int intLogReviewUpdating(@Param("review") PersonalObjective review,
                             @Param("newStatus") ObjectiveStatus newStatus,
                             @Param("changeReason") String changeReason,
                             @Param("updatedBy") String updatedBy,
                             @Param("updatedTime") Instant updatedTime);
}
