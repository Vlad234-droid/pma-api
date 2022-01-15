package com.tesco.pma.flow.notifications.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.event.Event;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.review.domain.TimelinePoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class InitTimelinePointNotificationHandler extends AbstractInitNotificationHandler {

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        super.execute(context);

        context.setVariable(FlowParameters.TIMELINE_POINT, getTimelinePoint(context.getEvent()));
    }

    protected TimelinePoint getTimelinePoint(Event event) {
        return (TimelinePoint) event.getEventProperty(FlowParameters.TIMELINE_POINT.name());
    }


}