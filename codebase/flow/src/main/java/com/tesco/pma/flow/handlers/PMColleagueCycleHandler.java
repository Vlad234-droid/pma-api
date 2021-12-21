package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleType;
import com.tesco.pma.cycle.service.PMColleagueCycleService;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.organisation.service.ConfigEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PMColleagueCycleHandler extends AbstractColleagueCycleHandler {

    private final ConfigEntryService configEntryService;
    private final PMColleagueCycleService pmColleagueCycleService;

    @Override
    protected void execute(ExecutionContext context) {
        var cycle = context.getVariable(FlowParameters.PM_CYCLE, PMCycle.class);
        var colleagues = PMCycleType.HIRING == cycle.getType()
                ? configEntryService.findColleaguesByCompositeKeyAndHireDate(cycle.getEntryConfigKey(), LocalDate.now())
                : configEntryService.findColleaguesByCompositeKey(cycle.getEntryConfigKey());
        var colleagueCycles = colleagues.stream()
                .map(c -> mapToColleagueCycle(c.getUuid(), cycle))
                .collect(Collectors.toList());
        pmColleagueCycleService.saveColleagueCycles(colleagueCycles);

        context.setVariable(FlowParameters.START_DATE, defineStartDate(cycle));
    }
}
