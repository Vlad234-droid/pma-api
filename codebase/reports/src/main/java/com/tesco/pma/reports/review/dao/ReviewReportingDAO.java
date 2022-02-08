package com.tesco.pma.reports.review.dao;

import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.reports.domain.ColleagueReportTargeting;
import com.tesco.pma.reports.review.domain.ObjectiveLinkedReviewData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ReviewReportingDAO {

    /**
     * Find Objectives linked with reviews
     *
     * @param requestQuery - parameters for filters by year of colleague cycle, statuses of review, etc
     * @return linked Objectives report data
     */
    List<ObjectiveLinkedReviewData> getLinkedObjectivesData(@Param("requestQuery") RequestQuery requestQuery);

    List<ColleagueReportTargeting> getColleagueTargeting(@Param("requestQuery") RequestQuery requestQuery);

    List<ColleagueReportTargeting> getColleagueTargetingAnniversary(@Param("requestQuery") RequestQuery requestQuery);
}
