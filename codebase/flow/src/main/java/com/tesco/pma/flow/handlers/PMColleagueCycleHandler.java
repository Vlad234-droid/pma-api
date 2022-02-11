package com.tesco.pma.flow.handlers;

import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.service.PMColleagueCycleService;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.organisation.service.ConfigEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PMColleagueCycleHandler extends AbstractColleagueCycleHandler {

    private final PMColleagueCycleService pmColleagueCycleService;
    private final PMCycleService pmCycleService;
    private final ConfigEntryService configEntryService;

    @Override
    protected void execute(ExecutionContext context) {
        var cycleUuid = context.getVariable(FlowParameters.PM_CYCLE, PMCycle.class).getUuid();
        var cycle = pmCycleService.get(cycleUuid, false).getCycle();
        DictionaryFilter<PMCycleStatus> statusFilter = DictionaryFilter.excludeFilter(PMCycleStatus.ACTIVE);
        var colleagues = configEntryService.findColleaguesByCompositeKey(cycle.getEntryConfigKey(), statusFilter);
        var colleagueCycles = colleagues.stream()
                .map(c -> mapToColleagueCycle(c, cycle))
                .collect(Collectors.toList());
        pmColleagueCycleService.saveColleagueCycles(colleagueCycles);
    }
}
