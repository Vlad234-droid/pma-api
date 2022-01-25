package com.tesco.pma.reports.review.dao;

import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.reports.review.domain.ObjectiveLinkedReviewData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ReviewReportingDAO {

    /**
     * Find Objectives linked with reviews
     *
     * @param year        - time of colleague cycle
     * @param statuses    - statuses of review
     * @return linked Objectives report data
     */
    List<ObjectiveLinkedReviewData> getLinkedObjectivesData(@Param("year") Integer year,
                                                            @Param("statuses") List<PMTimelinePointStatus> statuses);
}
