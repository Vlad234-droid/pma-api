package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.flow.FlowParameters;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CreateCycleHandler extends CamundaAbstractFlowHandler {

    private final PMCycleService pmCycleService;

    @Override
    protected void execute(ExecutionContext context) {
        PMCycle previousCycle = context.getVariable(FlowParameters.PM_CYCLE, PMCycle.class);
        PMCycle nextCycle = pmCycleService.create(previousCycle, previousCycle.getCreatedBy().getUuid());
        context.setVariable(FlowParameters.PM_CYCLE_UUID, nextCycle.getUuid());
        context.setVariable(FlowParameters.PM_CYCLE, nextCycle);
    }

}
