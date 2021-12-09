package com.tesco.pma.flow;

import com.tesco.pma.bpm.camunda.flow.AbstractCamundaSpringBootTest;
import com.tesco.pma.bpm.camunda.flow.CamundaSpringBootTestConfig;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleType;
import com.tesco.pma.flow.handlers.ProcessTimelinePointHandler;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2021-11-22 12:33
 */
@ActiveProfiles("test")
@SpringBootTest(classes = {CamundaSpringBootTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class Type1FlowTest extends AbstractCamundaSpringBootTest {

    private static final String PROCESS_ID_TYPE_1 = "type_1_test";
    private static final LocalDateTime CYCLE_START_TIME = LocalDateTime.of(2021, 4, 1, 0, 0);

    @MockBean(name = "processTimelinePointEyr")
    private ProcessTimelinePointHandler processTimelinePointEyr;

    @MockBean(name = "processTimelinePointMyr")
    private ProcessTimelinePointHandler processTimelinePointMyr;

//    @Test
//    @Disabled
//    void checkTimeLinePoint() {
//        assertThatForProcess(runProcess(PROCESS_ID_TYPE_1,
//                Map.of(FlowParameters.PM_CYCLE.name(), getCycle(CYCLE_START_TIME))))
//                .activity("processTimelinePointEyr").executedOnce()
//                .activity("processTimelinePointMyr").executedOnce();
//    }

    private PMCycle getCycle(LocalDateTime cycleStartTime) {
        return PMCycle.builder()
                .type(PMCycleType.FISCAL)
                .startTime(cycleStartTime.toInstant(ZoneOffset.UTC)).build();
    }
}