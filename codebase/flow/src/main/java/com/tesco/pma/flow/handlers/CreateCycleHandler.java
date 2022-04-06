package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.flow.FlowParameters;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateCycleHandler extends CamundaAbstractFlowHandler {

    private final PMCycleService pmCycleService;

    @Override
    protected void execute(ExecutionContext context) {
        var previousCycle = context.getVariable(FlowParameters.PM_CYCLE, PMCycle.class);
        var nextCycle = pmCycleService.create(previousCycle, previousCycle.getCreatedBy().getUuid());
        context.setVariable(FlowParameters.PM_CYCLE_UUID, nextCycle.getUuid());
        context.setVariable(FlowParameters.PM_CYCLE, nextCycle);
    }

}
