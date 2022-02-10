package com.tesco.pma.review.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.exception.AlreadyExistsException;
import com.tesco.pma.review.dao.TimelinePointDAO;
import com.tesco.pma.review.domain.TimelinePoint;
import com.tesco.pma.review.exception.ErrorCodes;
import com.tesco.pma.service.BatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TimelinePointServiceImpl implements TimelinePointService {

    private final TimelinePointDAO dao;
    private final BatchService batchService;
    private final NamedMessageSourceAccessor messages;

    @Override
    public void saveAll(Collection<TimelinePoint> timelinePoints) {
        batchService.executeDBOperationInBatch(timelinePoints, dao::saveAll);
    }

    @Override
    @Transactional
    public int updateStatus(UUID uuid, PMTimelinePointStatus newStatus, Collection<PMTimelinePointStatus> prevStatuses) {
        return dao.updateStatus(uuid, newStatus, prevStatuses);
    }

    @Override
    @Transactional
    public void create(TimelinePoint timelinePoint) {
        try {
            dao.create(timelinePoint);
        } catch (DuplicateKeyException e) {
            throw new AlreadyExistsException(ErrorCodes.TIMELINE_POINT_ALREADY_EXISTS.name(),
                    messages.getMessage(ErrorCodes.TIMELINE_POINT_ALREADY_EXISTS, Map.of("tlPoint", timelinePoint)), e);
        }
    }

    @Override
    public List<TimelinePoint> get(UUID colleagueCycleUuid, String code, PMTimelinePointStatus status) {
        return dao.getByParams(colleagueCycleUuid, code, status);
    }
}