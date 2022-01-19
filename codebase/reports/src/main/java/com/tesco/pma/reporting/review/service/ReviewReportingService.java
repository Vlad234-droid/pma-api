package com.tesco.pma.reporting.review.service;

import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.reporting.Report;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Service for mapping data management
 */
public interface ReviewReportingService {

    /**
     * Find Objectives linked with reviews
     * @param year     - date time of colleague cycle
     * @param statuses - statuses of review
     * @return NotFoundException if review report data doesn't exist.
     */
    Report getLinkedObjectivesData(@NotNull Integer year, @NotEmpty List<PMTimelinePointStatus> statuses);
}
