package com.tesco.pma.flow.handlers;

import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.flow.FlowParameters;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2021-12-23 15:01
 */
@Component
@RequiredArgsConstructor
public class UpdatePMCycleStatusHandler extends AbstractUpdateEnumStatusHandler<PMCycleStatus> {

    private final PMCycleService pmCycleService;

    @Override
    protected void execute(ExecutionContext context) {
        var cycle = context.getVariable(FlowParameters.PM_CYCLE, PMCycle.class);
        pmCycleService.updateStatus(cycle.getUuid(), getStatus(), DictionaryFilter.includeFilter(getOldStatuses()));
    }

    @Override
    protected Class<PMCycleStatus> getEnumClass() {
        return PMCycleStatus.class;
    }
}
