package com.tesco.pma.review.service;

import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.cycle.api.PMTimelinePointStatus;

import java.util.List;

public interface ReviewDmnService {

    List<PMTimelinePointStatus> getReviewAllowedStatuses(PMReviewType reviewType, String operation);

    List<PMTimelinePointStatus> getReviewAllowedPrevStatuses(PMReviewType reviewType, PMTimelinePointStatus newStatus);

    List<PMTimelinePointStatus> getTlPointAllowedPrevStatuses(PMTimelinePointStatus newStatus);
}
