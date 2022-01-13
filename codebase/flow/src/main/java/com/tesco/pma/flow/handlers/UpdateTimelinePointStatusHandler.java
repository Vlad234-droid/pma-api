package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.event.Event;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.review.service.TimelinePointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class UpdateTimelinePointStatusHandler extends AbstractUpdateEnumStatusHandler<PMTimelinePointStatus> {

    private final TimelinePointService timelinePointService;

    @Override
    protected void execute(ExecutionContext context) {
        var event = context.getEvent();
        var tlPointUUID = getTlPointUUID(event);
        timelinePointService.updateStatus(tlPointUUID, getStatus(), getOldStatuses());
    }

    @Override
    protected Class<PMTimelinePointStatus> getEnumClass() {
        return PMTimelinePointStatus.class;
    }

    protected UUID getTlPointUUID(Event event) {
        return (UUID) event.getEventProperty(FlowParameters.TL_POINT_UUID.name());
    }
}
