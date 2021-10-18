package com.tesco.pma.objective.dao;

import com.tesco.pma.objective.domain.Review;
import com.tesco.pma.objective.domain.ReviewStatus;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;

import static java.time.Instant.now;

public interface ReviewAuditLogDAO {
    default int logLogReviewUpdating(Review review, ReviewStatus newStatus, String changeReason, String updatedBy) {
        return intLogReviewUpdating(review, newStatus, changeReason, updatedBy, now());
    }

    int intLogReviewUpdating(@Param("review") Review review,
                             @Param("newStatus") ReviewStatus newStatus,
                             @Param("changeReason") String changeReason,
                             @Param("updatedBy") String updatedBy,
                             @Param("updatedTime") Instant updatedTime);
}
