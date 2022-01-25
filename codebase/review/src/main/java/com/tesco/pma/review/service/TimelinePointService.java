package com.tesco.pma.review.service;

import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.review.domain.TimelinePoint;

import java.util.Collection;
import java.util.UUID;

public interface TimelinePointService {

    /**
     * Creates list of timeline points
     *
     * @param timelinePoints list of timeline points
     */
    void saveAll(Collection<TimelinePoint> timelinePoints);

    /**
     * Updates a timeline point status
     *
     * @param uuid         an identifier of timeline point
     * @param newStatus    a new timeline point status
     * @param prevStatuses previous timeline point statuses
     * @return number of updated timeline point statuses
     */
    int updateStatus(UUID uuid,
                     PMTimelinePointStatus newStatus,
                     Collection<PMTimelinePointStatus> prevStatuses);
}