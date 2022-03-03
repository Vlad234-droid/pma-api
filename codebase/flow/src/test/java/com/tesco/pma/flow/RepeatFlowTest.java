package com.tesco.pma.flow;

import com.tesco.pma.bpm.camunda.flow.CamundaSpringBootTestConfig;
import com.tesco.pma.flow.handlers.BuildNextCycleHandler;
import com.tesco.pma.flow.handlers.CreateCycleHandler;
import com.tesco.pma.flow.handlers.StartCycleHandler;
import com.tesco.pma.flow.handlers.UpdatePMCycleStatusHandler;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@SpringBootTest(classes = {CamundaSpringBootTestConfig.class})
class RepeatFlowTest {

    ProcessScenario scenario = mock(ProcessScenario.class);
    @MockBean
    BuildNextCycleHandler buildNextCycleHandler;
    @MockBean
    CreateCycleHandler createCycleHandler;
    @MockBean
    UpdatePMCycleStatusHandler updatePMCycleStatusHandler;
    @MockBean
    StartCycleHandler startCycleHandler;

    private static final String END_EVENT_NEXT_CYCLE_STARTED = "Event_0udd3h7";
    private static final String BUILD_NEXT_CYCLE = "Activity_0u4krab";
    private static final String CREATE_CYCLE = "Activity_0unikql";
    private static final String START_CYCLE = "Activity_0uxnsor";

    @Test
    void shouldStartNextCycleByRepeatEvent() {
        //when
        Scenario.run(scenario).startByMessage("PM_CYCLE_REPEAT").execute();

        //then
        verify(scenario, times(1)).hasCompleted(BUILD_NEXT_CYCLE);
        verify(scenario, times(1)).hasCompleted(CREATE_CYCLE);
        verify(scenario, times(1)).hasCompleted(START_CYCLE);
        verify(scenario, times(1)).hasFinished(END_EVENT_NEXT_CYCLE_STARTED);
    }

}
