package com.tesco.pma.flow;

import com.tesco.pma.bpm.camunda.flow.CamundaSpringBootTestConfig;
import com.tesco.pma.flow.handlers.CreateColleagueTimelinePointHandler;
import com.tesco.pma.flow.handlers.InitTimelinePointHandler;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.scenario.ProcessScenario;
import org.camunda.bpm.scenario.Scenario;
import org.camunda.bpm.scenario.delegate.ExternalTaskDelegate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(
        classes = {CamundaSpringBootTestConfig.class},
        properties = "camunda.bpm.deployment-resource-pattern=com/tesco/pma/flow/init_timeline_point.bpmn"
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class InitTimelinePointFlowTest {

    private static final String KEY = "init_timeline_point";
    private static final String END_EVENT = "Event_0a8ctnz";
    private static final String INIT_TIMELINE_POINT = "initTimelinePointHandler";
    private static final String CREATE_TIMELINE_POINT = "Activity_1hk4ozx";
    private static final String SCHEDULE_TIMELINE_POINT_ACTIVITY = "Activity_18xvw5z";
    private static final String SCHEDULE_TIMELINE_POINT = "schedule_timeline_point";

    ProcessScenario scenario = mock(ProcessScenario.class);
    @MockBean
    InitTimelinePointHandler initTimelinePointHandler;
    @MockBean
    CreateColleagueTimelinePointHandler createColleagueTimelinePointHandler;

    @Test
    void shouldCreateTimelinePoint() {
        //given
        var variables = Variables.createVariables()
                .putValue(FlowParameters.SCHEDULED.name(), false)
                .putValue(FlowParameters.COLLEAGUE_UUID.name(), UUID.randomUUID());

        //when
        Scenario.run(scenario).startByKey(KEY, variables).execute();

        //then
        verify(scenario, times(1)).hasCompleted(INIT_TIMELINE_POINT);
        verify(scenario, times(1)).hasCompleted(CREATE_TIMELINE_POINT);
        verify(scenario, times(1)).hasFinished(END_EVENT);
    }

    @Test
    void shouldScheduleTimelinePoint() {
        //given
        var variables = Variables.createVariables()
                .putValue(FlowParameters.SCHEDULED.name(), true);
        when(scenario.waitsAtMockedCallActivity(SCHEDULE_TIMELINE_POINT_ACTIVITY)).thenReturn(ExternalTaskDelegate::complete);

        //when
        Scenario.run(scenario)
                .withMockedProcess(SCHEDULE_TIMELINE_POINT)
                .startByKey(KEY, variables)
                .execute();

        //then
        verify(scenario, times(1)).hasCompleted(INIT_TIMELINE_POINT);
        verify(scenario, times(0)).hasCompleted(CREATE_TIMELINE_POINT);
        verify(scenario, times(1)).hasFinished(END_EVENT);
    }

}