package com.tesco.pma.flow.handlers;


import com.tesco.pma.bpm.camunda.flow.FlowTestUtil;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.pagination.RequestQuery;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.tesco.pma.flow.exception.ErrorCodes.PM_CYCLE_MORE_THAN_ONE_IN_STATUSES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FindCycleHandlerTest {

    private static final List<String> STATUSES = List.of("ACTIVE");
    private static final String ALLOWED_STATUSES = "ALLOWED_STATUSES";
    private static final String CONFIG_KEY = "config-key";

    private final PMCycleService pmCycleService = mock(PMCycleService.class);
    private final NamedMessageSourceAccessor messageSourceAccessor = mock(NamedMessageSourceAccessor.class);

    private final FindCycleHandler handler = new FindCycleHandler(pmCycleService, messageSourceAccessor);

    @Test
    void cycleFound() throws Exception {
        var pmCycle = new PMCycle();
        var executionContext = FlowTestUtil.executionBuilder()
                .withVariable(FlowParameters.PM_CYCLE_KEY, CONFIG_KEY)
                .withVariable(ALLOWED_STATUSES, STATUSES)
                .build();
        var rq = new RequestQuery();
        rq.addFilters("status_in", STATUSES);
        rq.addFilters("entry-config-key", CONFIG_KEY);

        when(pmCycleService.findAll(rq, false)).thenReturn(List.of(pmCycle));

        handler.execute(executionContext);

        Assertions.assertEquals(pmCycle, executionContext.getVariable(FlowParameters.PM_CYCLE));
    }

    @Test
    void cycleNotFound() throws Exception {
        var message = "error message";
        var executionContext = FlowTestUtil.executionBuilder()
                .withVariable(FlowParameters.PM_CYCLE_KEY, CONFIG_KEY)
                .withVariable(ALLOWED_STATUSES, STATUSES)
                .build();

        when(pmCycleService.findAll(any(), eq(false))).thenReturn(Collections.emptyList());
        when(messageSourceAccessor.getMessage(PM_CYCLE_MORE_THAN_ONE_IN_STATUSES,
                Map.of("count", 0,
                        "key", CONFIG_KEY,
                        "statuses", STATUSES))).thenReturn(message);

        var bpmnError = assertThrows(BpmnError.class, () -> handler.execute(executionContext));

        assertEquals(PM_CYCLE_MORE_THAN_ONE_IN_STATUSES.getCode(), bpmnError.getErrorCode());
        assertEquals(message, bpmnError.getMessage());
    }
}