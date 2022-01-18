package com.tesco.pma.reporting.review.dao;

import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.reporting.review.domain.ObjectiveLinkedReviewReport;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;

public interface ReviewReportingDAO {

    /**
     * Find Objectives linked with reviews
     *
     * @param startTime - start time of colleague cycle
     * @param endTime   - end time of colleague cycle
     * @param status    - status of timeline point
     * @return linked Objectives report data
     */
    ObjectiveLinkedReviewReport getLinkedObjectivesData(@Param("startTime") Instant startTime,
                                                        @Param("endTime") Instant endTime,
                                                        @Param("status") PMTimelinePointStatus status);
}
