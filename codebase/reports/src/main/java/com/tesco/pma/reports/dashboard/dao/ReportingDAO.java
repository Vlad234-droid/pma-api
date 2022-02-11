package com.tesco.pma.reports.dashboard.dao;

import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.reports.dashboard.domain.ColleagueReportTargeting;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ReportingDAO {

    /**
     * Find colleagues marked with tags
     *
     * @param requestQuery - parameters for filters
     * @return colleagues with tags
     */
    List<ColleagueReportTargeting> getColleagueTargeting(@Param("requestQuery") RequestQuery requestQuery);

    /**
     * Find colleagues anniversary reviews marked with tags
     *
     * @param requestQuery - parameters for filters
     * @return colleagues with tags
     */
    List<ColleagueReportTargeting> getColleagueTargetingAnniversary(@Param("requestQuery") RequestQuery requestQuery);
}
