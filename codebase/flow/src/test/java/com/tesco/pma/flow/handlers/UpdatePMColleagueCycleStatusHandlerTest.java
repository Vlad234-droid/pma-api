package com.tesco.pma.flow.handlers;

import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.bpm.camunda.flow.FlowTestUtil;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.service.PMColleagueCycleService;
import com.tesco.pma.flow.FlowParameters;
import org.camunda.bpm.engine.impl.el.FixedValue;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

class UpdatePMColleagueCycleStatusHandlerTest {

    private final PMColleagueCycleService colleagueCycleService = Mockito.mock(PMColleagueCycleService.class);
    private final UpdatePMColleagueCycleStatusHandler handler = new UpdatePMColleagueCycleStatusHandler(colleagueCycleService);

    @Test
    void execute() {
        handler.setStatusValue(new FixedValue("INACTIVE"));
        handler.setOldStatusValues(new FixedValue("ACTIVE,REGISTERED,STARTED"));

        var colleagueUuid = UUID.randomUUID();
        var pmCycle = new PMCycle();
        pmCycle.setUuid(UUID.randomUUID());

        var executionContext = FlowTestUtil.executionBuilder()
                .withVariable(FlowParameters.COLLEAGUE_UUID, colleagueUuid)
                .withVariable(FlowParameters.PM_CYCLE, pmCycle)
                .build();

        handler.execute(executionContext);

        Mockito.verify(colleagueCycleService)
                .changeStatusForColleagueAndCycle(colleagueUuid, pmCycle.getUuid(),
                        DictionaryFilter.includeFilter(PMCycleStatus.ACTIVE, PMCycleStatus.REGISTERED, PMCycleStatus.STARTED),
                        PMCycleStatus.INACTIVE);
    }

    @Test
    void executeNoUpdate() {
        handler.setStatusValue(new FixedValue("INACTIVE"));
        handler.setOldStatusValues(new FixedValue("ACTIVE,REGISTERED,STARTED"));

        var colleagueUuid = UUID.randomUUID();

        var executionContext = FlowTestUtil.executionBuilder()
                .withVariable(FlowParameters.COLLEAGUE_UUID, colleagueUuid)
                .build();

        handler.execute(executionContext);

        Mockito.verifyNoInteractions(colleagueCycleService);
    }
}