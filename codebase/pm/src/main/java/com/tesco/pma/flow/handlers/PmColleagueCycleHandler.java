package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.CamundaExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.cycle.api.PMColleagueCycle;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.service.PMColleagueCycleService;
import com.tesco.pma.organisation.service.ConfigEntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PmColleagueCycleHandler extends CamundaAbstractFlowHandler {

    private final ConfigEntryService configEntryService;
    private final PMColleagueCycleService pmColleagueCycleService;

    @Override
    protected void execute(ExecutionContext context) {
        PMCycle cycle = context.getVariable(FlowParameters.PM_CYCLE);
        var colleagues = configEntryService.findColleaguesByCompositeKey(cycle.getEntryConfigKey());
        var colleagueCycles = colleagues.stream()
                .map(c -> mapToColleagueCycle(c.getUuid(), cycle))
                .collect(Collectors.toList());

        pmColleagueCycleService.saveColleagueCycles(colleagueCycles);


        log.info("Context variables: \n" + (((CamundaExecutionContext) context).getDelegateExecution()).getVariables());
        log.info("Local variables: \n" + (((CamundaExecutionContext) context).getDelegateExecution()).getVariablesLocal());
    }

    private PMColleagueCycle mapToColleagueCycle(UUID colleagueUuid, PMCycle cycle) {
        var cc = new PMColleagueCycle();

        cc.setUuid(UUID.randomUUID());
        cc.setColleagueUuid(colleagueUuid);
        cc.setCycleUuid(cycle.getUuid());
        cc.setStatus(cycle.getStatus());
        cc.setStartTime(cycle.getStartTime());
        cc.setEndTime(cycle.getEndTime());
        cc.setProperties(cycle.getProperties());

        return cc;
    }
}
