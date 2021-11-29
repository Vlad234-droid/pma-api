package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.event.EventSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventSendHandler extends CamundaAbstractFlowHandler {

    @Autowired
    EventSender eventSender;

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        eventSender.send(context.getEvent());
    }
}
