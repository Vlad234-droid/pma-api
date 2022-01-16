package com.tesco.pma.reporting.review.service;

import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.reporting.review.domain.ObjectiveLinkedReviewReport;

import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Service for mapping data management
 */
public interface ReviewReportingService {

    /**
     * Find Objectives linked with reviews
     * @return linked Objectives report data
     * @throws NotFoundException if review report data doesn't exist.
     */
    ObjectiveLinkedReviewReport getLinkedObjectivesData(@NotNull UUID tlPointUuid,
                                                        @NotNull PMTimelinePointStatus status);
}
