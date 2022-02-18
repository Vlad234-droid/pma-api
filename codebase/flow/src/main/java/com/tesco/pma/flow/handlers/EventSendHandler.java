package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.event.EventSupport;
import com.tesco.pma.event.service.EventSender;
import com.tesco.pma.flow.FlowParameters;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventSendHandler extends AbstractEventSendHandler {

    private final EventSender eventSender;

    public EventSendHandler(NamedMessageSourceAccessor messageSourceAccessor,
                            EventSender eventSender) {
        super(messageSourceAccessor);
        this.eventSender = eventSender;
    }

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        var params = getParams(context);
        var event = EventSupport.create(getEventNameExpression(), params);
        eventSender.sendEvent(event, null, isErrorSensitiveExpression());
    }


    private Map<String, Serializable> getParams(ExecutionContext context) {
        var params = context.getVariable(FlowParameters.EVENT_PARAMS);

        if (!(params instanceof Map)) {
            params = new HashMap<>();
        }

        return (Map) params;
    }


}
