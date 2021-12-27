package com.tesco.pma.flow;

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
import com.tesco.pma.flow.handlers.*;
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

    private static final String PM_REVIEW_SUBMITTED = "PM_REVIEW_SUBMITTED";
    private static final String PM_REVIEW_APPROVED = "PM_REVIEW_APPROVED";
    private static final String PM_REVIEW_DECLINED = "PM_REVIEW_DECLINED";
    private static final String PM_REVIEW_BEFORE_START = "PM_REVIEW_BEFORE_START";
    private static final String PM_REVIEW_BEFORE_END = "PM_REVIEW_BEFORE_END";

    private static final String ORGANISATION_OBJECTIVES = "ORGANISATION_OBJECTIVES";
    private static final String OBJECTIVES_APPROVED_FOR_SHARING = "OBJECTIVES_APPROVED_FOR_SHARING";
    private static final String OBJECTIVE_SHARING_START = "OBJECTIVE_SHARING_START";
    private static final String OBJECTIVE_SHARING_END = "OBJECTIVE_SHARING_END";

    private static final String FEEDBACK_GIVEN = "FEEDBACK_GIVEN";
    private static final String RESPOND_TO_FEEDBACK_REQUESTS = "FEEDBACK_REQUESTS_RESPONDED";
    private static final String REQUEST_FEEDBACK = "FEEDBACK_REQUESTED";

    private static final String BEFORE_CYCLE_START = "BEFORE_CYCLE_START";
    private static final String BEFORE_CYCLE_END = "BEFORE_CYCLE_END";
    private static final String START_TIMELINE_NOTIFICATION = "START_TIMELINE_NOTIFICATION";

    private static final String RECEIVE_TIPS = "TIPS_RECEIVED";

    @SpyBean
    private DefaultInitNotificationHandler initNotificationHandler;

    @SpyBean
    private InitReviewNotificationHandler reviewInitHandler;

    @SpyBean
    private InitObjectiveNotificationHandler objectiveInitHandler;

    @SpyBean
    private InitCycleNotificationHandler reminderNotificationHandler;

    @MockBean(name = "sendNotification")
    private SendNotification sendNotification;

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
        checkReviewGroup(PM_REVIEW_SUBMITTED, PMReviewType.OBJECTIVE, true, true);
        checkReviewGroup(PM_REVIEW_SUBMITTED, PMReviewType.OBJECTIVE, false, false);
        checkReviewGroup(PM_REVIEW_APPROVED, PMReviewType.OBJECTIVE, true, true);
        checkReviewGroup(PM_REVIEW_APPROVED, PMReviewType.OBJECTIVE, false, true);
        checkReviewGroup(PM_REVIEW_DECLINED, PMReviewType.OBJECTIVE, true, true);
        checkReviewGroup(PM_REVIEW_DECLINED, PMReviewType.OBJECTIVE, false, true);
    }

    @Test
    void checReviewMYR() throws Exception {
        checkReviewGroup(PM_REVIEW_BEFORE_START, PMReviewType.MYR, true, true);
        checkReviewGroup(PM_REVIEW_BEFORE_START, PMReviewType.MYR, false, true);
        checkReviewGroup(PM_REVIEW_SUBMITTED, PMReviewType.MYR, true, true);
        checkReviewGroup(PM_REVIEW_SUBMITTED, PMReviewType.MYR, false, false);
        checkReviewGroup(PM_REVIEW_APPROVED, PMReviewType.MYR, true, true);
        checkReviewGroup(PM_REVIEW_APPROVED, PMReviewType.MYR, false, false);
        checkReviewGroup(PM_REVIEW_DECLINED, PMReviewType.MYR, true, true);
        checkReviewGroup(PM_REVIEW_DECLINED, PMReviewType.MYR, false, true);
        checkReviewGroup(PM_REVIEW_BEFORE_END, PMReviewType.MYR, true, true);
        checkReviewGroup(PM_REVIEW_BEFORE_END, PMReviewType.MYR, false, true);
    }

    @Test
    void checkReviewEYR() throws Exception {
        checkReviewGroup(PM_REVIEW_BEFORE_START, PMReviewType.EYR, true, true);
        checkReviewGroup(PM_REVIEW_BEFORE_START, PMReviewType.EYR, false, true);
        checkReviewGroup(PM_REVIEW_SUBMITTED, PMReviewType.EYR, true, true);
        checkReviewGroup(PM_REVIEW_SUBMITTED, PMReviewType.EYR, false, false);
        checkReviewGroup(PM_REVIEW_APPROVED, PMReviewType.EYR, true, true);
        checkReviewGroup(PM_REVIEW_APPROVED, PMReviewType.EYR, false, true);
        checkReviewGroup(PM_REVIEW_DECLINED, PMReviewType.EYR, true, true);
        checkReviewGroup(PM_REVIEW_DECLINED, PMReviewType.EYR, false, true);
        checkReviewGroup(PM_REVIEW_BEFORE_END, PMReviewType.EYR, true, true);
        checkReviewGroup(PM_REVIEW_BEFORE_END, PMReviewType.EYR, false, true);
    }

    @Test
    void checkObjectives() throws Exception {
        checkObjectivesGroup(ORGANISATION_OBJECTIVES, null, true, WorkLevel.WL1, false);
        checkObjectivesGroup(ORGANISATION_OBJECTIVES, null, true, WorkLevel.WL4, true);

        checkObjectivesGroup(OBJECTIVES_APPROVED_FOR_SHARING, null, true, WorkLevel.WL1, true);
        checkObjectivesGroup(OBJECTIVES_APPROVED_FOR_SHARING, null, false, WorkLevel.WL1, false);
        checkObjectivesGroup(OBJECTIVE_SHARING_START, null, false, WorkLevel.WL1, true);
        checkObjectivesGroup(OBJECTIVE_SHARING_END, null, false, WorkLevel.WL1, true);

    }

    @Test
    void checkFeedbacks() throws Exception {
        checkFeedbackGroup(FEEDBACK_GIVEN, null, false, WorkLevel.WL1, true);
        checkFeedbackGroup(RESPOND_TO_FEEDBACK_REQUESTS, null, false, WorkLevel.WL1, true);
        checkFeedbackGroup(REQUEST_FEEDBACK, null, false, WorkLevel.WL1, true);
    }

    @Test
    void checkTimelineNotifications() throws Exception {

        checkTimelineNotifications(START_TIMELINE_NOTIFICATION, "Q1", true);
        checkTimelineNotifications(START_TIMELINE_NOTIFICATION, "Q3", true);
        checkTimelineNotifications(START_TIMELINE_NOTIFICATION, "Q2", false);
    }

    @Test
    void beforeCycleTest() throws Exception {
        checkCycleGroup(BEFORE_CYCLE_START, null, false, WorkLevel.WL1, true);
        checkCycleGroup(BEFORE_CYCLE_END, null, false, WorkLevel.WL1, true);

        checkCycleGroup(BEFORE_CYCLE_START, null, true, WorkLevel.WL1, true);
        checkCycleGroup(BEFORE_CYCLE_END, null, true, WorkLevel.WL1, true);
    }

    @Test
    void checkReceiveTips() throws Exception {
        checkTipsGroup(RECEIVE_TIPS, null, false, WorkLevel.WL1, true);
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

    void checkTipsGroup(String evenName, PMReviewType reviewType, Boolean isManager, WorkLevel workLevel, boolean send) throws Exception {
        check("InitTipsNotifications", "tips_decision_table", evenName, reviewType, isManager, workLevel, send);
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