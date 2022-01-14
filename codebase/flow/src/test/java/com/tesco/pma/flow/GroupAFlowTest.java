package com.tesco.pma.flow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesco.pma.bpm.camunda.flow.AbstractCamundaSpringBootTest;
import com.tesco.pma.bpm.camunda.flow.CamundaSpringBootTestConfig;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.flow.handlers.PMColleagueCycleHandler;
import com.tesco.pma.flow.handlers.ProcessTimelinePointHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2022-01-14 00:37
 */
@ActiveProfiles("test")
@SpringBootTest(classes = {JacksonAutoConfiguration.class, CamundaSpringBootTestConfig.class})
class GroupAFlowTest extends AbstractCamundaSpringBootTest {

    @Autowired
    ObjectMapper om;

    @MockBean
    PMColleagueCycleHandler pmColleagueCycleHandler;

    @MockBean
    ProcessTimelinePointHandler processTimelinePointHandler;

    @Test
    void success() throws Exception {
        PMCycle pmCycle = om.readValue(this.getClass().getResourceAsStream("group_a_wl3_1.json"), PMCycle.class);
        var pid = runProcess("group_a", Map.of(FlowParameters.PM_CYCLE.name(), pmCycle));
        assertThatForProcess(pid)
                .activity("processColleagueCycles").executedOnce()
                .activity("objectives").executedOnce()
                .activity("q1").executedOnce()
                .activity("q3").executedOnce()
                .activity("midYearReview").executedOnce()
                .activity("endOfYearReview").executedOnce();
    }
}
