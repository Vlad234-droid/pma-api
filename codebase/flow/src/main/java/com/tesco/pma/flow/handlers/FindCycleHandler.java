package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.pagination.RequestQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.tesco.pma.cycle.api.PMCycleStatus.ACTIVE;
import static com.tesco.pma.cycle.api.PMCycleStatus.REGISTERED;
import static com.tesco.pma.cycle.api.PMCycleStatus.STARTED;

@Component
@RequiredArgsConstructor
public class FindCycleHandler extends CamundaAbstractFlowHandler {

    private static final String STATUS_IN_CONDITION = "status_in";
    private static final String ENTRY_CONFIG_KEY_CONDITION = "entry-config-key";

    private final PMCycleService pmCycleService;

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        var configKey = context.getVariable(FlowParameters.PM_CYCLE_KEY, String.class);
        var requestQuery = new RequestQuery();
        requestQuery.addFilters(STATUS_IN_CONDITION, List.of(ACTIVE.getId(), REGISTERED.getId(), STARTED.getId()));
        requestQuery.addFilters(ENTRY_CONFIG_KEY_CONDITION, configKey);

        try {
            var pmCycles = pmCycleService.getAll(requestQuery, false);
            if (pmCycles.size() == 1) {
                context.setVariable(FlowParameters.PM_CYCLE, pmCycles.get(0));
            } else {
                log.warn("Found {} pm cycles: {}", pmCycles.size(), pmCycles);
            }
        } catch (NotFoundException ex) {
            log.warn("Pm cycle does not found by query: {}", requestQuery);
        }
    }
}
