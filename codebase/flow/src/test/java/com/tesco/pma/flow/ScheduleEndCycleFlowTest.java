package com.tesco.pma.flow;

import com.tesco.pma.bpm.camunda.flow.CamundaSpringBootTestConfig;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.flow.handlers.CalculateRemainingCyclesHandler;
import com.tesco.pma.flow.handlers.ColleaguesEventsSendHandler;
import com.tesco.pma.flow.handlers.EventSendHandler;
import com.tesco.pma.flow.handlers.UpdatePMCycleStatusHandler;
import com.tesco.pma.review.domain.TimelinePoint;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@SpringBootTest(
        classes = {CamundaSpringBootTestConfig.class},
        properties = "camunda.bpm.deployment-resource-pattern=com/tesco/pma/flow/schedule_end_annual_cycle.bpmn"
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ScheduleEndCycleFlowTest {

    private static final UUID CYCLE_UUID = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
    private static final UUID TIMELINE_POINT_UUID = UUID.fromString("a0c0e913-5a45-4165-86a7-2fa9d5b1c8cb");
    private static final String KEY = "schedule_end_annual_cycle";
    private static final String END_EVENT = "Event_0zp8vpa";
    private static final String SEND_NOTIFICATION_BEFORE_END = "Activity_1qxyb5w";
    private static final String UPDATE_STATUS = "Activity_0s5ed5q";
    private static final String SEND_END = "Activity_0v4l2b5";
    private static final String SEND_NOTIFICATION_END = "Activity_15v2e6c";
    private static final String CALCULATE_REMAINING_CYCLES = "Activity_123sf2q";
    private static final String SEND_REPEAT_CYCLE = "Activity_170353t";

    ProcessScenario scenario = mock(ProcessScenario.class);
    @MockBean
    ColleaguesEventsSendHandler colleaguesEventsSendHandler;
    @MockBean
    UpdatePMCycleStatusHandler updatePMCycleStatusHandler;
    @MockBean
    CalculateRemainingCyclesHandler calculateRemainingCyclesHandler;
    @MockBean
    EventSendHandler eventSendHandler;

    @Test
    void shouldFinishEndCycle() {
        //given
        var variables = Variables.createVariables()
                .putValue(FlowParameters.CYCLE_BEFORE_END_DATE.name(), LocalDate.now().toString())
                .putValue(FlowParameters.CYCLE_END_DATE.name(), LocalDate.now().toString())
                .putValue(FlowParameters.PM_CYCLE_REPEATS_LEFT.name(), "5")
                .putValue(FlowParameters.TIMELINE_POINT.name(), buildTimelinePoint())
                .putValue(FlowParameters.PM_CYCLE.name(), buildPmCycle());

        //when
        Scenario.run(scenario).startByKey(KEY, variables).execute();

        //then
        verify(scenario, times(1)).hasCompleted(SEND_NOTIFICATION_BEFORE_END);
        verify(scenario, times(1)).hasCompleted(UPDATE_STATUS);
        verify(scenario, times(1)).hasCompleted(SEND_END);
        verify(scenario, times(1)).hasCompleted(SEND_NOTIFICATION_END);
        verify(scenario, times(1)).hasCompleted(CALCULATE_REMAINING_CYCLES);
        verify(scenario, times(1)).hasCompleted(SEND_REPEAT_CYCLE);
        verify(scenario, times(1)).hasFinished(END_EVENT);
    }

    @Test
    void shouldNotSendBeforeEndEventIfDateIsAbsent() {
        //given
        //Don not put DATE variables (do not confused with null value)
        var variables = Variables.createVariables()
                .putValue(FlowParameters.CYCLE_END_DATE.name(), LocalDate.now().toString())
                .putValue(FlowParameters.PM_CYCLE_REPEATS_LEFT.name(), "5")
                .putValue(FlowParameters.TIMELINE_POINT.name(), buildTimelinePoint())
                .putValue(FlowParameters.PM_CYCLE.name(), buildPmCycle());

        //when
        Scenario.run(scenario).startByKey(KEY, variables).execute();

        //then
        verify(scenario, times(0)).hasCompleted(SEND_NOTIFICATION_BEFORE_END);
        verify(scenario, times(1)).hasCompleted(UPDATE_STATUS);
        verify(scenario, times(1)).hasCompleted(SEND_END);
        verify(scenario, times(1)).hasCompleted(SEND_NOTIFICATION_END);
        verify(scenario, times(1)).hasCompleted(CALCULATE_REMAINING_CYCLES);
        verify(scenario, times(1)).hasCompleted(SEND_REPEAT_CYCLE);
        verify(scenario, times(1)).hasFinished(END_EVENT);
    }

    @Test
    void shouldNotSendBeforeEndEventIfDateIsNull() {
        //given
        var variables = Variables.createVariables()
                .putValue(FlowParameters.CYCLE_BEFORE_END_DATE.name(), null)
                .putValue(FlowParameters.CYCLE_END_DATE.name(), LocalDate.now().toString())
                .putValue(FlowParameters.PM_CYCLE_REPEATS_LEFT.name(), "5")
                .putValue(FlowParameters.TIMELINE_POINT.name(), buildTimelinePoint())
                .putValue(FlowParameters.PM_CYCLE.name(), buildPmCycle());

        //when
        Scenario.run(scenario).startByKey(KEY, variables).execute();

        //then
        verify(scenario, times(0)).hasCompleted(SEND_NOTIFICATION_BEFORE_END);
        verify(scenario, times(1)).hasCompleted(UPDATE_STATUS);
        verify(scenario, times(1)).hasCompleted(SEND_END);
        verify(scenario, times(1)).hasCompleted(SEND_NOTIFICATION_END);
        verify(scenario, times(1)).hasCompleted(CALCULATE_REMAINING_CYCLES);
        verify(scenario, times(1)).hasCompleted(SEND_REPEAT_CYCLE);
        verify(scenario, times(1)).hasFinished(END_EVENT);
    }

    private TimelinePoint buildTimelinePoint() {
        return TimelinePoint.builder()
                .uuid(TIMELINE_POINT_UUID)
                .build();
    }

    private PMCycle buildPmCycle() {
        return PMCycle.builder()
                .uuid(CYCLE_UUID)
                .status(PMCycleStatus.STARTED)
                .build();
    }
}