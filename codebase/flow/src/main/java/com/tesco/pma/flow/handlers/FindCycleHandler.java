package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
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
import java.util.Map;
import java.util.stream.Collectors;

import static com.tesco.pma.cycle.service.PMCycleServiceImpl.ENTRY_CONFIG_KEY_CONDITION;
import static com.tesco.pma.cycle.service.PMCycleServiceImpl.STATUS_CONDITION;
import static com.tesco.pma.flow.exception.ErrorCodes.PM_CYCLE_MORE_THAN_ONE_IN_STATUSES;

/**
 * Find cycle by composite key and statuses
 *
 * Params: ALLOWED_STATUSES - PM cycle statuses
 * PM_CYCLE_KEY - composite key
 */
@Component
@RequiredArgsConstructor
public class FindCycleHandler extends CamundaAbstractFlowHandler {

    private static final String STATUS_IN_CONDITION = STATUS_CONDITION + "_in";
    private static final String ALLOWED_STATUSES = "ALLOWED_STATUSES";
    private static final String COUNT_PARAM = "count";
    private static final String KEY_PARAM = "key";
    private static final String STATUSES_PARAM = "statuses";

    private final PMCycleService pmCycleService;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Override
    protected void execute(ExecutionContext context) throws Exception {
        var configKey = context.getVariable(FlowParameters.PM_CYCLE_KEY, String.class);
        var statuses = getStatuses(context);
        var requestQuery = buildRequestQuery(configKey, statuses);

        var pmCycles = pmCycleService.findAll(requestQuery, false);
        if (pmCycles.size() == 1) {
            context.setVariable(FlowParameters.PM_CYCLE, pmCycles.get(0));
        } else {
            throw new BpmnError(PM_CYCLE_MORE_THAN_ONE_IN_STATUSES.getCode(),
                    messageSourceAccessor.getMessage(PM_CYCLE_MORE_THAN_ONE_IN_STATUSES, Map.of(
                            COUNT_PARAM, pmCycles.size(),
                            KEY_PARAM, configKey,
                            STATUSES_PARAM, statuses)));
        }
    }

    private RequestQuery buildRequestQuery(String configKey, Collection<PMCycleStatus> statuses) {
        var requestQuery = new RequestQuery();
        requestQuery.addFilters(STATUS_IN_CONDITION, statuses);
        requestQuery.addFilters(ENTRY_CONFIG_KEY_CONDITION, configKey);
        return requestQuery;
    }

    private Collection<PMCycleStatus> getStatuses(ExecutionContext context) {
        List<String> params = context.getVariable(ALLOWED_STATUSES);
        return params.stream().map(p -> EnumUtils.getEnumIgnoreCase(PMCycleStatus.class, p)).collect(Collectors.toSet());
    }
}
