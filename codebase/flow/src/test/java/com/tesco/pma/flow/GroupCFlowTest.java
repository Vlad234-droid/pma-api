package com.tesco.pma.flow;

import com.tesco.pma.bpm.camunda.flow.CamundaSpringBootTestConfig;
import com.tesco.pma.flow.handlers.CreateColleagueCycleHandler;
import com.tesco.pma.flow.handlers.InitCycleHandler;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.camunda.bpm.scenario.delegate.ExternalTaskDelegate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(
        classes = {CamundaSpringBootTestConfig.class},
        properties = "camunda.bpm.deployment-resource-pattern=com/tesco/pma/flow/group_c.bpmn"
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class GroupCFlowTest {

    private static final String KEY = "group_c";
    private static final String SCHEDULE_START_CYCLE = "schedule_start_cycle";
    private static final String EYR = "eyr";
    private static final String SCHEDULE_END_CYCLE = "schedule_end_cycle";
    private static final String SCHEDULE_START_ANNUAL_CYCLE = "schedule_start_annual_cycle";
    private static final String INIT_TIMELINE_POINT = "init_timeline_point";
    private static final String SCHEDULE_END_ANNUAL_CYCLE = "schedule_end_annual_cycle";
    private static final String END_EVENT = "event_cycle_finished";
    private static final String INIT_PROCESS = "init_process";
    private static final String CREATE_COLLEAGUE_CYCLE = "create_colleague_cycle";
    private static final String INIT_ERROR = "init_error";
    private static final String PM_COLLEAGUE_CYCLE_MORE_THAN_ONE_IN_STATUS = "PM_COLLEAGUE_CYCLE_MORE_THAN_ONE_IN_STATUS";
    private static final String END_EVENT_INIT_ERROR = "Event_010wo6f";
    private static final String END_EVENT_ALREADY_EXISTS_ERROR = "Event_0qnzct4";

    ProcessScenario scenario = mock(ProcessScenario.class);
    @MockBean
    InitCycleHandler initCycleHandler;
    @MockBean
    CreateColleagueCycleHandler createColleagueCycleHandler;

    @Test
    void shouldFinishScheduledCycle() {
        //given
        var variables = Variables.createVariables()
                .putValue(FlowParameters.SCHEDULED.name(), true)
                .putValue(FlowParameters.CYCLE_END_DATE_S.name(), LocalDate.now().plusDays(2).toString())
                .putValue(FlowParameters.PM_CYCLE_UUID.name(), UUID.randomUUID());
        when(scenario.waitsAtMockedCallActivity(SCHEDULE_START_CYCLE)).thenReturn((task) -> task.defer("PT12H1S", task::complete));
        when(scenario.waitsAtMockedCallActivity(EYR)).thenReturn(ExternalTaskDelegate::complete);
        when(scenario.waitsAtMockedCallActivity(SCHEDULE_END_CYCLE)).thenReturn(ExternalTaskDelegate::complete);

        //when
        Scenario.run(scenario)
                .withMockedProcess(SCHEDULE_START_ANNUAL_CYCLE)
                .withMockedProcess(INIT_TIMELINE_POINT)
                .withMockedProcess(SCHEDULE_END_ANNUAL_CYCLE)
                .startByKey(KEY, variables).execute();

        //then
        verify(scenario, times(1)).hasCompleted(INIT_PROCESS);
        verify(scenario, times(1)).hasFinished(END_EVENT);
    }

    @Test
    void shouldFinishColleagueCycle() {
        //given
        var variables = Variables.createVariables()
                .putValue(FlowParameters.SCHEDULED.name(), false)
                .putValue(FlowParameters.COLLEAGUE_UUID.name(), UUID.randomUUID())
                .putValue(FlowParameters.PM_CYCLE_UUID.name(), UUID.randomUUID());
        when(scenario.waitsAtMockedCallActivity(EYR)).thenReturn(ExternalTaskDelegate::complete);

        //when
        Scenario.run(scenario)
                .withMockedProcess(SCHEDULE_START_ANNUAL_CYCLE)
                .withMockedProcess(INIT_TIMELINE_POINT)
                .withMockedProcess(SCHEDULE_END_ANNUAL_CYCLE)
                .startByKey(KEY, variables).execute();

        //then
        verify(scenario, times(1)).hasCompleted(CREATE_COLLEAGUE_CYCLE);
        verify(scenario, times(1)).hasFinished(END_EVENT);
    }

    @Test
    void shouldEndByInitializationError() throws Exception {
        //given
        doThrow(new BpmnError(INIT_ERROR)).when(initCycleHandler).execute(any());

        //when
        Scenario.run(scenario).startByKey(KEY).execute();

        //then
        verify(scenario, times(1)).hasCanceled(INIT_PROCESS);
        verify(scenario, times(1)).hasFinished(END_EVENT_INIT_ERROR);
    }

    @Test
    void shouldEndByAlreadyExistsError() throws Exception {
        //given
        var variables = Variables.createVariables()
                .putValue(FlowParameters.SCHEDULED.name(), false)
                .putValue(FlowParameters.COLLEAGUE_UUID.name(), UUID.randomUUID());
        doThrow(new BpmnError(PM_COLLEAGUE_CYCLE_MORE_THAN_ONE_IN_STATUS)).when(createColleagueCycleHandler).execute(any());

        //when
        Scenario.run(scenario).startByKey(KEY, variables).execute();

        //then
        verify(scenario, times(1)).hasCanceled(CREATE_COLLEAGUE_CYCLE);
        verify(scenario, times(1)).hasFinished(END_EVENT_ALREADY_EXISTS_ERROR);
    }

    @Test
    void shouldFinishCycleMoreThanTwoTimes() {
        //given
        var variables = Variables.createVariables()
                .putValue(FlowParameters.CYCLE_END_DATE_S.name(), LocalDate.now().plusDays(2).toString())
                .putValue(FlowParameters.SCHEDULED.name(), true)
                .putValue(FlowParameters.PM_CYCLE_UUID.name(), UUID.randomUUID());
        when(scenario.waitsAtMockedCallActivity(SCHEDULE_END_CYCLE)).thenReturn(ExternalTaskDelegate::complete);
        when(scenario.waitsAtMockedCallActivity(SCHEDULE_START_CYCLE)).thenReturn((task) -> task.defer("P3DT1S", task::complete));
        when(scenario.waitsAtMockedCallActivity(EYR)).thenReturn(ExternalTaskDelegate::complete);

        //when
        Scenario.run(scenario)
                .withMockedProcess(SCHEDULE_START_ANNUAL_CYCLE)
                .withMockedProcess(INIT_TIMELINE_POINT)
                .withMockedProcess(SCHEDULE_END_ANNUAL_CYCLE)
                .startByKey(KEY, variables).execute();

        //then
        verify(scenario, atLeast(2)).hasFinished(END_EVENT);
    }

}
