package com.tesco.pma.reporting.review.dao;

import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.reporting.review.domain.ObjectiveLinkedReviewReport;
import org.apache.ibatis.annotations.Param;

public interface ReviewReportingDAO {

    /**
     * Find Objectives linked with reviews
     *
     * @param year - time of colleague cycle
     * @param status    - status of timeline point
     * @return linked Objectives report data
     */
    ObjectiveLinkedReviewReport getLinkedObjectivesData(@Param("year") Integer year,
                                                        @Param("status") PMTimelinePointStatus status);
}
