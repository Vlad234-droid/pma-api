package com.tesco.pma.flow;

import com.tesco.pma.bpm.camunda.flow.AbstractCamundaSpringBootTest;
import com.tesco.pma.bpm.camunda.flow.CamundaSpringBootTestConfig;
import com.tesco.pma.event.EventSupport;
import com.tesco.pma.flow.handlers.AbstractFlowHandler;
import com.tesco.pma.flow.handlers.CaseFlowHandler;
import com.tesco.pma.flow.handlers.InitFlowHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static com.tesco.pma.bpm.camunda.flow.FlowTestUtil.mockExecutionInHandler;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 15.06.2021 Time: 17:37
 */
@ActiveProfiles("test")
@SpringBootTest(classes = {CamundaSpringBootTestConfig.class})
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class DemoFlowTest extends AbstractCamundaSpringBootTest {

    public static final String EP_IS_DIRECT = "IS_DIRECT";

    @MockBean
    private InitFlowHandler initTask;
    @MockBean(name = "caseYes")
    private CaseFlowHandler caseYes;
    @MockBean(name = "caseNo")
    private CaseFlowHandler caseNo;

    private enum Events {
        E_START_1
    }

    @Test
    void success() throws Exception {
        mockExecutionInHandler(initTask, (context) -> {
            context.setVariable(AbstractFlowHandler.Params.IS_DIRECT.name(), true);
        });

        assertThatForProcess(runProcessByEvent(new EventSupport(Events.E_START_1)))
                .activity("initTask").executedOnce()
                .activity("caseYes").executedOnce()
                .activity("caseNo").neverExecuted();
    }

}
