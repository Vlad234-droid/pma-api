package com.tesco.pma.flow.notifications;

import com.tesco.pma.bpm.camunda.flow.CamundaSpringBootTestConfig;
import com.tesco.pma.colleague.api.workrelationships.WorkLevel;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.flow.notifications.handlers.InitTimelinePointNotificationHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;

@ActiveProfiles("test")
@SpringBootTest(classes = {CamundaSpringBootTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ReviewNotificationsTest extends AbstractNotificationsFlowTest {

    private static final String NF_PM_REVIEW_SUBMITTED = "NF_PM_REVIEW_SUBMITTED";
    private static final String NF_PM_REVIEW_APPROVED = "NF_PM_REVIEW_APPROVED";
    private static final String NF_PM_REVIEW_DECLINED = "NF_PM_REVIEW_DECLINED";
    private static final String NF_PM_REVIEW_BEFORE_START = "NF_PM_REVIEW_BEFORE_START";
    private static final String NF_PM_REVIEW_BEFORE_END = "NF_PM_REVIEW_BEFORE_END";

    private static final Map<String, Integer> REVIEW_SEND_FLOW = Map.of(
            "initReviewNotification", 1,
            "review_decision_table", 1,
            "sendNotification", 1);

    @SpyBean
    private InitTimelinePointNotificationHandler reviewInitHandler;


    private ColleagueProfile colleagueProfile;
    private ColleagueProfile senderColleagueProfile;


    @BeforeEach
    void init() {
        colleagueProfile = createColleagueProfile(UUID.randomUUID(), WorkLevel.WL1, Map.of());
        senderColleagueProfile = createColleagueProfile(UUID.randomUUID(), "Mr", "Sender", WorkLevel.WL4, Map.of());

        Mockito.when(profileService.findProfileByColleagueUuid(colleagueProfile.getColleague().getColleagueUUID()))
                .thenReturn(Optional.of(colleagueProfile));

        Mockito.when(profileService.findProfileByColleagueUuid(senderColleagueProfile.getColleague().getColleagueUUID()))
                .thenReturn(Optional.of(senderColleagueProfile));
    }

    @Test
    void reviewSubmittedTest() {
        colleagueProfile.getColleague().getWorkRelationships().get(0).setIsManager(true);

        var event = createEvent(NF_PM_REVIEW_SUBMITTED, PMReviewType.MYR.getCode());
        event.putProperty(FlowParameters.COLLEAGUE_UUID.name(), colleagueProfile.getColleague().getColleagueUUID());
        event.putProperty(FlowParameters.SENDER_COLLEAGUE_UUID.name(), senderColleagueProfile.getColleague().getColleagueUUID());

        check(REVIEW_SEND_FLOW, event);

        checkContent("Mid-year review was submitted by Mr Sender");
    }

    @Test
    void checReviewMyrApprovalTest() {

        colleagueProfile.getColleague().getWorkRelationships().get(0).setIsManager(true);

        var event = createEvent(NF_PM_REVIEW_APPROVED, PMReviewType.MYR.getCode());
        event.putProperty(FlowParameters.COLLEAGUE_UUID.name(), colleagueProfile.getColleague().getColleagueUUID());
        event.putProperty(FlowParameters.SENDER_COLLEAGUE_UUID.name(), senderColleagueProfile.getColleague().getColleagueUUID());

        check(REVIEW_SEND_FLOW, event);

        checkTitle("Mid-year review approval");
        checkContent("Mid-year review was approved by Mr Sender");
    }

    @Test
    void reviewBeforeEndTest() {

        String weekAgoDate = getDateSevenDaysAgo("yyyy-MM-dd");
        var timelinePoint = createTimelinePoint(PMReviewType.MYR.getCode(), weekAgoDate);
        var event = createEvent(NF_PM_REVIEW_BEFORE_END, timelinePoint);
        event.putProperty(FlowParameters.COLLEAGUE_UUID.name(), colleagueProfile.getColleague().getColleagueUUID());
        event.putProperty(FlowParameters.SENDER_COLLEAGUE_UUID.name(), senderColleagueProfile.getColleague().getColleagueUUID());

        check(REVIEW_SEND_FLOW, event);

        checkContent("Kind reminder, the review is due to be closed in 7 days (" + getDateSevenDaysAgo("dd.MM.yyyy") + ")");
    }
}
