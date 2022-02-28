package com.tesco.pma.process.rest;

import com.tesco.pma.bpm.api.ProcessExecutionException;
import com.tesco.pma.bpm.api.ProcessManagerService;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.LocalTestConfig;
import com.tesco.pma.process.service.PMProcessService;
import com.tesco.pma.rest.AbstractEndpointTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Map;

import static com.tesco.pma.flow.FlowParameters.COLLEAGUE_UUIDS;
import static com.tesco.pma.process.rest.PMProcessEndpoint.PM_CYCLE_ASSIGNMENT;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PMProcessEndpoint.class)
@ContextConfiguration(classes = {LocalTestConfig.class, PMProcessEndpoint.class})
class PMProcessEndpointTest extends AbstractEndpointTest {

    private static final String PM_CYCLE_ASSIGNMENT_JSON_FILE_NAME = "pm_cycle_assignment.json";
    private static final String PM_CYCLE_ASSIGNMENT_OK_RESPONSE_JSON_FILE_NAME = "pm_cycle_assignment_ok_response.json";

    private static final String PM_CYCLES_ASSIGNMENT_URL = "/processes/pm-cycles/assignment";
    public static final List<String> COLLEAGUES = List.of("10000000-0000-0000-0000-000000000001", "10000000-0000-0000-0000-000000000002");

    @MockBean
    private PMProcessService processService;

    @MockBean
    private ProcessManagerService processManagerService;

    @MockBean
    private NamedMessageSourceAccessor messageSourceAccessor;

    @Test
    void runCycleAssignmentProcess() throws Exception {
        var parameters = Map.of(COLLEAGUE_UUIDS.name(), COLLEAGUES);
        when(processManagerService.runProcess(PM_CYCLE_ASSIGNMENT, parameters)).thenReturn("test data");

        var result = performPostWith(admin(), PM_CYCLE_ASSIGNMENT_JSON_FILE_NAME, status().isOk(), PM_CYCLES_ASSIGNMENT_URL);

        assertResponseContent(result.getResponse(), PM_CYCLE_ASSIGNMENT_OK_RESPONSE_JSON_FILE_NAME);
    }

    @Test
    void runCycleAssignmentProcessThrowsProcessExecutionException() throws Exception {
        var parameters = Map.of(COLLEAGUE_UUIDS.name(), COLLEAGUES);
        when(processManagerService.runProcess(PM_CYCLE_ASSIGNMENT, parameters)).thenThrow(ProcessExecutionException.class);

        performPostWith(admin(), PM_CYCLE_ASSIGNMENT_JSON_FILE_NAME, status().isInternalServerError(), PM_CYCLES_ASSIGNMENT_URL);
    }
}