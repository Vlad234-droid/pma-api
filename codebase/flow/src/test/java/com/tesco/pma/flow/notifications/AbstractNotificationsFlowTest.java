package com.tesco.pma.flow.notifications;

import com.tesco.pma.bpm.camunda.flow.AbstractCamundaSpringBootTest;
import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.api.Profile;
import com.tesco.pma.colleague.api.workrelationships.WorkLevel;
import com.tesco.pma.colleague.api.workrelationships.WorkRelationship;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.colleague.profile.domain.TypedAttribute;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.event.Event;
import com.tesco.pma.event.EventSupport;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.flow.notifications.handlers.SendNotificationHandler;
import com.tesco.pma.flow.notifications.service.ColleagueInboxNotificationSender;
import com.tesco.pma.fs.service.FileService;
import com.tesco.pma.review.dao.TimelinePointDAO;
import com.tesco.pma.review.domain.TimelinePoint;
import com.tesco.pma.service.colleague.inbox.client.ColleagueInboxApiClient;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class AbstractNotificationsFlowTest extends AbstractCamundaSpringBootTest {

    @SpyBean
    SendNotificationHandler sendNotificationHandler;

    @MockBean
    ProfileService profileService;

    @MockBean
    NamedMessageSourceAccessor messageSourceAccessor;

    @SpyBean
    ColleagueInboxNotificationSender colleagueInboxNotificationSender;

    @MockBean
    ColleagueInboxApiClient colleagueInboxApiClient;

    @MockBean
    FileService fileService;

    @MockBean
    TimelinePointDAO timelinePointDAO;


    void check(Map<String, Integer> execBlocks, Event event) {
        var assertion = assertThatForProcess(runProcessByEvent(event));
        execBlocks.forEach((block, execTimes) -> assertion.activity(block).executedTimes(execTimes));
    }

     ColleagueProfile createColleagueProfile(UUID colleagueUUID, WorkLevel wl, Map<String, String> attrs){
        return createColleagueProfile(colleagueUUID, "Random" , "Name", wl, attrs);
    }

    ColleagueProfile createColleagueProfile(UUID colleagueUUID, String firstName, String lastName,
                                            WorkLevel wl, Map<String, String> attrs){
        var wr = new WorkRelationship();
        wr.setWorkLevel(wl);
        wr.setIsManager(false);

        var profile = new Profile();
        profile.setFirstName(firstName);
        profile.setLastName(lastName);

        var colleague = new Colleague();
        colleague.setColleagueUUID(colleagueUUID);
        colleague.setWorkRelationships(List.of(wr));
        colleague.setProfile(profile);

        ColleagueProfile colleagueProfile = new ColleagueProfile();
        colleagueProfile.setColleague(colleague);
        colleagueProfile.setProfileAttributes(new ArrayList<>());
        attrs.forEach((k, v) -> colleagueProfile.getProfileAttributes().add(createAttr(k, v)));
        return colleagueProfile;
    }

     TypedAttribute createAttr(String name, String value) {
        var attr = new TypedAttribute();
        attr.setName(name);
        attr.setValue(value);
        return attr;
    }

    EventSupport createEvent(String evenName, String timelineCode) {
        var event = new EventSupport(evenName);

        if(timelineCode != null) {
            var timelinePoint = new TimelinePoint();
            timelinePoint.setUuid(UUID.randomUUID());
            Mockito.when(timelinePointDAO.getTimelineByUUID(Mockito.eq(timelinePoint.getUuid()))).thenReturn(timelinePoint);
            timelinePoint.setCode(timelineCode);
            event.putProperty(FlowParameters.TIMELINE_POINT_UUID.name(), timelinePoint.getUuid());
        }

        return event;
    }

    void checkContent(String eventName, String eventNameExpected, String content) {
        if (!eventName.equals(eventNameExpected)) {
            return;
        }

        checkContent(content);
    }

    void checkContent(String content) {

        Mockito.verify(colleagueInboxApiClient, Mockito.atLeastOnce()).sendNotification(Mockito.argThat(msg -> {
            assertEquals(content, msg.getContent());
            return true;
        }));
    }

    void checkTitle(String eventName, String eventNameExpected, String title) {
        if (!eventName.equals(eventNameExpected)) {
            return;
        }

        checkTitle(title);
    }

    void checkTitle(String title) {
        Mockito.verify(colleagueInboxApiClient, Mockito.atLeastOnce()).sendNotification(Mockito.argThat(msg -> {
            assertEquals(title, msg.getTitle());
            return true;
        }));
    }

}
