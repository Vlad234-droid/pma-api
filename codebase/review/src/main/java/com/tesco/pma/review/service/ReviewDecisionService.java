package com.tesco.pma.review.service;

import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.cycle.api.PMTimelinePointStatus;

import java.util.Collection;

public interface ReviewDecisionService {

    Collection<PMTimelinePointStatus> getReviewAllowedStatuses(PMReviewType reviewType, String operation);

    Collection<PMTimelinePointStatus> getReviewAllowedPrevStatuses(PMReviewType reviewType, PMTimelinePointStatus newStatus);
}
