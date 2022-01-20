package com.tesco.pma.reporting.review.dao;

import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.reporting.review.domain.ObjectiveLinkedReviewReport;
import com.tesco.pma.reporting.review.domain.ObjectiveLinkedReviewData;
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
    default ObjectiveLinkedReviewReport getObjectiveLinkedReviewReport(Integer year, List<PMTimelinePointStatus> statuses) {
        var report = new ObjectiveLinkedReviewReport();
        report.setObjectives(getLinkedObjectivesData(year, statuses));

        return report;
    }
    
    List<ObjectiveLinkedReviewData> getLinkedObjectivesData(@Param("year") Integer year,
                                                            @Param("statuses") List<PMTimelinePointStatus> statuses);
}
