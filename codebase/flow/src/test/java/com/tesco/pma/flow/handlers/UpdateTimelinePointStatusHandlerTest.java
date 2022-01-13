package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.camunda.flow.AbstractCamundaSpringBootTest;
import com.tesco.pma.bpm.camunda.flow.CamundaSpringBootTestConfig;
import com.tesco.pma.event.EventSupport;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.review.service.TimelinePointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

@ActiveProfiles("test")
@SpringBootTest(classes = {CamundaSpringBootTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class UpdateTimelinePointStatusHandlerTest extends AbstractCamundaSpringBootTest {

    private static final String PM_TL_POINT_CHECK_OVERDUE = "PM_TL_POINT_CHECK_OVERDUE";

    @SpyBean
    private UpdateTimelinePointStatusHandler updateTimelinePointStatusHandler;

    @MockBean
    private TimelinePointService timelinePointService;

    private final UUID tlPointUUID = UUID.randomUUID();

    @BeforeEach
    public void init() {

        Mockito.when(timelinePointService.updateStatus(any(), any(), any()))
                .thenReturn(1);
    }

    @Test
    void checkTlPointUpdateStatus() throws Exception {
        assertThatForProcess(runProcessByEvent(createEvent(PM_TL_POINT_CHECK_OVERDUE)))
                .activity("updateTimelinePointStatus").executedOnce();
    }

    EventSupport createEvent(String evenName) {
        var event = new EventSupport(evenName);

        event.putProperty(FlowParameters.TL_POINT_UUID.name(), tlPointUUID);
        return event;
    }
}