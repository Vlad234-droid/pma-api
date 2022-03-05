package com.tesco.pma.flow;

import com.tesco.pma.bpm.camunda.flow.CamundaSpringBootTestConfig;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.flow.handlers.ColleaguesEventsSendHandler;
import com.tesco.pma.flow.handlers.UpdatePMCycleStatusHandler;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@SpringBootTest(
        classes = {CamundaSpringBootTestConfig.class},
        properties = "camunda.bpm.deployment-resource-pattern=com/tesco/pma/flow/schedule_start_annual_cycle.bpmn"
)
class ScheduleStartCycleFlowTest {

    public static final UUID CYCLE_UUID = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");

    public static final String KEY = "schedule_start_annual_cycle";
    public static final String END_EVENT = "Event_0kgnfko";
    public static final String CYCLE_BEFORE_START_DATE = "CYCLE_BEFORE_START_DATE";
    public static final String CYCLE_START_DATE = "CYCLE_START_DATE";
    public static final String SEND_NOTIFICATION_BEFORE_START = "Activity_0039qsr";
    public static final String UPDATE_STATUS = "Activity_1h31hx9";
    public static final String SEND_START = "Activity_11j279t";
    public static final String SEND_NOTIFICATION_START = "Activity_1hj18fw";

    ProcessScenario scenario = mock(ProcessScenario.class);
    @MockBean
    ColleaguesEventsSendHandler colleaguesEventsSendHandler;
    @MockBean
    UpdatePMCycleStatusHandler updatePMCycleStatusHandler;

    @Test
    void shouldFinishScheduledCycle() {
        //given
        var variables = Variables.createVariables()
                .putValue(CYCLE_BEFORE_START_DATE, LocalDate.now().toString())
                .putValue(CYCLE_START_DATE, LocalDate.now().toString())
                .putValue(FlowParameters.PM_CYCLE.name(), buildPmCycle());

        //when
        Scenario.run(scenario).startByKey(KEY, variables).execute();

        //then
        verify(scenario, times(1)).hasCompleted(SEND_NOTIFICATION_BEFORE_START);
        verify(scenario, times(1)).hasCompleted(UPDATE_STATUS);
        verify(scenario, times(1)).hasCompleted(SEND_START);
        verify(scenario, times(1)).hasCompleted(SEND_NOTIFICATION_START);
        verify(scenario, times(1)).hasFinished(END_EVENT);
    }

    private PMCycle buildPmCycle() {
        return PMCycle.builder()
                .uuid(CYCLE_UUID)
                .build();
    }
}