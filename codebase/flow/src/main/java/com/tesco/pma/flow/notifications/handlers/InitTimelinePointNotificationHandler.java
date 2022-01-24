package com.tesco.pma.flow.notifications.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.review.dao.TimelinePointDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class InitTimelinePointNotificationHandler extends AbstractInitNotificationHandler {

    private final TimelinePointDAO timelinePointDAO;

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        super.execute(context);

        var timelineUUID = (UUID) context.getEvent().getEventProperty(FlowParameters.TIMELINE_POINT_UUID.name());
        var timelinePoint = timelinePointDAO.getTimelineByUUID(timelineUUID);
        context.setVariable(FlowParameters.TIMELINE_POINT, timelinePoint);
    }

}