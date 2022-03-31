package com.tesco.pma.flow;

import com.tesco.pma.bpm.camunda.flow.CamundaSpringBootTestConfig;
import com.tesco.pma.cycle.api.model.PMElement;
import com.tesco.pma.cycle.api.model.PMElementType;
import com.tesco.pma.cycle.api.model.PMTimelinePointElement;
import com.tesco.pma.flow.handlers.ColleaguesEventsSendHandler;
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
        properties = "camunda.bpm.deployment-resource-pattern=com/tesco/pma/flow/schedule_timeline_point.bpmn"
)
@DirtiesContext
class ScheduleTimelinePointFlowTest {

    private static final UUID TIMELINE_POINT_UUID = UUID.fromString("a0c0e913-5a45-4165-86a7-2fa9d5b1c8cb");
    private static final String KEY = "schedule_timeline_point";
    private static final String END_EVENT = "Event_1fwl5fm";
    private static final String END_QN_EVENT = "Event_02gqiwm";
    private static final String SCHEDULE_TIMELINE_POINT = "Activity_1du843u";
    private static final String SEND_BEFORE_START_REVIEW = "Activity_0gtujyz";
    private static final String SEND_BEFORE_END_REVIEW = "Activity_14syf2g";
    private static final String SEND_START_REVIEW = "Activity_02h925b";
    private static final String SEND_END_REVIEW = "Activity_1arxp9t";

    ProcessScenario scenario = mock(ProcessScenario.class);
    @MockBean
    ColleaguesEventsSendHandler colleaguesEventsSendHandler;

    @Test
    void shouldSendQn() {
        //given
        var variables = Variables.createVariables()
                .putValue(PMElement.PM_TYPE, PMElementType.TIMELINE_POINT.name().toLowerCase())
                .putValue(FlowParameters.TIMELINE_POINT.name(), buildTimelinePoint())
                .putValue(FlowParameters.START_DATE_S.name(), LocalDate.now().toString())
                .putValue(PMTimelinePointElement.PM_TIMELINE_POINT_CODE, "Q1");

        //when
        Scenario.run(scenario).startByKey(KEY, variables).execute();

        //then
        verify(scenario, times(1)).hasCompleted(SCHEDULE_TIMELINE_POINT);
        verify(scenario, times(1)).hasFinished(END_QN_EVENT);
    }

    @Test
    void shouldStartReview() {
        //given
        var variables = Variables.createVariables()
                .putValue(PMElement.PM_TYPE, PMElementType.REVIEW.name().toLowerCase())
                .putValue(FlowParameters.TIMELINE_POINT.name(), buildTimelinePoint())
                .putValue(FlowParameters.START_DATE_S.name(), LocalDate.now().toString())
                .putValue(FlowParameters.END_DATE_S.name(), LocalDate.now().toString())
                .putValue(FlowParameters.BEFORE_START_DATE_S.name(), LocalDate.now().toString())
                .putValue(FlowParameters.BEFORE_END_DATE_S.name(), LocalDate.now().toString());

        //when
        Scenario.run(scenario).startByKey(KEY, variables).execute();

        //then
        verify(scenario, times(1)).hasCompleted(SEND_BEFORE_START_REVIEW);
        verify(scenario, times(1)).hasCompleted(SEND_BEFORE_END_REVIEW);
        verify(scenario, times(1)).hasCompleted(SEND_START_REVIEW);
        verify(scenario, times(1)).hasCompleted(SEND_END_REVIEW);
        verify(scenario, times(1)).hasFinished(END_EVENT);
    }

    @Test
    void shouldNotSendBeforeStartReviewEventIfDateIsAbsent() {
        //given
        //Don not put BEFORE_START_DATE and BEFORE_END_DATE variables (do not confused with null value)
        var variables = Variables.createVariables()
                .putValue(PMElement.PM_TYPE, PMElementType.REVIEW.name().toLowerCase())
                .putValue(FlowParameters.TIMELINE_POINT.name(), buildTimelinePoint())
                .putValue(FlowParameters.START_DATE_S.name(), LocalDate.now().toString())
                .putValue(FlowParameters.END_DATE_S.name(), LocalDate.now().toString());

        //when
        Scenario.run(scenario).startByKey(KEY, variables).execute();

        //then
        verify(scenario, times(0)).hasCompleted(SEND_BEFORE_START_REVIEW);
        verify(scenario, times(1)).hasCompleted(SEND_START_REVIEW);
        verify(scenario, times(0)).hasCompleted(SEND_BEFORE_END_REVIEW);
        verify(scenario, times(1)).hasCompleted(SEND_END_REVIEW);
        verify(scenario, times(1)).hasFinished(END_EVENT);
    }

    @Test
    void shouldNotSendBeforeStartReviewEventIfDateIsNull() {
        //given
        var variables = Variables.createVariables()
                .putValue(PMElement.PM_TYPE, PMElementType.REVIEW.name().toLowerCase())
                .putValue(FlowParameters.TIMELINE_POINT.name(), buildTimelinePoint())
                .putValue(FlowParameters.START_DATE_S.name(), LocalDate.now().toString())
                .putValue(FlowParameters.END_DATE_S.name(), LocalDate.now().toString())
                .putValue(FlowParameters.BEFORE_START_DATE_S.name(), null)
                .putValue(FlowParameters.BEFORE_END_DATE_S.name(), null);

        //when
        Scenario.run(scenario).startByKey(KEY, variables).execute();

        //then
        verify(scenario, times(0)).hasCompleted(SEND_BEFORE_START_REVIEW);
        verify(scenario, times(1)).hasCompleted(SEND_START_REVIEW);
        verify(scenario, times(0)).hasCompleted(SEND_BEFORE_END_REVIEW);
        verify(scenario, times(1)).hasCompleted(SEND_END_REVIEW);
        verify(scenario, times(1)).hasFinished(END_EVENT);
    }

    private TimelinePoint buildTimelinePoint() {
        return TimelinePoint.builder()
                .uuid(TIMELINE_POINT_UUID)
                .build();
    }

}