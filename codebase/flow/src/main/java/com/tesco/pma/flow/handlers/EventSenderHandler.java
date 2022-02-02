package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.event.EventSupport;
import com.tesco.pma.event.service.EventSender;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EventSenderHandler extends AbstractEventSendHandler {

    private final EventSender eventSender;

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        var event = EventSupport.create(getEventNameExpression(), getParams(context));
        eventSender.sendEvent(event, null, isErrorSensitiveExpression());
    }

}
