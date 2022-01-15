package com.tesco.pma.flow.notifications;

import com.tesco.pma.bpm.camunda.flow.AbstractCamundaSpringBootTest;
import com.tesco.pma.bpm.camunda.flow.CamundaSpringBootTestConfig;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.event.Event;
import com.tesco.pma.event.EventSupport;
import com.tesco.pma.event.service.EventSender;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.flow.handlers.ColleagueEventsSendHandler;
import com.tesco.pma.organisation.service.ConfigEntryService;
import com.tesco.pma.review.domain.TimelinePoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ActiveProfiles("test")
@SpringBootTest(classes = {CamundaSpringBootTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ColleagueEventsSendHandlerTest extends AbstractCamundaSpringBootTest {

    private static final String ENTRY_CONFIG_KEY = "some key";

    @SpyBean
    private ColleagueEventsSendHandler colleagueEventsSendHandler;

    @MockBean
    private ConfigEntryService configEntryService;

    @MockBean
    private EventSender eventSender;

    @MockBean
    private NamedMessageSourceAccessor messageSourceAccessor;

    private final PMCycle pmCycle = new PMCycle();
    private final UUID colleagueUUID = UUID.randomUUID();
    private final TimelinePoint timelinePoint = new TimelinePoint();

    @BeforeEach
    void init(){
        pmCycle.setEntryConfigKey(ENTRY_CONFIG_KEY);

        var colleagueEntity = new ColleagueEntity();
        colleagueEntity.setUuid(colleagueUUID);

        Mockito.when(configEntryService.findColleaguesByCompositeKey(Mockito.eq(ENTRY_CONFIG_KEY)))
                .thenReturn(List.of(colleagueEntity));
    }

    @Test
    void test(){

        var event = new EventSupport("TEST_EVENT");
        event.putProperty(FlowParameters.PM_CYCLE.name(), pmCycle);
        event.putProperty(FlowParameters.TIMELINE_POINT.name(), timelinePoint);
        runProcessByEvent(event);

        Mockito.verify(eventSender, Mockito.times(1))
                .sendEvent(Mockito.argThat(this::checkEvent), Mockito.isNull(), Mockito.anyBoolean());
    }

    private boolean checkEvent(Event event){
        assertEquals("NF_TEST_EVENT", event.getEventName());
        assertEquals(timelinePoint, event.getEventProperty(FlowParameters.TIMELINE_POINT.name()));
        return true;
    }

}
