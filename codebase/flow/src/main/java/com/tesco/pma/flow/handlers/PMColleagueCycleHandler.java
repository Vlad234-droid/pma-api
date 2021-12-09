package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.cycle.api.PMColleagueCycle;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.service.PMColleagueCycleService;
import com.tesco.pma.organisation.service.ConfigEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PMColleagueCycleHandler extends CamundaAbstractFlowHandler {

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
