package com.tesco.pma.cycle.service;

import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.bpm.api.ProcessManagerService;
import com.tesco.pma.cycle.LocalTestConfig;
import com.tesco.pma.cycle.api.CompositePMCycleResponse;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.dao.PMColleagueCycleDAO;
import com.tesco.pma.cycle.dao.PMCycleDAO;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.fs.service.FileService;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.process.api.PMProcessStatus;
import com.tesco.pma.process.api.PMRuntimeProcess;
import com.tesco.pma.process.service.PMProcessService;
import com.tesco.pma.util.ResourceProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.UUID;

import static com.tesco.pma.cycle.api.PMCycleStatus.ACTIVE;
import static com.tesco.pma.cycle.api.PMCycleStatus.DRAFT;
import static com.tesco.pma.util.TestDataUtil.CYCLE_UUID;
import static com.tesco.pma.util.TestDataUtil.TEMPLATE_UUID;
import static com.tesco.pma.util.TestDataUtil.TEST_FILE_NAME;
import static com.tesco.pma.util.TestDataUtil.USER_UUID;
import static com.tesco.pma.util.TestDataUtil.buildCycle;
import static com.tesco.pma.util.TestDataUtil.buildCycleWithMetadata;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(classes = LocalTestConfig.class)
@ExtendWith(MockitoExtension.class)
class PMCycleServiceImplTest {


    @MockBean
    private PMCycleDAO cycleDAO;
    @MockBean
    private PMColleagueCycleDAO pmColleagueCycleDAO;
    @MockBean
    private ProcessManagerService processManagerService;
    @MockBean
    private PMProcessService pmProcessService;
    @MockBean
    private FileService fileService;
    @MockBean
    private ResourceProvider resourceProvider;
    @MockBean
    private DeploymentService deploymentService;

    @SpyBean
    private PMCycleServiceImpl cycleService;


    @Test
    void create() {
        var cycle = buildCycle();
        when(cycleDAO.create(cycle)).thenReturn(1);
        var result = cycleService.create(cycle, USER_UUID);
        assertEquals(result.getName(), cycle.getName());
    }


    @Test
    void updateStatus() {
        var cycle = buildCycle();
        cycle.setStatus(DRAFT);
        when(cycleDAO.read(eq(CYCLE_UUID), any())).thenReturn(cycle);
        when(cycleDAO.updateStatus(eq(CYCLE_UUID), any(), any())).thenReturn(1);

        var updatedCycle = cycleService.updateStatus(CYCLE_UUID, ACTIVE);

        assertEquals(ACTIVE, updatedCycle.getStatus());
    }

    @Test
    void updateStatusWithFilter() {
        var cycle = buildCycle();
        cycle.setStatus(DRAFT);
        when(cycleDAO.read(eq(CYCLE_UUID), any())).thenReturn(cycle);
        when(cycleDAO.updateStatus(eq(CYCLE_UUID), any(), any())).thenReturn(1);

        DictionaryFilter<PMCycleStatus> filter = DictionaryFilter.includeFilter(DRAFT);
        var updatedCycle = cycleService.updateStatus(CYCLE_UUID, ACTIVE, filter);

        assertEquals(ACTIVE, updatedCycle.getStatus());
    }

    @Test
    void updateStatusNotFound() {
        when(cycleDAO.read(eq(CYCLE_UUID), any())).thenReturn(null);
        assertThrows(NotFoundException.class, () -> cycleService.updateStatus(CYCLE_UUID, ACTIVE));
    }

    @Test
    void get() {
        var cycle = buildCycle();
        var expected = new CompositePMCycleResponse();
        expected.setCycle(cycle);

        when(cycleDAO.read(eq(CYCLE_UUID), any())).thenReturn(cycle);
        var actual = cycleService.get(CYCLE_UUID, false);
        assertEquals(expected, actual);
    }

    @Test
    void update() {
        var cycle = buildCycle();
        cycle.setStatus(DRAFT);
        when(cycleDAO.update(eq(cycle), any())).thenReturn(1);

        var updated = cycleService.update(cycle);
        assertEquals(cycle, updated);
    }

    @Test
    void getCurrentByColleague() {
        var cycle = buildCycle();

        when(cycleDAO.getByColleague(eq(USER_UUID), any())).thenReturn(singletonList(cycle));

        var actual = cycleService.getCurrentByColleague(USER_UUID);
        assertEquals(cycle, actual);
    }

    @Test
    void getByColleague() {
        var cycle = buildCycle();

        when(cycleDAO.getByColleague(USER_UUID)).thenReturn(singletonList(cycle));

        var actual = cycleService.getByColleague(USER_UUID);
        assertEquals(cycle, actual.get(0));
    }

    @Test
    void findAll() {
        var cycles = Collections.singletonList(buildCycle());

        when(cycleDAO.findAll(any(), anyBoolean())).thenReturn(cycles);

        var actual = cycleService.findAll(new RequestQuery(), false);
        assertEquals(cycles, actual);
    }

    @Test
    void deploy() {
        var cycle = buildCycleWithMetadata();
        var process = new PMRuntimeProcess();
        UUID processUUID = UUID.randomUUID();
        process.setId(processUUID);
        when(cycleDAO.read(eq(CYCLE_UUID), any())).thenReturn(cycle);
        when(cycleDAO.updateStatus(eq(CYCLE_UUID), any(), any())).thenReturn(1);
        when(pmProcessService.register(any(), eq(PMProcessStatus.REGISTERED))).thenReturn(process);

        var deployedUUID = cycleService.deploy(CYCLE_UUID);

        assertEquals(processUUID, deployedUUID);
        verify(deploymentService).deploy(TEMPLATE_UUID);
        verify(pmProcessService).register(any(), eq(PMProcessStatus.REGISTERED));
        assertEquals(ACTIVE, cycle.getStatus());
    }

    @Test
    void startCycle() throws Exception {
        var cycle = buildCycleWithMetadata();

        cycleService.start(cycle);
        verify(processManagerService).runProcessByResourceName(eq(TEST_FILE_NAME), any());
    }

}