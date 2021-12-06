package com.tesco.pma.review.service;

import com.tesco.pma.review.domain.TimelinePoint;

import java.util.Collection;

public interface TimelinePointService {

    /**
     * Creates list of timeline points
     *
     * @param timelinePoints list of timeline points
     */
    void saveAll(Collection<TimelinePoint> timelinePoints);
}