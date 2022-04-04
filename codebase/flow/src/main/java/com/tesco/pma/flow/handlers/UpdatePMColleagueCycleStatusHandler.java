package com.tesco.pma.flow.handlers;

import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.service.PMColleagueCycleService;
import com.tesco.pma.flow.FlowParameters;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UpdatePMColleagueCycleStatusHandler extends AbstractUpdateEnumStatusHandler<PMCycleStatus> {

    private final PMColleagueCycleService pmColleagueCycleService;

    @Override
    protected void execute(ExecutionContext context) {
        var cycle = context.getNullableVariable(FlowParameters.PM_CYCLE, PMCycle.class);
        var colleagueUuid = context.getVariable(FlowParameters.COLLEAGUE_UUID, UUID.class);

        if (cycle != null) {
            pmColleagueCycleService.changeStatusForColleagueAndCycle(colleagueUuid, cycle.getUuid(),
                    DictionaryFilter.includeFilter(getOldStatuses()), getStatus());
        }
    }

    @Override
    protected Class<PMCycleStatus> getEnumClass() {
        return PMCycleStatus.class;
    }
}
