package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.model.PMCycleElement;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.event.service.EventSender;
import com.tesco.pma.flow.FlowParameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
        int repeatCount = Integer.parseInt(cycle.getProperties().getMapJson().getOrDefault(FlowParameters.PM_CYCLE_REPEAT_COUNT.name(), "0")) - 1;
        if (repeatCount <= 0) {
            repeatCount = Integer.parseInt(cycle.getProperties().getMapJson().getOrDefault(PMCycleElement.PM_CYCLE_MAX, "0")) - 1;
        }
        context.setVariable(FlowParameters.PM_CYCLE_REPEAT_COUNT, Integer.toString(repeatCount));
        pmCycleService.completeCycle(cycle.getUuid());
    }
}
