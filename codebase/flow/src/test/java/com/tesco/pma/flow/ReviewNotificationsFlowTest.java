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
import com.tesco.pma.event.EventSupport;
import com.tesco.pma.flow.handlers.FlowParameters;
import com.tesco.pma.flow.handlers.InitReviewNotification;
import com.tesco.pma.flow.handlers.SendNotification;
import com.tesco.pma.service.contact.client.ContactApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ReviewNotificationsFlowTest extends AbstractCamundaSpringBootTest {

    private static final String DECISION_TABLE_TASK = "decision_table";

    private static final String PM_REVIEW_SUBMITTED = "PM_REVIEW_SUBMITTED";
    private static final String PM_REVIEW_APPROVED = "PM_REVIEW_APPROVED";
    private static final String PM_REVIEW_DECLINED = "PM_REVIEW_DECLINED";
    private static final String PM_REVIEW_BEFORE_START = "PM_REVIEW_BEFORE_START";
    private static final String PM_REVIEW_BEFORE_END = "PM_REVIEW_BEFORE_END";
    private static final String ORGANISATION_OBJECTIVES = "ORGANISATION_OBJECTIVES";
    private static final String LM_OBJECTIVES_APPROVED_FOR_SHARING = "LM_OBJECTIVES_APPROVED_FOR_SHARING";
    private static final String LM_SHARING_START = "LM_SHARING_START";
    private static final String LM_SHARING_END = "LM_SHARING_START";
    private static final String FEEDBACK_GIVEN = "FEEDBACK_GIVEN";
    private static final String RESPOND_TO_FEEDBACK_REQUESTS = "RESPOND_TO_FEEDBACK_REQUESTS";
    private static final String REQUEST_FEEDBACK = "REQUEST_FEEDBACK";

    @SpyBean(name = "initReviewNotification")
    private InitReviewNotification initTask;

    @MockBean(name = "sendNotification")
    private SendNotification sendNotification;

    @MockBean
    private ContactApiClient contactApiClient;

    @MockBean
    private ProfileService profileService;

    @MockBean
    private NamedMessageSourceAccessor messageSourceAccessor;

    private UUID colleagueUUID = UUID.randomUUID();
    private ColleagueProfile colleagueProfile;

    @BeforeEach
    public void init(){
        colleagueProfile = createColleagueProfile(colleagueUUID, WorkLevel.WL1, Map.of());

        Mockito.when(profileService.findProfileByColleagueUuid(Mockito.any()))
                .thenReturn(Optional.of(colleagueProfile));
    }

    @Test
    void checkObjectives() throws Exception {
        check(PM_REVIEW_SUBMITTED, PMReviewType.OBJECTIVE, true, true);
        check(PM_REVIEW_SUBMITTED, PMReviewType.OBJECTIVE, false, false);
        check(PM_REVIEW_APPROVED, PMReviewType.OBJECTIVE, true, true);
        check(PM_REVIEW_APPROVED, PMReviewType.OBJECTIVE, false, true);
        check(PM_REVIEW_DECLINED, PMReviewType.OBJECTIVE, true, true);
        check(PM_REVIEW_DECLINED, PMReviewType.OBJECTIVE, false, true);
    }

    @Test
    void checkMYR() throws Exception {
        check(PM_REVIEW_BEFORE_START, PMReviewType.MYR, true, true);
        check(PM_REVIEW_BEFORE_START, PMReviewType.MYR, false, true);
        check(PM_REVIEW_SUBMITTED, PMReviewType.MYR, true, true);
        check(PM_REVIEW_SUBMITTED, PMReviewType.MYR, false, false);
        check(PM_REVIEW_APPROVED, PMReviewType.MYR, true, true);
        check(PM_REVIEW_APPROVED, PMReviewType.MYR, false, false);
        check(PM_REVIEW_DECLINED, PMReviewType.MYR, true, true);
        check(PM_REVIEW_DECLINED, PMReviewType.MYR, false, true);
        check(PM_REVIEW_BEFORE_END, PMReviewType.MYR, true, true);
        check(PM_REVIEW_BEFORE_END, PMReviewType.MYR, false, true);
    }

    @Test
    void checkEYR() throws Exception {
        check(PM_REVIEW_BEFORE_START, PMReviewType.EYR, true, true);
        check(PM_REVIEW_BEFORE_START, PMReviewType.EYR, false, true);
        check(PM_REVIEW_SUBMITTED, PMReviewType.EYR, true, true);
        check(PM_REVIEW_SUBMITTED, PMReviewType.EYR, false, false);
        check(PM_REVIEW_APPROVED, PMReviewType.EYR, true, true);
        check(PM_REVIEW_APPROVED, PMReviewType.EYR, false, true);
        check(PM_REVIEW_DECLINED, PMReviewType.EYR, true, true);
        check(PM_REVIEW_DECLINED, PMReviewType.EYR, false, true);
        check(PM_REVIEW_BEFORE_END, PMReviewType.EYR, true, true);
        check(PM_REVIEW_BEFORE_END, PMReviewType.EYR, false, true);
    }

    @Test
    void checkOrganisationObjectives() throws Exception {
        check(ORGANISATION_OBJECTIVES, null, true, WorkLevel.WL1, false);
        check(ORGANISATION_OBJECTIVES, null, true, WorkLevel.WL4, true);
    }

    @Test
    void checkLM() throws Exception {
        check(LM_OBJECTIVES_APPROVED_FOR_SHARING, null, true, WorkLevel.WL1, true);
        check(LM_OBJECTIVES_APPROVED_FOR_SHARING, null, false, WorkLevel.WL1, false);
        check(LM_SHARING_START, null, false, WorkLevel.WL1, true);
        check(LM_SHARING_END, null, false, WorkLevel.WL1, true);
    }

    @Test
    void checkFeedbacks() throws Exception {
        check(FEEDBACK_GIVEN, null, false, WorkLevel.WL1, true);
        check(RESPOND_TO_FEEDBACK_REQUESTS, null, false, WorkLevel.WL1, true);
        check(REQUEST_FEEDBACK, null, false, WorkLevel.WL1, true);
    }

    void check(String evenName, PMReviewType reviewType, Boolean isManager, boolean send) throws Exception {
        check(evenName, reviewType, isManager, null, send);
    }

    void check(String evenName, PMReviewType reviewType, Boolean isManager, WorkLevel workLevel, boolean send) throws Exception {

        var event = new EventSupport(evenName);

        if(reviewType != null) {
            event.putProperty(FlowParameters.REVIEW_TYPE.name(), reviewType);
        }

        event.putProperty(FlowParameters.COLLEAGUE_UUID.name(), colleagueUUID);

        colleagueProfile.getColleague().getWorkRelationships().get(0).setIsManager(isManager);

        if(workLevel!=null) {
            colleagueProfile.getColleague().getWorkRelationships().get(0).setWorkLevel(workLevel);
        }

        assertThatForProcess(runProcessByEvent(event))
                .activity("initReviewNotification").executedOnce()
                .activity(DECISION_TABLE_TASK).executedOnce()
                .activity("sendNotification").executedTimes(send ? 1 : 0);
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
}