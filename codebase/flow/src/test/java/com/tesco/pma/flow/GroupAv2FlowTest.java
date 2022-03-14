package com.tesco.pma.flow;

import com.tesco.pma.bpm.camunda.flow.CamundaSpringBootTestConfig;
import com.tesco.pma.flow.handlers.CreateColleagueCycleHandler;
import com.tesco.pma.flow.handlers.InitCycleHandler;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.camunda.bpm.scenario.delegate.ExternalTaskDelegate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(
        classes = {CamundaSpringBootTestConfig.class},
        properties = "camunda.bpm.deployment-resource-pattern=com/tesco/pma/flow/group_a_v2.bpmn"
)
class GroupAv2FlowTest {

    private static final String SCHEDULE_START_CYCLE = "schedule_start_cycle";
    private static final String EYR = "eyr";
    private static final String Q_3 = "q3";
    private static final String MYR = "myr";
    private static final String Q_1 = "q1";
    private static final String OBJECTIVE = "objective";
    private static final String SCHEDULE_END_CYCLE = "Activity_0nievhn";
    private static final String SCHEDULE_START_ANNUAL_CYCLE = "schedule_start_annual_cycle";
    private static final String INIT_TIMELINE_POINT = "init_timeline_point";
    private static final String SCHEDULE_END_ANNUAL_CYCLE = "schedule_end_annual_cycle";
    private static final String GROUP_A_V_2 = "group_a_v2";
    private static final String END_EVENT_CYCLE_FINISHED = "Event_0v9jvii";
    private static final String INIT_PROCESS = "Activity_0z4446w";
    private static final String CREATE_COLLEAGUE_CYCLE = "Activity_1o7jf3y";

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
                .putValue(FlowParameters.PM_CYCLE_UUID.name(), UUID.randomUUID());
        when(scenario.waitsAtMockedCallActivity(SCHEDULE_START_CYCLE)).thenReturn(ExternalTaskDelegate::complete);
        when(scenario.waitsAtMockedCallActivity(EYR)).thenReturn(ExternalTaskDelegate::complete);
        when(scenario.waitsAtMockedCallActivity(Q_3)).thenReturn(ExternalTaskDelegate::complete);
        when(scenario.waitsAtMockedCallActivity(MYR)).thenReturn(ExternalTaskDelegate::complete);
        when(scenario.waitsAtMockedCallActivity(Q_1)).thenReturn(ExternalTaskDelegate::complete);
        when(scenario.waitsAtMockedCallActivity(OBJECTIVE)).thenReturn(ExternalTaskDelegate::complete);
        when(scenario.waitsAtMockedCallActivity(SCHEDULE_END_CYCLE)).thenReturn(ExternalTaskDelegate::complete);

        //when
        Scenario.run(scenario)
                .withMockedProcess(SCHEDULE_START_ANNUAL_CYCLE)
                .withMockedProcess(INIT_TIMELINE_POINT)
                .withMockedProcess(SCHEDULE_END_ANNUAL_CYCLE)
                .startByKey(GROUP_A_V_2, variables).execute();

        //then
        verify(scenario, times(1)).hasCompleted(INIT_PROCESS);
        verify(scenario, times(1)).hasFinished(END_EVENT_CYCLE_FINISHED);
    }

    @Test
    void shouldFinishColleagueCycle() {
        //given
        var variables = Variables.createVariables()
                .putValue(FlowParameters.SCHEDULED.name(), false)
                .putValue(FlowParameters.COLLEAGUE_UUID.name(), UUID.randomUUID())
                .putValue(FlowParameters.PM_CYCLE_UUID.name(), UUID.randomUUID());
        when(scenario.waitsAtMockedCallActivity(EYR)).thenReturn(ExternalTaskDelegate::complete);
        when(scenario.waitsAtMockedCallActivity(Q_3)).thenReturn(ExternalTaskDelegate::complete);
        when(scenario.waitsAtMockedCallActivity(MYR)).thenReturn(ExternalTaskDelegate::complete);
        when(scenario.waitsAtMockedCallActivity(Q_1)).thenReturn(ExternalTaskDelegate::complete);
        when(scenario.waitsAtMockedCallActivity(OBJECTIVE)).thenReturn(ExternalTaskDelegate::complete);

        //when
        Scenario.run(scenario)
                .withMockedProcess(SCHEDULE_START_ANNUAL_CYCLE)
                .withMockedProcess(INIT_TIMELINE_POINT)
                .withMockedProcess(SCHEDULE_END_ANNUAL_CYCLE)
                .startByKey(GROUP_A_V_2, variables).execute();

        //then
        verify(scenario, times(1)).hasCompleted(CREATE_COLLEAGUE_CYCLE);
        verify(scenario, times(1)).hasFinished(END_EVENT_CYCLE_FINISHED);
    }

}
