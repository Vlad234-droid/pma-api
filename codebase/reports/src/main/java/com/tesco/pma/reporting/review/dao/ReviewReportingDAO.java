package com.tesco.pma.reporting.review.dao;

import com.tesco.pma.reporting.review.domain.ObjectiveLinkedReviewReport;

public interface ReviewReportingDAO {

    /**
     * Find Objectives linked with reviews
     * @return linked Objectives report data
     */
    ObjectiveLinkedReviewReport getLinkedObjectivesData();
}
