package com.tesco.pma.flow.handlers;


import com.tesco.pma.bpm.camunda.flow.FlowTestUtil;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.flow.FlowParameters;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

class FindCurrentColleagueCycleHandlerTest {

    private final PMCycleService pmCycleService = Mockito.mock(PMCycleService.class);
    private final FindCurrentColleagueCycleHandler handler = new FindCurrentColleagueCycleHandler(pmCycleService);

    @Test
    void execute() throws Exception {
        var colleagueUuid = UUID.randomUUID();
        var pmCycle = new PMCycle();
        pmCycle.setUuid(UUID.randomUUID());

        var executionContext = FlowTestUtil.executionBuilder()
                .withVariable(FlowParameters.COLLEAGUE_UUID, colleagueUuid)
                .build();

        Mockito.when(pmCycleService.getCurrentByColleague(colleagueUuid))
                .thenReturn(pmCycle);

        handler.execute(executionContext);

        Assertions.assertEquals(pmCycle, executionContext.getVariable(FlowParameters.PM_CYCLE));
    }

    @Test
    void executeNoCycle() throws Exception {
        var colleagueUuid = UUID.randomUUID();

        var executionContext = FlowTestUtil.executionBuilder()
                .withVariable(FlowParameters.COLLEAGUE_UUID, colleagueUuid)
                .build();

        Mockito.when(pmCycleService.getCurrentByColleague(colleagueUuid))
                .thenThrow(new NotFoundException("code", "message"));

        handler.execute(executionContext);

        Assertions.assertNull(executionContext.getNullableVariable(FlowParameters.PM_CYCLE));
    }

}