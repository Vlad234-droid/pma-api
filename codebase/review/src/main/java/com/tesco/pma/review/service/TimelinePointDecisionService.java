package com.tesco.pma.review.service;

import com.tesco.pma.cycle.api.PMTimelinePointStatus;

import java.util.Collection;

public interface TimelinePointDecisionService {

    Collection<PMTimelinePointStatus> getTlPointAllowedPrevStatuses(PMTimelinePointStatus newStatus);

    String getStatusUpdateEventName(PMTimelinePointStatus status);
}
