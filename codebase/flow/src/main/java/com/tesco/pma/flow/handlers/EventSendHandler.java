package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.event.EventSupport;
import com.tesco.pma.event.service.EventSender;
import org.springframework.stereotype.Component;

/**
 * Send event handler
 * Param: Map - EVENT_PARAMS
 * Field injection: eventNameExpression
 */
@Component
public class EventSendHandler extends AbstractEventSendHandler {

    private final EventSender eventSender;

    public EventSendHandler(EventSender eventSender) {
        super();
        this.eventSender = eventSender;
    }

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        var params = getParams(context);
        var event = EventSupport.create(getEventNameExpression(), params);
        eventSender.sendEvent(event, null, isErrorSensitiveExpression());
    }

}
