package com.tesco.pma.reporting.review.dao;

import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.reporting.review.domain.ObjectiveLinkedReviewReport;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

public interface ReviewReportingDAO {

    /**
     * Find Objectives linked with reviews
     *
     * @param tlPointUuid - identifier of timeline point
     * @param status      - status of timeline point
     * @return linked Objectives report data
     */
    ObjectiveLinkedReviewReport getLinkedObjectivesData(@Param("tlPointUuid") UUID tlPointUuid,
                                                        @Param("status") PMTimelinePointStatus status);
}
