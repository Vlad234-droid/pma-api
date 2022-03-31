package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.flow.FlowParameters;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FindCurrentColleagueCycleHandler extends CamundaAbstractFlowHandler {

    private final PMCycleService pmCycleService;

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        var colleagueUuid = context.getVariable(FlowParameters.COLLEAGUE_UUID, UUID.class);

        try {
            var pmCycle = pmCycleService.getCurrentByColleague(colleagueUuid);
            context.setVariable(FlowParameters.PM_CYCLE, pmCycle);
        } catch (NotFoundException ex) {
            context.setVariable(FlowParameters.PM_CYCLE, null);
        }
    }
}
