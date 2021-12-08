package com.tesco.pma.review.service;

import com.tesco.pma.review.dao.TimelinePointDAO;
import com.tesco.pma.review.domain.TimelinePoint;
import com.tesco.pma.service.BatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class TimelinePointServiceImpl implements TimelinePointService {

    private final TimelinePointDAO dao;
    private final BatchService batchService;

    @Override
    public void saveAll(Collection<TimelinePoint> timelinePoints) {
        batchService.executeDBOperationInBatch(timelinePoints, dao::saveAll);
    }
}