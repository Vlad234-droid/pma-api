package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.model.PMCycleElement;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.event.EventNames;
import com.tesco.pma.event.EventParams;
import com.tesco.pma.event.EventSupport;
import com.tesco.pma.event.service.EventSender;
import com.tesco.pma.flow.FlowParameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FinalizeFlowHandler extends CamundaAbstractFlowHandler {

    private final PMCycleService pmCycleService;
    private final EventSender eventSender;

    @Override
    protected void execute(ExecutionContext context) {
        var cycle = context.getVariable(FlowParameters.PM_CYCLE, PMCycle.class);
        pmCycleService.completeCycle(cycle.getUuid());
        int nextCycleMax = Integer.parseInt(cycle.getProperties().getMapJson().getOrDefault(PMCycleElement.PM_CYCLE_MAX, "0")) - 1;
        if (nextCycleMax > 0) {
            EventSupport event = new EventSupport(EventNames.REPEAT_CYCLE);
            event.setEventProperties(Map.of(
                    EventParams.PM_CYCLE_UUID.name(), cycle.getUuid(),
                    PMCycleElement.PM_CYCLE_MAX, nextCycleMax));
            eventSender.sendEvent(event);
        }
    }
}
