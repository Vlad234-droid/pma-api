package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.model.PMCycleElement;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.event.EventParams;
import com.tesco.pma.flow.FlowParameters;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PMCycleRepeatHandler extends CamundaAbstractFlowHandler {

    private final PMCycleService pmCycleService;

    @Override
    protected void execute(ExecutionContext context) {
        var event = context.getEvent();
        if (event != null) {
            var eventProperties = event.getEventProperties();
            if (eventProperties.containsKey(EventParams.PM_CYCLE_UUID.name())) {
                UUID cycleUuid = UUID.fromString(eventProperties.get(EventParams.PM_CYCLE_UUID.name()).toString());
                PMCycle previousCycle = pmCycleService.get(cycleUuid, false).getCycle();
                String nextCycleMax = eventProperties.get(PMCycleElement.PM_CYCLE_MAX).toString();
                previousCycle.getProperties().getMapJson().replace(PMCycleElement.PM_CYCLE_MAX, nextCycleMax);
                PMCycle nextCycle = pmCycleService.create(previousCycle, previousCycle.getCreatedBy().getUuid());
                pmCycleService.start(nextCycle.getUuid());
                context.setVariable(FlowParameters.PM_CYCLE, nextCycle);
            }
        }
    }
}
