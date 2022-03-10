package com.tesco.pma.flow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesco.pma.bpm.camunda.flow.AbstractCamundaSpringBootTest;
import com.tesco.pma.bpm.camunda.flow.CamundaSpringBootTestConfig;
import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.api.workrelationships.WorkLevel;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.flow.handlers.EventSendHandler;
import com.tesco.pma.flow.handlers.FindCycleHandler;
import com.tesco.pma.util.TestUtils.KEYS;
import com.tesco.pma.flow.handlers.ReadColleaguesHandler;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.tesco.pma.bpm.camunda.flow.FlowTestUtil.mockExecutionInHandler;
import static com.tesco.pma.flow.exception.ErrorCodes.PM_CYCLE_MORE_THAN_ONE_IN_STATUSES;
import static com.tesco.pma.util.TestUtils.createColleague;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2022-02-12 20:42
 */
@ActiveProfiles("test")
@SpringBootTest(classes = {JacksonAutoConfiguration.class, CamundaSpringBootTestConfig.class})
public class PMCycleAssignmentFlowTest extends AbstractCamundaSpringBootTest {
    private static final String PROCESS_KEY = "pm_cycle_assignment";
    private static final String READ_COLLEAGUES = "read_colleagues";
    private static final String CALCULATE_CYCLE = "calculate_cycle";
    private static final String FIND_CYCLE = "find_cycle";
    private static final String SEND_EVENT = "send_event";
    private static final String COUNT_DOWN = "count_down";

    @Autowired
    ObjectMapper om;

    @MockBean
    ReadColleaguesHandler readColleaguesHandler;

    @MockBean
    FindCycleHandler findCycleHandler;

    @MockBean
    EventSendHandler eventSendHandler;

    @Test
    void successMany() throws Exception {
        successCount(0);
        successCount(1);
        successCount(3);
    }

    @Test
    void cannotFindPMCycle() throws Exception {
        var colleagues = getColleagues(1);
        var uuids = colleagues.stream().map(c -> c.getColleagueUUID().toString()).collect(Collectors.toList());
        mockExecutionInHandler(readColleaguesHandler,
                (context) -> context.setVariable(FlowParameters.COLLEAGUES.name(), colleagues));

        doThrow(new BpmnError(PM_CYCLE_MORE_THAN_ONE_IN_STATUSES.getCode())).when(findCycleHandler).execute(any());

        var pid = runProcess(PROCESS_KEY, Map.of(FlowParameters.COLLEAGUE_UUIDS.name(), uuids));
        assertThatForProcess(pid)
                .activity(READ_COLLEAGUES).executedOnce()
                .activity(CALCULATE_CYCLE).executedOnce()
                .activity(FIND_CYCLE).executedOnce()
                .activity(SEND_EVENT).neverExecuted()
                .activity(COUNT_DOWN).executedOnce();
    }

    private void successCount(int count) throws Exception {
        var colleagues = getColleagues(count);
        var uuids = colleagues.stream().map(c -> c.getColleagueUUID().toString()).collect(Collectors.toList());
        mockExecutionInHandler(readColleaguesHandler,
                (context) -> context.setVariable(FlowParameters.COLLEAGUES.name(), colleagues));

        mockExecutionInHandler(findCycleHandler,
                (context) -> context.setVariable(FlowParameters.PM_CYCLE.name(), buildCycle()));

        var pid = runProcess(PROCESS_KEY, Map.of(FlowParameters.COLLEAGUE_UUIDS.name(), uuids));
        assertThatForProcess(pid)
                .activity(READ_COLLEAGUES).executedOnce()
                .activity(CALCULATE_CYCLE).executedTimes(count)
                .activity(FIND_CYCLE).executedTimes(count)
                .activity(SEND_EVENT).executedTimes(count)
                .activity(COUNT_DOWN).executedTimes(count);
    }

    private List<Colleague> getColleagues(int count) {
        var colleagues = new ArrayList<Colleague>(count);
        for (int i = 0; i < count; i++) {
            colleagues.add(createColleague(Map.of(
                    KEYS.COLLEAGUE_UUID, UUID.randomUUID(),
                    KEYS.LEGAL_EMPLOYER_NAME, "tesco stores limited",
                    KEYS.BUSINESS_TYPE, "Office",
                    KEYS.WORK_LEVEL, WorkLevel.WL2,
                    KEYS.SALARY_FREQUENCY, "annual"))
            );
        }
        return colleagues;
    }

    private PMCycle buildCycle() {
        var cycle = new PMCycle();
        cycle.setUuid(UUID.randomUUID());
        return cycle;
    }
}
