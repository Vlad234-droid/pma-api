package com.tesco.pma.flow;

import com.tesco.pma.bpm.camunda.flow.CamundaSpringBootTestConfig;
import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.api.workrelationships.WorkLevel;
import com.tesco.pma.colleague.api.workrelationships.WorkRelationship;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.flow.handlers.EventSendHandler;
import com.tesco.pma.flow.handlers.FindCycleHandler;
import com.tesco.pma.flow.handlers.ResolveColleagueHandler;
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

import static com.tesco.pma.event.EventNames.PM_COLLEAGUE_CYCLE_ASSIGNMENT_NEW_JOINER;
import static com.tesco.pma.flow.exception.ErrorCodes.PM_CYCLE_NOT_ASSIGNED_FOR_COLLEAGUE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@SpringBootTest(
        classes = {CamundaSpringBootTestConfig.class},
        properties = "camunda.bpm.deployment-resource-pattern=com/tesco/pma/flow/pm_cycle_assignment_new_joiner.bpmn,"
                + "com/tesco/pma/flow/pm_cycle_mapping.dmn"
)
class PmCycleAssignmentNewJoinerFlowTest {

    private static final String MESSAGE_KEY = PM_COLLEAGUE_CYCLE_ASSIGNMENT_NEW_JOINER.name();
    private static final String END_EVENT_1 = "Event_1hceb4a";
    private static final String END_EVENT_2 = "Event_069p1qz";
    private static final String END_EVENT_NO_COLLEAGUE = "Event_03wj34z";
    private static final String END_EVENT_NO_CYCLE = "Event_0wbcq88";
    private static final String COLLEAGUE_NOT_FOUND = "COLLEAGUE_NOT_FOUND";
    private static final String PM_CYCLE_MORE_THAN_ONE_IN_STATUSES = "PM_CYCLE_MORE_THAN_ONE_IN_STATUSES";
    private static final String PM_CYCLE_NOT_ASSIGNED_FOR_COLLEAGUE = "PM_CYCLE_NOT_ASSIGNED_FOR_COLLEAGUE";

    private static final String RESOLVE_COLLEAGUE_HANDLER_ACTIVITY = "resolveColleagueHandler";
    private static final String CALCULATE_CYCLE_NEW_JOINER_ACTIVITY = "calculate_cycle_new_joiner";
    private static final String FIND_CYCLE_NEW_JOINER_ACTIVITY = "find_cycle_new_joiner";
    private static final String UPSERT_COLLEAGUE_NEW_JOINER_ACTIVITY = "upsert_colleague_new_joiner";
    private static final String SEND_EVENT_COL_CYCLE_CREATE_ACTIVITY = "send_event_col_cycle_create";
    private static final String SEND_EVENT_ACC_CREATE_ACTIVITY = "send_event_acc_create";

    private final ProcessScenario scenario = mock(ProcessScenario.class);

    @MockBean
    private ResolveColleagueHandler resolveColleagueHandler;
    @MockBean
    private FindCycleHandler findCycleHandler;
    @MockBean
    private UpsertColleagueHandler upsertColleagueHandler;
    @MockBean
    private EventSendHandler eventSendHandler;

    @Test
    void successFlow() {
        //given
        var colleagueUuid = UUID.randomUUID();
        var colleague = getColleague();
        var variables = Variables.createVariables()
                .putValue(FlowParameters.COLLEAGUE_UUID.name(), colleagueUuid)
                .putValue(FlowParameters.COLLEAGUE.name(), colleague)
                .putValue(FlowParameters.PM_CYCLE.name(), new PMCycle());

        //when
        Scenario.run(scenario).startByMessage(MESSAGE_KEY, variables).execute();

        //then
        verify(scenario, times(1)).hasCompleted(RESOLVE_COLLEAGUE_HANDLER_ACTIVITY);
        verify(scenario, times(1)).hasCompleted(CALCULATE_CYCLE_NEW_JOINER_ACTIVITY);
        verify(scenario, times(1)).hasCompleted(FIND_CYCLE_NEW_JOINER_ACTIVITY);
        verify(scenario, times(1)).hasCompleted(UPSERT_COLLEAGUE_NEW_JOINER_ACTIVITY);
        verify(scenario, times(1)).hasCompleted(SEND_EVENT_COL_CYCLE_CREATE_ACTIVITY);
        verify(scenario, times(1)).hasCompleted(SEND_EVENT_ACC_CREATE_ACTIVITY);
        verify(scenario, times(1)).hasFinished(END_EVENT_1);
        verify(scenario, times(1)).hasFinished(END_EVENT_2);
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

    @Test
    void cycleNotFound() throws Exception {
        //given
        var colleagueUuid = UUID.randomUUID();
        var colleague = getColleague();
        var variables = Variables.createVariables()
                .putValue(FlowParameters.COLLEAGUE_UUID.name(), colleagueUuid)
                .putValue(FlowParameters.COLLEAGUE.name(), colleague);

        doThrow(new BpmnError(PM_CYCLE_MORE_THAN_ONE_IN_STATUSES)).when(findCycleHandler).execute(any());
        //when
        Scenario.run(scenario).startByMessage(MESSAGE_KEY, variables).execute();

        //then
        verify(scenario, times(1)).hasCompleted(RESOLVE_COLLEAGUE_HANDLER_ACTIVITY);
        verify(scenario, times(1)).hasCompleted(CALCULATE_CYCLE_NEW_JOINER_ACTIVITY);
        verify(scenario, times(1)).hasCanceled(FIND_CYCLE_NEW_JOINER_ACTIVITY);
        verify(scenario, times(1)).hasFinished(END_EVENT_NO_CYCLE);
    }

    @Test
    void pmCycleKeyNotFound() throws Exception {
        //given
        var colleagueUuid = UUID.randomUUID();
        var colleague = getColleague();
        var variables = Variables.createVariables()
                .putValue(FlowParameters.COLLEAGUE_UUID.name(), colleagueUuid)
                .putValue(FlowParameters.COLLEAGUE.name(), colleague);

        doThrow(new BpmnError(PM_CYCLE_NOT_ASSIGNED_FOR_COLLEAGUE)).when(findCycleHandler).execute(any());
        //when
        Scenario.run(scenario).startByMessage(MESSAGE_KEY, variables).execute();

        //then
        verify(scenario, times(1)).hasCompleted(RESOLVE_COLLEAGUE_HANDLER_ACTIVITY);
        verify(scenario, times(1)).hasCompleted(CALCULATE_CYCLE_NEW_JOINER_ACTIVITY);
        verify(scenario, times(1)).hasCanceled(FIND_CYCLE_NEW_JOINER_ACTIVITY);
        verify(scenario, times(1)).hasFinished(END_EVENT_NO_CYCLE);
    }

    private Colleague getColleague() {
        var value = new Colleague();
        var wr = new WorkRelationship();
        wr.setWorkLevel(WorkLevel.WL4);
        wr.setIsManager(false);
        value.setWorkRelationships(List.of(wr));
        return value;
    }
}