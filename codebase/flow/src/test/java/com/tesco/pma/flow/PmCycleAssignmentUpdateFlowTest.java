package com.tesco.pma.flow;

import com.tesco.pma.bpm.camunda.flow.CamundaSpringBootTestConfig;
import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.api.workrelationships.Department;
import com.tesco.pma.colleague.api.workrelationships.WorkLevel;
import com.tesco.pma.colleague.api.workrelationships.WorkRelationship;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.event.EventNames;
import com.tesco.pma.flow.handlers.EventSendHandler;
import com.tesco.pma.flow.handlers.FindCurrentColleagueCycleHandler;
import com.tesco.pma.flow.handlers.FindCycleHandler;
import com.tesco.pma.flow.handlers.ResolveColleagueHandler;
import com.tesco.pma.flow.handlers.UpdatePMColleagueCycleStatusHandler;
import com.tesco.pma.flow.handlers.UpsertColleagueHandler;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@SpringBootTest(
        classes = {CamundaSpringBootTestConfig.class},
        properties = "camunda.bpm.deployment-resource-pattern=com/tesco/pma/flow/pm_cycle_assignment_update.bpmn," +
                "com/tesco/pma/flow/pm_cycle_mapping.dmn"
)
class PmCycleAssignmentUpdateFlowTest {

    private static final String MESSAGE_KEY = EventNames.CEP_COLLEAGUE_UPDATED.name();
    private static final String END_EVENT_1 = "end_event_update";
    private static final String END_EVENT_NO_COLLEAGUE = "end_event_no_colleague_update";
    private static final String END_EVENT_NO_CYCLE = "end_event_no_cycle_update";
    private static final String END_EVENT_NO_UPDATE_NEEDED = "end_event_no_update_needed";
    private static final String COLLEAGUE_NOT_FOUND = "COLLEAGUE_NOT_FOUND";
    private static final String PM_CYCLE_MORE_THAN_ONE_IN_STATUSES = "PM_CYCLE_MORE_THAN_ONE_IN_STATUSES";

    private static final String RESOLVE_COLLEAGUE_HANDLER_ACTIVITY = "resolve_colleague_handler_update";
    private static final String CALCULATE_CYCLE_UPDATE_ACTIVITY = "calculate_cycle_update";
    private static final String FIND_CYCLE_UPDATE_ACTIVITY = "find_cycle_update";
    private static final String UPSERT_COLLEAGUE_UPDATE_ACTIVITY = "upsert_colleague_update";
    private static final String CHANGE_STATUS_PM_COL_CYCLE_ACTIVITY = "change_status_pm_col_cycle_update";
    private static final String SEND_EVENT_COL_CYCLE_UPDATE_ACTIVITY = "send_event_col_cycle_update";
    private static final String FIND_CURRENT_CYCLE_ACTIVITY = "find_current_cycle_update";

    private final ProcessScenario scenario = mock(ProcessScenario.class);

    @MockBean
    private ResolveColleagueHandler resolveColleagueHandler;
    @MockBean
    private FindCycleHandler findCycleHandler;
    @MockBean
    private UpsertColleagueHandler upsertColleagueHandler;
    @MockBean
    private EventSendHandler eventSendHandler;
    @MockBean
    private FindCurrentColleagueCycleHandler findCurrentColleagueCycleHandler;
    @MockBean
    private UpdatePMColleagueCycleStatusHandler updatePMColleagueCycleStatusHandler;

    @Test
    void successFlowNoUpdateTheSameCycle() {
        //given
        var colleagueUuid = UUID.randomUUID();
        var colleague = getColleagueGroupA();
        var pmCycle = new PMCycle();
        pmCycle.setEntryConfigKey("group_a1");
        var variables = Variables.createVariables()
                .putValue(FlowParameters.COLLEAGUE_UUID.name(), colleagueUuid)
                .putValue(FlowParameters.COLLEAGUE.name(), colleague)
                .putValue(FlowParameters.PM_CYCLE.name(), pmCycle);

        //when
        Scenario.run(scenario).startByMessage(MESSAGE_KEY, variables).execute();

        //then
        verify(scenario, times(1)).hasCompleted(RESOLVE_COLLEAGUE_HANDLER_ACTIVITY);
        verify(scenario, times(1)).hasCompleted(UPSERT_COLLEAGUE_UPDATE_ACTIVITY);
        verify(scenario, times(1)).hasCompleted(FIND_CURRENT_CYCLE_ACTIVITY);
        verify(scenario, times(1)).hasCompleted(CALCULATE_CYCLE_UPDATE_ACTIVITY);
        verify(scenario, times(1)).hasFinished(END_EVENT_NO_UPDATE_NEEDED);
    }

    @Test
    void successFlowNoUpdateTheSameCycleNull() {
        //given
        var colleagueUuid = UUID.randomUUID();
        var colleague = getColleagueNoCycle();
        var pmCycle = new PMCycle();
        pmCycle.setEntryConfigKey(null);
        var variables = Variables.createVariables()
                .putValue(FlowParameters.COLLEAGUE_UUID.name(), colleagueUuid)
                .putValue(FlowParameters.COLLEAGUE.name(), colleague)
                .putValue(FlowParameters.PM_CYCLE.name(), pmCycle);

        //when
        Scenario.run(scenario).startByMessage(MESSAGE_KEY, variables).execute();

        //then
        verify(scenario, times(1)).hasCompleted(RESOLVE_COLLEAGUE_HANDLER_ACTIVITY);
        verify(scenario, times(1)).hasCompleted(UPSERT_COLLEAGUE_UPDATE_ACTIVITY);
        verify(scenario, times(1)).hasCompleted(FIND_CURRENT_CYCLE_ACTIVITY);
        verify(scenario, times(1)).hasCompleted(CALCULATE_CYCLE_UPDATE_ACTIVITY);
        verify(scenario, times(1)).hasFinished(END_EVENT_NO_UPDATE_NEEDED);
    }

    @Test
    void colleagueNotFound() throws Exception {
        //given
        var colleagueUuid = UUID.randomUUID();
        var variables = Variables.createVariables()
                .putValue(FlowParameters.COLLEAGUE_UUID.name(), colleagueUuid);
        doThrow(new BpmnError(COLLEAGUE_NOT_FOUND)).when(resolveColleagueHandler).execute(any());

        //when
        Scenario.run(scenario).startByMessage(MESSAGE_KEY, variables).execute();

        //then
        verify(scenario, times(1)).hasCanceled(RESOLVE_COLLEAGUE_HANDLER_ACTIVITY);
        verify(scenario, times(1)).hasFinished(END_EVENT_NO_COLLEAGUE);

    }

//    @Test
//    void colleagueNotFound() throws Exception {
//        //given
//        var colleagueUuid = UUID.randomUUID();
//        var variables = Variables.createVariables()
//                .putValue(FlowParameters.COLLEAGUE_UUID.name(), colleagueUuid);
//        doThrow(new BpmnError(COLLEAGUE_NOT_FOUND)).when(resolveColleagueHandler).execute(any());
//
//        //when
//        Scenario.run(scenario).startByMessage(MESSAGE_KEY, variables).execute();
//
//        //then
//        verify(scenario, times(1)).hasCanceled(RESOLVE_COLLEAGUE_HANDLER_ACTIVITY);
//        verify(scenario, times(1)).hasFinished(END_EVENT_NO_COLLEAGUE);
//    }
//
//    @Test
//    void cycleNotFound() throws Exception {
//        //given
//        var colleagueUuid = UUID.randomUUID();
//        var colleague = getColleague();
//        var variables = Variables.createVariables()
//                .putValue(FlowParameters.COLLEAGUE_UUID.name(), colleagueUuid)
//                .putValue(FlowParameters.COLLEAGUE.name(), colleague);
//
//        doThrow(new BpmnError(PM_CYCLE_MORE_THAN_ONE_IN_STATUSES)).when(findCycleHandler).execute(any());
//        //when
//        Scenario.run(scenario).startByMessage(MESSAGE_KEY, variables).execute();
//
//        //then
//        verify(scenario, times(1)).hasCompleted(RESOLVE_COLLEAGUE_HANDLER_ACTIVITY);
//        verify(scenario, times(1)).hasCompleted(CALCULATE_CYCLE_NEW_JOINER_ACTIVITY);
//        verify(scenario, times(1)).hasCanceled(FIND_CYCLE_NEW_JOINER_ACTIVITY);
//        verify(scenario, times(1)).hasFinished(END_EVENT_NO_CYCLE);
//    }

    private Colleague getColleagueGroupA() {
        var value = new Colleague();
        var wr = new WorkRelationship();
        wr.setWorkLevel(WorkLevel.WL4);
        wr.setIsManager(false);
        var dep = new Department();
        dep.setBusinessType("Office");
        wr.setDepartment(dep);
        wr.setPrimaryEntity("tesco plc");
        value.setWorkRelationships(List.of(wr));
        return value;
    }

    private Colleague getColleagueNoCycle() {
        var value = new Colleague();
        var wr = new WorkRelationship();
        wr.setWorkLevel(WorkLevel.WL4);
        value.setWorkRelationships(List.of(wr));
        return value;
    }

}