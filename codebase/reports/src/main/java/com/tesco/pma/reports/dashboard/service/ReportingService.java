package com.tesco.pma.reports.dashboard.service;

import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.reporting.Report;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.reports.dashboard.domain.ColleagueReportTargeting;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Service for reporting data
 */
public interface ReportingService {

    /**
     * Find Objectives linked with reviews
     *
     * @param requestQuery - parameters for filters by year of colleague cycle, statuses of review, etc
     * @return linked Objectives report
     * @throws NotFoundException if review report data doesn't exist.
     */
    Report getLinkedObjectivesReport(@NotNull RequestQuery requestQuery);

    /**
     * Find colleagues marked with tags
     * @param requestQuery - parameters for filters
     * @return colleagues with tags
     * @throws NotFoundException if colleagues don't exist.
     */
    List<ColleagueReportTargeting> getReportColleagues(@NotNull RequestQuery requestQuery);

    /**
     * Get statistics report
     * @param requestQuery - parameters for filters
     * @return statistics report data
     * @throws NotFoundException if colleagues with tags don't exist.
     */
    Report getStatsReport(@NotNull RequestQuery requestQuery);
}
