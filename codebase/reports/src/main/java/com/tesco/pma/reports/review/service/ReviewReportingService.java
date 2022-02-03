package com.tesco.pma.reports.review.service;

import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.reporting.Report;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.reports.domain.ColleagueReportTargeting;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Service for mapping data management
 */
public interface ReviewReportingService {

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
    List<ColleagueReportTargeting> getReviewReportColleagues(@NotNull RequestQuery requestQuery);

    /**
     * Get review statistics report
     * @param requestQuery - parameters for filters
     * @return review statistics report data
     * @throws NotFoundException if colleagues with tags don't exist.
     */
    Report getReviewStatsReport(@NotNull RequestQuery requestQuery);
}
