package com.tesco.pma.flow.notifications.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.flow.FlowParameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class InitTipsNotificationHandler extends AbstractInitNotificationHandler {

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        super.execute(context);
        context.setVariable(FlowParameters.TIP_UUID, context.getEvent().getEventProperty(FlowParameters.TIP_UUID.name()));
    }

}