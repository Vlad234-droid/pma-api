package com.tesco.pma.flow;

import com.tesco.pma.bpm.camunda.flow.AbstractCamundaSpringBootTest;
import com.tesco.pma.bpm.camunda.flow.CamundaSpringBootTestConfig;
import com.tesco.pma.flow.handlers.FinalizeFlowHandler;
import com.tesco.pma.flow.handlers.InitTimelinePointHandler;
import com.tesco.pma.flow.handlers.PMColleagueCycleHandler;
import com.tesco.pma.flow.handlers.ProcessTimelinePointHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static com.tesco.pma.bpm.camunda.flow.FlowTestUtil.mockExecutionInHandler;
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
public class FiscalFlowTest extends AbstractCamundaSpringBootTest {

    @MockBean
    private PMColleagueCycleHandler colleagueCycleHandler;

    @MockBean
    private FinalizeFlowHandler finalizeFlowHandler;

    @MockBean
    private InitTimelinePointHandler initTimelinePointHandler;

    @MockBean
    private ProcessTimelinePointHandler processTimelinePointHandler;

    @Test
    void success() throws Exception {
        mockExecutionInHandler(initTimelinePointHandler, (context) -> {
            context.setVariable(BEFORE_START_DATE.name(), "2021-12-01");
            context.setVariable(START_DATE.name(), "2021-12-14");
            context.setVariable(BEFORE_END_DATE.name(), "2021-12-21");
            context.setVariable(END_DATE.name(), "2021-12-28");
        });

        assertThatForProcess(runProcess("fiscal_test"))
                .activity("call_review_schedule_eyr").executedOnce();
    }
}
