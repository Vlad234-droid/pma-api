package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.flow.FlowParameters;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class InitReminderNotificationHandler extends AbstractInitNotificationHandler {

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        super.execute(context);
        context.setVariable(FlowParameters.QUARTER, context.getEvent().getEventProperty(FlowParameters.QUARTER.name()));
    }

}