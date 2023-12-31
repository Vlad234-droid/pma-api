package com.tesco.pma.flow;

import com.tesco.pma.bpm.camunda.flow.AbstractCamundaSpringBootTest;
import com.tesco.pma.bpm.camunda.flow.CamundaSpringBootTestConfig;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleType;
import com.tesco.pma.cycle.api.model.PMReviewElement;
import com.tesco.pma.event.EventSupport;
import com.tesco.pma.flow.handlers.FinalizeFlowHandler;
import com.tesco.pma.flow.handlers.InitTimelinePointHandler;
import com.tesco.pma.flow.handlers.PMColleagueCycleHandler;
import com.tesco.pma.flow.handlers.PMNewColleagueEventHandler;
import com.tesco.pma.flow.handlers.ProcessTimelinePointHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.bpm.camunda.flow.FlowTestUtil.mockExecutionInHandler;
import static com.tesco.pma.cycle.api.model.PMElement.PM_TYPE;
import static com.tesco.pma.flow.FlowParameters.BEFORE_END_DATE;
import static com.tesco.pma.flow.FlowParameters.BEFORE_START_DATE;
import static com.tesco.pma.flow.FlowParameters.END_DATE;
import static com.tesco.pma.flow.FlowParameters.START_DATE;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2021-12-23 23:16
 */
@ActiveProfiles("test")
@SpringBootTest(classes = {CamundaSpringBootTestConfig.class})
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class FiscalFlowTest extends AbstractCamundaSpringBootTest {

    private static final String IMPORT_NEW_COLLEAGUE_EVENT_NAME = "IMPORT_NEW_COLLEAGUE_1";

    private final LocalDate startDate = LocalDate.of(2021, 4, 1);
    private final LocalDate endDate = LocalDate.of(2022, 3, 31);

    @MockBean
    private PMColleagueCycleHandler colleagueCycleHandler;

    @MockBean
    private PMNewColleagueEventHandler newColleagueEventHandler;

    @MockBean
    private FinalizeFlowHandler finalizeFlowHandler;

    @MockBean
    @Qualifier("initTimelinePointEyr")
    private InitTimelinePointHandler initTimelinePointEyr;

    @MockBean
    @Qualifier("initTimelinePointMyr")
    private InitTimelinePointHandler initTimelinePointMyr;

    @MockBean
    @Qualifier("initTimelinePointQ3")
    private InitTimelinePointHandler initTimelinePointQ3;

    @MockBean
    @Qualifier("initTimelinePointQ1")
    private InitTimelinePointHandler initTimelinePointQ1;

    @MockBean
    @Qualifier("initTimelinePointObjective")
    private InitTimelinePointHandler initTimelinePointObjective;

    @MockBean
    private ProcessTimelinePointHandler processTimelinePointHandler;

    @BeforeEach
    void init() throws Exception {
        mockExecutionInHandler(initTimelinePointEyr, (context) -> {
            context.setVariable(BEFORE_START_DATE.name(), "2022-03-02");
            context.setVariable(START_DATE.name(), "2022-03-15");
            context.setVariable(BEFORE_END_DATE.name(), "2022-03-24");
            context.setVariable(END_DATE.name(), "2022-03-31");
        });
    }

    @Test
    void success() throws Exception {
        assertThatForProcess(runProcess("fiscal_test",
                Map.of(FlowParameters.PM_CYCLE.name(), buildPMCycle(),
                        PM_TYPE, PMReviewElement.PM_REVIEW)))
                .activity("initTimelinePointEyr").executedOnce()
                .activity("call_review_schedule_eyr").executedOnce()
                .activity("initTimelinePointMyr").executedOnce()
                .activity("initTimelinePointQ3").executedOnce()
                .activity("initTimelinePointQ1").executedOnce()
                .activity("initTimelinePointObjective").executedOnce();
    }

    @Test
    void importNewColleagueEvent() throws Exception {
        assertThatForProcess(runProcessByEvent(new EventSupport(IMPORT_NEW_COLLEAGUE_EVENT_NAME),
                new HashMap<>(Map.of(FlowParameters.COLLEAGUE_UUID.name(), UUID.randomUUID(),
                        FlowParameters.PM_CYCLE.name(), buildPMCycle(),
                        PM_TYPE, PMReviewElement.PM_REVIEW))))
                .activity("processNewColleague").executedOnce()
                .activity("initTimelinePointEyr").executedOnce()
                .activity("processTimelinePointEyr").executedOnce()
                .activity("initTimelinePointMyr").executedOnce()
                .activity("initTimelinePointQ3").executedOnce()
                .activity("initTimelinePointQ1").executedOnce()
                .activity("initTimelinePointObjective").executedOnce()
                .activity("call_review_schedule_eyr").neverExecuted();
    }

    private PMCycle buildPMCycle() {
        return PMCycle.builder()
                .uuid(UUID.randomUUID())
                .type(PMCycleType.FISCAL)
                .startTime(startDate.atStartOfDay().toInstant(ZoneOffset.UTC))
                .build();
    }
}
