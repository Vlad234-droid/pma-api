package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.model.PMCycleElement;
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
public class CalculateRemainingCyclesHandler extends CamundaAbstractFlowHandler {

    @Override
    protected void execute(ExecutionContext context) {
        var cycle = context.getVariable(FlowParameters.PM_CYCLE, PMCycle.class);
        int remainigCycles = -1;
        if (cycle.getProperties() != null && cycle.getProperties().getMapJson() != null) {
            remainigCycles = Integer.parseInt(cycle.getProperties().getMapJson().getOrDefault(FlowParameters.PM_CYCLE_REPEATS_LEFT.name(),
                    "0")) - 1;
        }
        if (remainigCycles < 0 && cycle.getMetadata() != null && cycle.getMetadata().getCycle() != null
                && cycle.getMetadata().getCycle().getProperties() != null) {
            remainigCycles =
                    Integer.parseInt(cycle.getMetadata().getCycle().getProperties().getOrDefault(PMCycleElement.PM_CYCLE_MAX, "0")) - 1;
        }
        context.setVariable(FlowParameters.PM_CYCLE_REPEATS_LEFT, remainigCycles);
    }
}
