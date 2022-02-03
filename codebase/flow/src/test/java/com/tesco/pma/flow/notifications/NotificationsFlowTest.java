package com.tesco.pma.flow.notifications;

import com.tesco.pma.bpm.camunda.flow.AbstractCamundaSpringBootTest;
import com.tesco.pma.bpm.camunda.flow.CamundaSpringBootTestConfig;
import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.api.Profile;
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
import com.tesco.pma.flow.notifications.handlers.InitTimelinePointNotificationHandler;
import com.tesco.pma.flow.notifications.handlers.InitFeedbacksNotificationHandler;
import com.tesco.pma.flow.notifications.handlers.InitTipsNotificationHandler;
import com.tesco.pma.flow.notifications.handlers.SendNotificationHandler;
import com.tesco.pma.flow.notifications.service.ColleagueInboxNotificationSender;
import com.tesco.pma.fs.service.FileService;
import com.tesco.pma.review.dao.TimelinePointDAO;
import com.tesco.pma.review.domain.TimelinePoint;
import com.tesco.pma.service.colleague.inbox.client.ColleagueInboxApiClient;
import com.tesco.pma.service.contact.client.ContactApiClient;
import com.tesco.pma.tip.api.Tip;
import com.tesco.pma.tip.service.TipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;


import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;


import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2021-11-20 00:03
 */
@ActiveProfiles("test")
@SpringBootTest(classes = {CamundaSpringBootTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class NotificationsFlowTest extends AbstractCamundaSpringBootTest {

    private static final String NF_PM_REVIEW_SUBMITTED = "NF_PM_REVIEW_SUBMITTED";
    private static final String NF_PM_REVIEW_APPROVED = "NF_PM_REVIEW_APPROVED";
    private static final String NF_PM_REVIEW_DECLINED = "NF_PM_REVIEW_DECLINED";
    private static final String NF_PM_REVIEW_BEFORE_START = "NF_PM_REVIEW_BEFORE_START";
    private static final String NF_PM_REVIEW_BEFORE_END = "NF_PM_REVIEW_BEFORE_END";

    private static final String NF_ORGANISATION_OBJECTIVES = "NF_ORGANISATION_OBJECTIVES";
    private static final String NF_OBJECTIVES_APPROVED_FOR_SHARING = "NF_OBJECTIVES_APPROVED_FOR_SHARING";
    private static final String NF_OBJECTIVE_SHARING_START = "NF_OBJECTIVE_SHARING_START";
    private static final String NF_OBJECTIVE_SHARING_END = "NF_OBJECTIVE_SHARING_END";

    private static final String NF_FEEDBACK_GIVEN = "NF_FEEDBACK_GIVEN";
    private static final String NF_RESPOND_TO_FEEDBACK_REQUESTS = "NF_FEEDBACK_REQUESTS_RESPONDED";
    private static final String NF_REQUEST_FEEDBACK = "NF_FEEDBACK_REQUESTED";

    private static final String NF_BEFORE_CYCLE_START = "NF_BEFORE_CYCLE_START";
    private static final String NF_BEFORE_CYCLE_END = "NF_BEFORE_CYCLE_END";
    private static final String NF_START_TIMELINE_NOTIFICATION = "NF_START_TIMELINE_NOTIFICATION";

    private static final String NF_RECEIVE_TIPS = "NF_TIPS_RECEIVED";

    private static final String SEND_NOTIFICATION_HANDLER_ID = "sendNotification";

    @SpyBean
    private DefaultInitNotificationHandler initNotificationHandler;

    @SpyBean
    private InitTimelinePointNotificationHandler reviewInitHandler;

    @SpyBean
    private InitFeedbacksNotificationHandler feedbacksNotificationHandler;

    @SpyBean
    private InitTipsNotificationHandler initTipsNotificationHandler;

    @SpyBean
    private SendNotificationHandler sendNotificationHandler;

    @MockBean
    private ContactApiClient contactApiClient;

    @MockBean
    private ProfileService profileService;

    @MockBean
    private NamedMessageSourceAccessor messageSourceAccessor;

    @MockBean
    private TimelinePointDAO timelinePointDAO;

    @MockBean
    private TipService tipService;

    @SpyBean
    private ColleagueInboxNotificationSender colleagueInboxNotificationSender;

    @MockBean
    private ColleagueInboxApiClient colleagueInboxApiClient;

    @MockBean
    private FileService fileService;

    private final UUID sourceColleagueUUID = UUID.randomUUID();
    private ColleagueProfile colleagueProfile;

    @BeforeEach
    public void init() throws Exception {
        colleagueProfile = createColleagueProfile(UUID.randomUUID(), WorkLevel.WL1, Map.of());

        Mockito.when(profileService.findProfileByColleagueUuid(Mockito.eq(colleagueProfile.getColleague().getColleagueUUID())))
                .thenReturn(Optional.of(colleagueProfile));

        Mockito.when(profileService.findProfileByColleagueUuid(Mockito.eq(sourceColleagueUUID)))
                .thenReturn(Optional.of(createColleagueProfile(sourceColleagueUUID, WorkLevel.WL1, new HashMap<>())));
    }

    @Test
    void checkReviewObjectives() throws Exception {
        checkReviewGroup(NF_PM_REVIEW_SUBMITTED, PMReviewType.OBJECTIVE, true, true);
        checkReviewGroup(NF_PM_REVIEW_SUBMITTED, PMReviewType.OBJECTIVE, false, false);
        checkReviewGroup(NF_PM_REVIEW_APPROVED, PMReviewType.OBJECTIVE, true, true);
        checkReviewGroup(NF_PM_REVIEW_APPROVED, PMReviewType.OBJECTIVE, false, true);
        checkReviewGroup(NF_PM_REVIEW_DECLINED, PMReviewType.OBJECTIVE, true, true);
        checkReviewGroup(NF_PM_REVIEW_DECLINED, PMReviewType.OBJECTIVE, false, true);
    }

    @Test
    void checReviewMYR() throws Exception {
        checkReviewGroup(NF_PM_REVIEW_BEFORE_START, PMReviewType.MYR, true, true);
        checkReviewGroup(NF_PM_REVIEW_BEFORE_START, PMReviewType.MYR, false, true);
        checkReviewGroup(NF_PM_REVIEW_SUBMITTED, PMReviewType.MYR, true, true);
        checkReviewGroup(NF_PM_REVIEW_SUBMITTED, PMReviewType.MYR, false, false);
        checkReviewGroup(NF_PM_REVIEW_APPROVED, PMReviewType.MYR, true, true);
        checkReviewGroup(NF_PM_REVIEW_APPROVED, PMReviewType.MYR, false, false);
        checkReviewGroup(NF_PM_REVIEW_DECLINED, PMReviewType.MYR, true, true);
        checkReviewGroup(NF_PM_REVIEW_DECLINED, PMReviewType.MYR, false, true);
        checkReviewGroup(NF_PM_REVIEW_BEFORE_END, PMReviewType.MYR, true, true);
        checkReviewGroup(NF_PM_REVIEW_BEFORE_END, PMReviewType.MYR, false, true);
    }

    @Test
    void checReviewMyrApproval() throws Exception {
        checkReviewGroup(NF_PM_REVIEW_APPROVED, PMReviewType.MYR, true, true);

        checkTitle(NF_PM_REVIEW_APPROVED, NF_PM_REVIEW_APPROVED, "Mid-year approval");
        checkContent(NF_PM_REVIEW_APPROVED, NF_PM_REVIEW_APPROVED, "Mid-year review was approved by Random Name");
    }

    @Test
    void checkReviewEYR() throws Exception {
        checkReviewGroup(NF_PM_REVIEW_BEFORE_START, PMReviewType.EYR, true, true);
        checkReviewGroup(NF_PM_REVIEW_BEFORE_START, PMReviewType.EYR, false, true);
        checkReviewGroup(NF_PM_REVIEW_SUBMITTED, PMReviewType.EYR, true, true);
        checkReviewGroup(NF_PM_REVIEW_SUBMITTED, PMReviewType.EYR, false, false);
        checkReviewGroup(NF_PM_REVIEW_APPROVED, PMReviewType.EYR, true, true);
        checkReviewGroup(NF_PM_REVIEW_APPROVED, PMReviewType.EYR, false, true);
        checkReviewGroup(NF_PM_REVIEW_DECLINED, PMReviewType.EYR, true, true);
        checkReviewGroup(NF_PM_REVIEW_DECLINED, PMReviewType.EYR, false, true);
        checkReviewGroup(NF_PM_REVIEW_BEFORE_END, PMReviewType.EYR, true, true);
        checkReviewGroup(NF_PM_REVIEW_BEFORE_END, PMReviewType.EYR, false, true);
    }

    @Test
    void checkObjectives() throws Exception {
        checkObjectivesGroup(NF_ORGANISATION_OBJECTIVES, true, WorkLevel.WL1, false);
        checkObjectivesGroup(NF_ORGANISATION_OBJECTIVES,  true, WorkLevel.WL4, true);

        checkObjectivesGroup(NF_OBJECTIVES_APPROVED_FOR_SHARING,  true, WorkLevel.WL1, true);
        checkObjectivesGroup(NF_OBJECTIVES_APPROVED_FOR_SHARING,  false, WorkLevel.WL1, false);
        checkObjectivesGroup(NF_OBJECTIVE_SHARING_START,  false, WorkLevel.WL1, true);
        checkObjectivesGroup(NF_OBJECTIVE_SHARING_END,  false, WorkLevel.WL1, true);

    }

    @Test
    void checkFeedbacks() throws Exception {
        checkFeedbackGroup(NF_FEEDBACK_GIVEN);
        checkFeedbackGroup(NF_RESPOND_TO_FEEDBACK_REQUESTS);
        checkFeedbackGroup(NF_REQUEST_FEEDBACK);
    }

    @Test
    void checkTimelineNotifications() throws Exception {
        checkTimelineNotifications(NF_START_TIMELINE_NOTIFICATION, "Q1", true);
        checkTimelineNotifications(NF_START_TIMELINE_NOTIFICATION, "Q3", true);
        checkTimelineNotifications(NF_START_TIMELINE_NOTIFICATION, "Q2", false);
    }

    @Test
    void beforeCycleStartTest() throws Exception {
        var initCycleNotifications = "InitCycleNotifications";
        var cycleDmn = "cycle_decision_table";
        check(initCycleNotifications, cycleDmn, NF_BEFORE_CYCLE_START, PMReviewType.EYR, false, WorkLevel.WL1, true);
        check(initCycleNotifications, cycleDmn, NF_BEFORE_CYCLE_START, PMReviewType.EYR, true, WorkLevel.WL1, true);

        checkContent(NF_BEFORE_CYCLE_START, NF_BEFORE_CYCLE_START, "Next performance cycle starts tomorrow!");
    }

    @Test
    void beforeCycleEndTest() throws Exception {
        var initCycleNotifications = "InitCycleNotifications";
        var cycleDmn = "cycle_decision_table";
        check(initCycleNotifications, cycleDmn, NF_BEFORE_CYCLE_END, PMReviewType.EYR, false, WorkLevel.WL1, true);
        check(initCycleNotifications, cycleDmn, NF_BEFORE_CYCLE_END, PMReviewType.EYR, true, WorkLevel.WL1, true);

        checkContent(NF_BEFORE_CYCLE_END, NF_BEFORE_CYCLE_END, "Current performance cycle ends tomorrow!");
    }

    @Test
    void checkReceiveTips() throws Exception {
        checkTipsGroup(NF_RECEIVE_TIPS, false, WorkLevel.WL1, true);
    }

    void checkReviewGroup(String evenName, PMReviewType reviewType, Boolean isManager, boolean send) throws Exception {
        check("initReviewNotification", "review_decision_table", evenName, reviewType, isManager, null, send);
    }

    void checkObjectivesGroup(String evenName, Boolean isManager, WorkLevel workLevel, boolean send) throws Exception {
        var event = createEvent(evenName, null);
        colleagueProfile.getColleague().getWorkRelationships().get(0).setIsManager(isManager);
        colleagueProfile.getColleague().getWorkRelationships().get(0).setWorkLevel(workLevel);

        check(Map.of(
                "InitObjectivesNotifications", 1,
                "objectives_decision_table", 1,
                SEND_NOTIFICATION_HANDLER_ID, send? 1: 0
        ), event);

        if (send) {
            checkContent(evenName, NF_ORGANISATION_OBJECTIVES, "Please check the organisation objectives for the year");
        }

    }

    void checkFeedbackGroup(String evenName){
        var event = createEvent(evenName, null);
        event.putProperty(FlowParameters.SOURCE_COLLEAGUE_UUID.name(), sourceColleagueUUID);

        check(Map.of(
                "InitFeedbacksNotifications", 1,
                "feedbacks_decision_table", 1,
                "feedbacks_placeholders_mapping", 1,
                SEND_NOTIFICATION_HANDLER_ID, 1
        ), event);

        checkContent(evenName, NF_FEEDBACK_GIVEN, "You have feedback from Random Name");
    }

    void checkTimelineNotifications(String evenName, String quarter, boolean send) throws Exception {
        var event = createEvent(evenName, quarter);
        check("InitCycleNotifications", "cycle_decision_table", event, false, null, send);
    }

    void checkTipsGroup(String evenName, Boolean isManager, WorkLevel workLevel, boolean send) throws Exception {
        var event = createEvent(evenName, null);
        event.putProperty(FlowParameters.TIP_UUID.name(), UUID.randomUUID());

        var tip = new Tip();
        var tipTitle = "test title";
        tip.setTitle(tipTitle);

        Mockito.when(tipService.findOne(Mockito.any())).thenReturn(tip);

        check(Map.of("InitTipsNotifications", 1, "tips_decision_table", 1, "sendTipsNotification", 1), event);

        Mockito.verify(colleagueInboxNotificationSender).send(Mockito.any(), Mockito.any(), Mockito.argThat(placeholders ->
            tipTitle.equals(placeholders.get("CONTENT"))
        ));
    }

    void check(String initHandlerName, String decisionTable, String evenName, PMReviewType reviewType, Boolean isManager, WorkLevel workLevel, boolean send) throws Exception {
        var event = createEvent(evenName, reviewType != null? reviewType.getCode(): null);
        check(initHandlerName, decisionTable, event, isManager, workLevel, send);
    }

    void check(String initHandlerName, String decisionTable, Event event, boolean isManager, WorkLevel workLevel, boolean send) throws Exception {

        colleagueProfile.getColleague().getWorkRelationships().get(0).setIsManager(isManager);

        if(workLevel!=null) {
            colleagueProfile.getColleague().getWorkRelationships().get(0).setWorkLevel(workLevel);
        }

        check(Map.of(initHandlerName, 1, decisionTable, 1, "sendNotification", send ? 1 : 0), event);
    }

    void check(Map<String, Integer> execBlocks, Event event) {
        var assertion = assertThatForProcess(runProcessByEvent(event));
        execBlocks.forEach((block, execTimes) -> assertion.activity(block).executedTimes(execTimes));
    }

    void checkContent(String eventName, String eventNameExpected, String content){
        if (!eventName.equals(eventNameExpected)) {
            return;
        }

        Mockito.verify(colleagueInboxApiClient, Mockito.atLeastOnce()).sendNotification(Mockito.argThat(msg -> {
                assertEquals(content, msg.getContent());
                return true;
            }));
    }

    void checkTitle(String eventName, String eventNameExpected, String title){
        if (!eventName.equals(eventNameExpected)) {
            return;
        }

        Mockito.verify(colleagueInboxApiClient, Mockito.atLeastOnce()).sendNotification(Mockito.argThat(msg -> {
            assertEquals(title, msg.getTitle());
            return true;
        }));
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

        event.putProperty(FlowParameters.COLLEAGUE_UUID.name(), colleagueProfile.getColleague().getColleagueUUID());
        return event;
    }



    private ColleagueProfile createColleagueProfile(UUID colleagueUUID, WorkLevel wl, Map<String, String> attrs){
        var wr = new WorkRelationship();
        wr.setWorkLevel(wl);
        wr.setIsManager(false);

        var profile = new Profile();
        profile.setFirstName("Random");
        profile.setLastName("Name");

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