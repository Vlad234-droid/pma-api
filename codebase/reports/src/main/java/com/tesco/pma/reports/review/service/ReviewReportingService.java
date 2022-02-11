package com.tesco.pma.reports.review.service;

import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.reporting.Report;

import javax.validation.constraints.NotNull;

/**
 * Service for review reporting data
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
}