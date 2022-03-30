package com.tesco.pma.cycle.service.rest;

import com.tesco.pma.bpm.api.ProcessExecutionException;
import com.tesco.pma.bpm.api.ProcessManagerService;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.configuration.audit.AuditorAware;
import com.tesco.pma.cycle.LocalTestConfig;
import com.tesco.pma.cycle.service.PMColleagueCycleService;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.cycle.service.PMCycleMappingService;
import com.tesco.pma.error.ApiExceptionHandler;
import com.tesco.pma.rest.AbstractEndpointTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.tesco.pma.cycle.service.rest.PMCycleEndpoint.PM_CYCLE_ASSIGNMENT;
import static com.tesco.pma.flow.FlowParameters.COLLEAGUE_UUIDS;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PMCycleEndpoint.class)
@ContextConfiguration(classes = {LocalTestConfig.class, PMCycleEndpoint.class, ApiExceptionHandler.class})
class PMCycleEndpointTest extends AbstractEndpointTest {

    private static final String PM_CYCLES_ASSIGNMENT_URL = "/pm-cycles/assignment";
    private static final String PM_CYCLES_MAPPING_KEYS = "/pm-cycles/mappings/keys";
    private static final String PM_CYCLES_MAPPING_KEYS_COLLEAGUES = "/pm-cycles/mappings/keys/colleagues";
    private static final List<String> COLLEAGUES = List.of("10000000-0000-0000-0000-000000000001", "10000000-0000-0000-0000-000000000002");

    private static final String PM_CYCLE_ASSIGNMENT_JSON_FILE_NAME = "pm_cycle_assignment.json";
    private static final String PM_CYCLE_ASSIGNMENT_OK_RESPONSE_JSON_FILE_NAME = "pm_cycle_assignment_ok_response.json";

    @MockBean
    private PMCycleService service;

    @MockBean
    private ProcessManagerService processManagerService;

    @MockBean
    private AuditorAware<UUID> auditorAware;

    @MockBean
    private PMCycleMappingService pmCycleMappingService;

    @MockBean
    private NamedMessageSourceAccessor messageSourceAccessor;

    @MockBean
    PMColleagueCycleService pmColleagueCycleService;

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

    @Test
    void getPmCycleMappingKeyTest() throws Exception {
        when(pmCycleMappingService.getPmCycleMappingKeys()).thenReturn(Set.of("Group A", "Group B"));
        performGetWith(admin(), status().isOk(), PM_CYCLES_MAPPING_KEYS);
    }

    @Test
    void getPmCycleMappingKeyByColleagues() throws Exception {

        var uuid1 = UUID.randomUUID();
        var uuid2 = UUID.randomUUID();

        when(pmCycleMappingService.getPmCycleMappingKeys(Mockito.anyList()))
                .thenReturn(Map.of(uuid1, "Group B", uuid2, "Group A"));

        performGetWith(admin(), status().isOk(),
                PM_CYCLES_MAPPING_KEYS_COLLEAGUES + "?uuids={uuid1},{uuid2}", uuid1, uuid2);
    }
}