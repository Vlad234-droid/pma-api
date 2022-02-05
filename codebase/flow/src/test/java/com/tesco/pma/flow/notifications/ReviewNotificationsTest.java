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

import java.util.*;

@ActiveProfiles("test")
@SpringBootTest(classes = {CamundaSpringBootTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ReviewNotificationsTest extends AbstractNotificationsFlowTest {

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
        senderColleagueProfile = createColleagueProfile(UUID.randomUUID(), WorkLevel.WL4, Map.of());

        Mockito.when(profileService.findProfileByColleagueUuid(Mockito.eq(colleagueProfile.getColleague().getColleagueUUID())))
                .thenReturn(Optional.of(colleagueProfile));

        Mockito.when(profileService.findProfileByColleagueUuid(Mockito.eq(senderColleagueProfile.getColleague().getColleagueUUID())))
                .thenReturn(Optional.of(senderColleagueProfile));
    }

    @Test
    void reviewSubmittedTest() {
        colleagueProfile.getColleague().getWorkRelationships().get(0).setIsManager(true);

        var event = createEvent(NF_PM_REVIEW_SUBMITTED, PMReviewType.OBJECTIVE.getCode());
        event.putProperty(FlowParameters.COLLEAGUE_UUID.name(), colleagueProfile.getColleague().getColleagueUUID());
        event.putProperty(FlowParameters.SENDER_COLLEAGUE_UUID.name(), senderColleagueProfile.getColleague().getColleagueUUID());

        check(REVIEW_SEND_FLOW, event);
    }

    @Test
    void checReviewMyrApprovalTest() {

        colleagueProfile.getColleague().getWorkRelationships().get(0).setIsManager(true);

        var event = createEvent(NF_PM_REVIEW_APPROVED, PMReviewType.MYR.getCode());
        event.putProperty(FlowParameters.COLLEAGUE_UUID.name(), colleagueProfile.getColleague().getColleagueUUID());
        event.putProperty(FlowParameters.SENDER_COLLEAGUE_UUID.name(), senderColleagueProfile.getColleague().getColleagueUUID());

        check(REVIEW_SEND_FLOW, event);
        Mockito.verify(profileService).findProfileByColleagueUuid(Mockito.eq(senderColleagueProfile.getColleague().getColleagueUUID()));
        checkTitle(NF_PM_REVIEW_APPROVED, NF_PM_REVIEW_APPROVED, "Mid-year approval");
        checkContent(NF_PM_REVIEW_APPROVED, NF_PM_REVIEW_APPROVED, "Mid-year review was approved by Random Name");
    }

//    @Test
//    void checkReviewEYR() throws Exception {
//        checkReviewGroup(NF_PM_REVIEW_BEFORE_START, PMReviewType.EYR, true, true);
//        checkReviewGroup(NF_PM_REVIEW_BEFORE_START, PMReviewType.EYR, false, true);
//        checkReviewGroup(NF_PM_REVIEW_SUBMITTED, PMReviewType.EYR, true, true);
//        checkReviewGroup(NF_PM_REVIEW_SUBMITTED, PMReviewType.EYR, false, false);
//        checkReviewGroup(NF_PM_REVIEW_APPROVED, PMReviewType.EYR, true, true);
//        checkReviewGroup(NF_PM_REVIEW_APPROVED, PMReviewType.EYR, false, true);
//        checkReviewGroup(NF_PM_REVIEW_DECLINED, PMReviewType.EYR, true, true);
//        checkReviewGroup(NF_PM_REVIEW_DECLINED, PMReviewType.EYR, false, true);
//        checkReviewGroup(NF_PM_REVIEW_BEFORE_END, PMReviewType.EYR, true, true);
//        checkReviewGroup(NF_PM_REVIEW_BEFORE_END, PMReviewType.EYR, false, true);
//    }

}
