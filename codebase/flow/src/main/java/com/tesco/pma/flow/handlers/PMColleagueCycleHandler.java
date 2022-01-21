package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleType;
import com.tesco.pma.cycle.service.PMColleagueCycleService;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.flow.FlowParameters;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PMColleagueCycleHandler extends AbstractColleagueCycleHandler {

    private final PMColleagueCycleService pmColleagueCycleService;
    private final PMCycleService pmCycleService;

    @Override
    protected void execute(ExecutionContext context) {
        UUID cycleUUID = context.getVariable(FlowParameters.PM_CYCLE, PMCycle.class).getUuid();
        var cycle = adjustStartDate(pmCycleService.get(cycleUUID, false).getCycle());
        var hiringDate = PMCycleType.HIRING == cycle.getType() ? LocalDate.now() : null;
        var colleagues = pmColleagueCycleService.findColleagues(cycle.getEntryConfigKey(),
                hiringDate, true);

        var colleagueCycles = colleagues.stream()
                .map(c -> mapToColleagueCycle(c.getUuid(), cycle))
                .collect(Collectors.toList());
        pmColleagueCycleService.saveColleagueCycles(colleagueCycles);
    }
}
