package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.pagination.RequestQuery;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.EnumUtils;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.tesco.pma.cycle.service.PMCycleServiceImpl.ENTRY_CONFIG_KEY_CONDITION;
import static com.tesco.pma.cycle.service.PMCycleServiceImpl.STATUS_CONDITION;
import static com.tesco.pma.flow.exception.ErrorCodes.PM_CYCLE_MORE_THAN_ONE_IN_STATUSES;

@Component
@RequiredArgsConstructor
public class FindCycleHandler extends CamundaAbstractFlowHandler {

    private static final String STATUS_IN_CONDITION = STATUS_CONDITION + "_in";
    private static final String ALLOWED_STATUSES = "ALLOWED_STATUSES";

    private final PMCycleService pmCycleService;

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        var configKey = context.getVariable(FlowParameters.PM_CYCLE_KEY, String.class);
        var requestQuery = new RequestQuery();
        requestQuery.addFilters(STATUS_IN_CONDITION, getStatuses(context));
        requestQuery.addFilters(ENTRY_CONFIG_KEY_CONDITION, configKey);

        var pmCycles = pmCycleService.getAll(requestQuery, false);
        if (pmCycles.size() == 1) {
            context.setVariable(FlowParameters.PM_CYCLE, pmCycles.get(0));
        } else {
            throw new BpmnError(PM_CYCLE_MORE_THAN_ONE_IN_STATUSES.getCode());
        }
    }

    private Collection<PMCycleStatus> getStatuses(ExecutionContext context) {
        List<String> params = context.getVariable(ALLOWED_STATUSES);

        return params.stream().map(p -> EnumUtils.getEnumIgnoreCase(PMCycleStatus.class, p)).collect(Collectors.toSet());
    }
}
