package com.tesco.pma.flow.notifications;

import com.tesco.pma.bpm.camunda.flow.AbstractCamundaSpringBootTest;
import com.tesco.pma.bpm.camunda.flow.CamundaSpringBootTestConfig;
import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.api.workrelationships.WorkLevel;
import com.tesco.pma.colleague.api.workrelationships.WorkRelationship;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.colleague.profile.domain.TypedAttribute;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.cycle.api.model.PMElementType;
import com.tesco.pma.event.Event;
import com.tesco.pma.event.EventSupport;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.flow.notifications.handlers.DefaultInitNotificationHandler;
import com.tesco.pma.flow.notifications.handlers.InitCycleNotificationHandler;
import com.tesco.pma.flow.notifications.handlers.InitObjectiveNotificationHandler;
import com.tesco.pma.flow.notifications.handlers.InitReviewNotificationHandler;
import com.tesco.pma.flow.notifications.handlers.InitTipsNotificationHandler;
import com.tesco.pma.flow.notifications.handlers.SendNotificationHandler;
import com.tesco.pma.review.domain.TimelinePoint;
import com.tesco.pma.service.contact.client.ContactApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2021-11-20 00:03
 */
@ActiveProfiles("test")
@SpringBootTest(classes = {CamundaSpringBootTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class NotificationsFlowTest extends AbstractCamundaSpringBootTest {

    @SpyBean
    private DefaultInitNotificationHandler initNotificationHandler;

    @SpyBean
    private InitReviewNotificationHandler reviewInitHandler;

    @SpyBean
    private InitObjectiveNotificationHandler objectiveInitHandler;

    @SpyBean
    private InitCycleNotificationHandler reminderNotificationHandler;

    @SpyBean
    private InitTipsNotificationHandler initTipsNotificationHandler;

    @MockBean(name = "sendNotification")
    private SendNotificationHandler sendNotification;

    @MockBean
    private ContactApiClient contactApiClient;

    @MockBean
    private ProfileService profileService;

    @MockBean
    private NamedMessageSourceAccessor messageSourceAccessor;

    private final UUID colleagueUUID = UUID.randomUUID();
    private ColleagueProfile colleagueProfile;

    @BeforeEach
    public void init(){
        colleagueProfile = createColleagueProfile(colleagueUUID, WorkLevel.WL1, Map.of());

        Mockito.when(profileService.findProfileByColleagueUuid(Mockito.any()))
                .thenReturn(Optional.of(colleagueProfile));
    }

    @Test
    void checkReviewObjectives() throws Exception {
        checkReviewGroup(NotificationEvents.NF_PM_REVIEW_SUBMITTED.name(), PMReviewType.OBJECTIVE, true, true);
        checkReviewGroup(NotificationEvents.NF_PM_REVIEW_SUBMITTED.name(), PMReviewType.OBJECTIVE, false, false);
        checkReviewGroup(NotificationEvents.NF_PM_REVIEW_APPROVED.name(), PMReviewType.OBJECTIVE, true, true);
        checkReviewGroup(NotificationEvents.NF_PM_REVIEW_APPROVED.name(), PMReviewType.OBJECTIVE, false, true);
        checkReviewGroup(NotificationEvents.NF_PM_REVIEW_DECLINED.name(), PMReviewType.OBJECTIVE, true, true);
        checkReviewGroup(NotificationEvents.NF_PM_REVIEW_DECLINED.name(), PMReviewType.OBJECTIVE, false, true);
    }

    @Test
    void checReviewMYR() throws Exception {
        checkReviewGroup(NotificationEvents.NF_PM_REVIEW_BEFORE_START.name(), PMReviewType.MYR, true, true);
        checkReviewGroup(NotificationEvents.NF_PM_REVIEW_BEFORE_START.name(), PMReviewType.MYR, false, true);
        checkReviewGroup(NotificationEvents.NF_PM_REVIEW_SUBMITTED.name(), PMReviewType.MYR, true, true);
        checkReviewGroup(NotificationEvents.NF_PM_REVIEW_SUBMITTED.name(), PMReviewType.MYR, false, false);
        checkReviewGroup(NotificationEvents.NF_PM_REVIEW_APPROVED.name(), PMReviewType.MYR, true, true);
        checkReviewGroup(NotificationEvents.NF_PM_REVIEW_APPROVED.name(), PMReviewType.MYR, false, false);
        checkReviewGroup(NotificationEvents.NF_PM_REVIEW_DECLINED.name(), PMReviewType.MYR, true, true);
        checkReviewGroup(NotificationEvents.NF_PM_REVIEW_DECLINED.name(), PMReviewType.MYR, false, true);
        checkReviewGroup(NotificationEvents.NF_PM_REVIEW_BEFORE_END.name(), PMReviewType.MYR, true, true);
        checkReviewGroup(NotificationEvents.NF_PM_REVIEW_BEFORE_END.name(), PMReviewType.MYR, false, true);
    }

    @Test
    void checkReviewEYR() throws Exception {
        checkReviewGroup(NotificationEvents.NF_PM_REVIEW_BEFORE_START.name(), PMReviewType.EYR, true, true);
        checkReviewGroup(NotificationEvents.NF_PM_REVIEW_BEFORE_START.name(), PMReviewType.EYR, false, true);
        checkReviewGroup(NotificationEvents.NF_PM_REVIEW_SUBMITTED.name(), PMReviewType.EYR, true, true);
        checkReviewGroup(NotificationEvents.NF_PM_REVIEW_SUBMITTED.name(), PMReviewType.EYR, false, false);
        checkReviewGroup(NotificationEvents.NF_PM_REVIEW_APPROVED.name(), PMReviewType.EYR, true, true);
        checkReviewGroup(NotificationEvents.NF_PM_REVIEW_APPROVED.name(), PMReviewType.EYR, false, true);
        checkReviewGroup(NotificationEvents.NF_PM_REVIEW_DECLINED.name(), PMReviewType.EYR, true, true);
        checkReviewGroup(NotificationEvents.NF_PM_REVIEW_DECLINED.name(), PMReviewType.EYR, false, true);
        checkReviewGroup(NotificationEvents.NF_PM_REVIEW_BEFORE_END.name(), PMReviewType.EYR, true, true);
        checkReviewGroup(NotificationEvents.NF_PM_REVIEW_BEFORE_END.name(), PMReviewType.EYR, false, true);
    }

    @Test
    void checkObjectives() throws Exception {
        checkObjectivesGroup(NotificationEvents.NF_ORGANISATION_OBJECTIVES.name(), null, true, WorkLevel.WL1, false);
        checkObjectivesGroup(NotificationEvents.NF_ORGANISATION_OBJECTIVES.name(), null, true, WorkLevel.WL4, true);

        checkObjectivesGroup(NotificationEvents.NF_OBJECTIVES_APPROVED_FOR_SHARING.name(), null, true, WorkLevel.WL1, true);
        checkObjectivesGroup(NotificationEvents.NF_OBJECTIVES_APPROVED_FOR_SHARING.name(), null, false, WorkLevel.WL1, false);
        checkObjectivesGroup(NotificationEvents.NF_OBJECTIVE_SHARING_START.name(), null, false, WorkLevel.WL1, true);
        checkObjectivesGroup(NotificationEvents.NF_OBJECTIVE_SHARING_END.name(), null, false, WorkLevel.WL1, true);

    }

    @Test
    void checkFeedbacks() throws Exception {
        checkFeedbackGroup(NotificationEvents.NF_FEEDBACK_GIVEN.name(), null, false, WorkLevel.WL1, true);
        checkFeedbackGroup(NotificationEvents.NF_FEEDBACK_REQUESTS_RESPONDED.name(), null, false, WorkLevel.WL1, true);
        checkFeedbackGroup(NotificationEvents.NF_FEEDBACK_REQUESTED.name(), null, false, WorkLevel.WL1, true);
    }

    @Test
    void checkTimelineNotifications() throws Exception {

        checkTimelineNotifications(NotificationEvents.NF_START_TIMELINE_NOTIFICATION.name(), "Q1", true);
        checkTimelineNotifications(NotificationEvents.NF_START_TIMELINE_NOTIFICATION.name(), "Q3", true);
        checkTimelineNotifications(NotificationEvents.NF_START_TIMELINE_NOTIFICATION.name(), "Q2", false);
    }

    @Test
    void beforeCycleTest() throws Exception {
        checkCycleGroup(NotificationEvents.NF_BEFORE_CYCLE_START.name(), null, false, WorkLevel.WL1, true);
        checkCycleGroup(NotificationEvents.NF_BEFORE_CYCLE_END.name(), null, false, WorkLevel.WL1, true);

        checkCycleGroup(NotificationEvents.NF_BEFORE_CYCLE_START.name(), null, true, WorkLevel.WL1, true);
        checkCycleGroup(NotificationEvents.NF_BEFORE_CYCLE_END.name(), null, true, WorkLevel.WL1, true);
    }

    @Test
    void checkReceiveTips() throws Exception {
        checkTipsGroup(NotificationEvents.NF_TIPS_RECEIVED.name(), false, WorkLevel.WL1, true);
    }

    void checkReviewGroup(String evenName, PMReviewType reviewType, Boolean isManager, boolean send) throws Exception {
        check("initReviewNotification", "review_decision_table", evenName, reviewType, isManager, null, send);
    }

    void checkObjectivesGroup(String evenName, PMReviewType reviewType, Boolean isManager, WorkLevel workLevel, boolean send) throws Exception {
        check("InitObjectivesNotifications", "objectives_decision_table", evenName, reviewType, isManager, workLevel, send);
    }

    void checkFeedbackGroup(String evenName, PMReviewType reviewType, Boolean isManager, WorkLevel workLevel, boolean send) throws Exception {
        check("InitFeedbacksNotifications", "feedbacks_decision_table", evenName, reviewType, isManager, workLevel, send);
    }

    void checkTimelineNotifications(String evenName, String quarter, boolean send) throws Exception {
        var event = createEvent(evenName, null);
        event.putProperty(FlowParameters.QUARTER.name(), quarter);
        check("InitCycleNotifications", "cycle_decision_table", event, false, null, send);
    }

    void checkCycleGroup(String evenName, PMReviewType reviewType, Boolean isManager, WorkLevel workLevel, boolean send) throws Exception {
        check("InitCycleNotifications", "cycle_decision_table", evenName, reviewType, isManager, workLevel, send);
    }

    void checkTipsGroup(String evenName, Boolean isManager, WorkLevel workLevel, boolean send) throws Exception {
        var event = createEvent(evenName, null);
        event.putProperty(FlowParameters.TIP_UUID.name(), UUID.randomUUID().toString());
        check("InitTipsNotifications", "tips_decision_table", event, isManager, workLevel, send);
    }

    void check(String initHandlerName, String decisionTable, String evenName, PMReviewType reviewType, Boolean isManager, WorkLevel workLevel, boolean send) throws Exception {
        var event = createEvent(evenName, reviewType);
        check(initHandlerName, decisionTable, event, isManager, workLevel, send);
    }

    void check(String initHandlerName, String decisionTable, Event event, Boolean isManager, WorkLevel workLevel, boolean send) throws Exception {

        colleagueProfile.getColleague().getWorkRelationships().get(0).setIsManager(isManager);

        if(workLevel!=null) {
            colleagueProfile.getColleague().getWorkRelationships().get(0).setWorkLevel(workLevel);
        }

        assertThatForProcess(runProcessByEvent(event))
                .activity(initHandlerName).executedOnce()
                .activity(decisionTable).executedOnce()
                .activity("sendNotification").executedTimes(send ? 1 : 0);
    }

    EventSupport createEvent(String evenName, PMReviewType reviewType) {
        var event = new EventSupport(evenName);

        if(reviewType != null) {
            event.putProperty(FlowParameters.REVIEW_TYPE.name(), reviewType);
        }

        event.putProperty(FlowParameters.COLLEAGUE_UUID.name(), colleagueUUID);
        return event;
    }



    private ColleagueProfile createColleagueProfile(UUID colleagueUUID, WorkLevel wl, Map<String, String> attrs){
        var wr = new WorkRelationship();
        wr.setWorkLevel(wl);

        var colleague = new Colleague();
        colleague.setColleagueUUID(colleagueUUID);
        colleague.setWorkRelationships(List.of(wr));

        ColleagueProfile colleagueProfile = new ColleagueProfile();
        colleagueProfile.setColleague(colleague);
        colleagueProfile.setProfileAttributes(new ArrayList<>());
        attrs.forEach((k, v) -> colleagueProfile.getProfileAttributes().add(createAttr(k, v)));
        return colleagueProfile;
    }

    private TypedAttribute createAttr(String name, String value) {
        var attr = new TypedAttribute();
        attr.setName(name);
        attr.setValue(value);
        return attr;
    }

    private TimelinePoint createTimelinePoint(String code){
        TimelinePoint tp = new TimelinePoint();
        tp.setType(PMElementType.TIMELINE_POINT);
        tp.setCode(code);
        return tp;
    }
}