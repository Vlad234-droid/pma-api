package com.tesco.pma.flow.notifications;

import com.tesco.pma.bpm.camunda.flow.CamundaSpringBootTestConfig;
import com.tesco.pma.colleague.api.workrelationships.WorkLevel;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.flow.FlowParameters;
import com.tesco.pma.flow.notifications.handlers.InitTimelinePointNotificationHandler;
import com.tesco.pma.flow.notifications.handlers.SendNotificationHandler;
import com.tesco.pma.flow.notifications.service.ColleagueInboxNotificationSender;
import com.tesco.pma.fs.service.FileService;
import com.tesco.pma.review.dao.TimelinePointDAO;
import com.tesco.pma.service.colleague.inbox.client.ColleagueInboxApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ActiveProfiles("test")
@SpringBootTest(classes = {CamundaSpringBootTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ReviewNotificationsTest extends AbstractNotificationsFlowTest {

    private static final String NF_PM_REVIEW_SUBMITTED = "NF_PM_REVIEW_SUBMITTED";
    private static final String NF_PM_REVIEW_APPROVED = "NF_PM_REVIEW_APPROVED";
    private static final String NF_PM_REVIEW_DECLINED = "NF_PM_REVIEW_DECLINED";
    private static final String NF_PM_REVIEW_BEFORE_START = "NF_PM_REVIEW_BEFORE_START";
    private static final String NF_PM_REVIEW_BEFORE_END = "NF_PM_REVIEW_BEFORE_END";

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

        check(Map.of(
                "initReviewNotification", 1,
                "review_decision_table", 1,
                "sendNotification", 1
        ), event);
    }

}
