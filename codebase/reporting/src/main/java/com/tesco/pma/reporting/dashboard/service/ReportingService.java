package com.tesco.pma.reporting.dashboard.service;

import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.reporting.Report;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.reporting.dashboard.domain.ColleagueReportTargeting;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Service for reporting data
 */
public interface ReportingService {

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
