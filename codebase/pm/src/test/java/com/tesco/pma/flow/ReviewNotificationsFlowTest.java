package com.tesco.pma.flow;

import com.tesco.pma.bpm.camunda.flow.AbstractCamundaSpringBootTest;
import com.tesco.pma.bpm.camunda.flow.CamundaSpringBootTestConfig;
import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.event.EventSupport;
import com.tesco.pma.flow.handlers.InitReviewNotification;
import com.tesco.pma.flow.handlers.SendNotification;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2021-11-20 00:03
 */
@ActiveProfiles("test")
@SpringBootTest(classes = {CamundaSpringBootTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ReviewNotificationsFlowTest extends AbstractCamundaSpringBootTest {

    public static final String DECISION_TABLE_TASK = "decision_table";

    public static final String PM_REVIEW_SUBMITTED = "PM_REVIEW_SUBMITTED";
    public static final String PM_REVIEW_APPROVED = "PM_REVIEW_APPROVED";
    public static final String PM_REVIEW_DECLINED = "PM_REVIEW_DECLINED";
    public static final String PM_REVIEW_BEFORE_START = "PM_REVIEW_BEFORE_START";
    public static final String PM_REVIEW_BEFORE_END = "PM_REVIEW_BEFORE_END";

    @SpyBean(name = "initReviewNotification")
    private InitReviewNotification initTask;

    @MockBean(name = "sendNotification")
    private SendNotification sendNotification;

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

    void check(String evenName, PMReviewType reviewType, Boolean isManager, boolean send) throws Exception {
        Mockito.when(initTask.getReviewType()).thenReturn(reviewType);
        Mockito.when(initTask.isManager()).thenReturn(isManager);

        assertThatForProcess(runProcessByEvent(new EventSupport(evenName)))
                .activity("initReviewNotification").executedOnce()
                .activity(DECISION_TABLE_TASK).executedOnce()
                .activity("sendNotification").executedTimes(send ? 1 : 0);
    }
}