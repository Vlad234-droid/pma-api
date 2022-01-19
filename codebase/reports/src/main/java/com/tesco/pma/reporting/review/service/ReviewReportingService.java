package com.tesco.pma.reporting.review.service;

import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.reporting.Report;

import javax.validation.constraints.NotNull;

/**
 * Service for mapping data management
 */
public interface ReviewReportingService {

    /**
     * Find Objectives linked with reviews
     * @param year - date time of colleague cycle
     * @param status        - status of timeline point
     * @return NotFoundException if review report data doesn't exist.
     */
    Report getLinkedObjectivesData(@NotNull Integer year, @NotNull PMTimelinePointStatus status);
}
