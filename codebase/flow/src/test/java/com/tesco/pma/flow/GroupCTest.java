package com.tesco.pma.flow;

import com.tesco.pma.bpm.camunda.flow.CamundaSpringBootTestConfig;
import com.tesco.pma.flow.handlers.PMColleagueCycleHandler;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.camunda.bpm.scenario.delegate.ExternalTaskDelegate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(
        classes = {CamundaSpringBootTestConfig.class},
        properties = "camunda.bpm.deployment-resource-pattern=com/tesco/pma/flow/group_c.bpmn"
)
class GroupCTest {

    private static final String KEY = "group_c";
    private static final String END_EVENT = "Event_0v9jvii";
    private static final String EYR = "eyr";
    private static final String STANDARD_INIT_TIMELINE_POINT = "standard_init_timeline_point";
    private static final String INIT_PROCESS = "processColleagueCycles";


    ProcessScenario scenario = mock(ProcessScenario.class);
    @MockBean
    PMColleagueCycleHandler pmColleagueCycleHandler;

    @Test
    void shouldScheduleReview() {
        //given
        when(scenario.waitsAtMockedCallActivity(EYR)).thenReturn(ExternalTaskDelegate::complete);

        //when
        Scenario.run(scenario)
                .withMockedProcess(STANDARD_INIT_TIMELINE_POINT)
                .startByKey(KEY).execute();

        //then
        verify(scenario, times(1)).hasCompleted(INIT_PROCESS);
        verify(scenario, times(1)).hasFinished(END_EVENT);
    }

}
