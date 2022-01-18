package com.tesco.pma.reporting.review.service;

import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.reporting.Report;

import javax.validation.constraints.NotNull;
import java.time.Instant;

/**
 * Service for mapping data management
 */
public interface ReviewReportingService {

    /**
     * Find Objectives linked with reviews
     * @param startTime - start time of colleague cycle
     * @param endTime   - end time of colleague cycle
     * @param status    - status of timeline point
     * @return NotFoundException if review report data doesn't exist.
     */
    Report getLinkedObjectivesData(@NotNull Instant startTime, @NotNull Instant endTime, @NotNull PMTimelinePointStatus status);
}
