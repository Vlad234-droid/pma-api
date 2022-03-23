package com.tesco.pma.cycle.service;

import com.tesco.pma.bpm.api.ProcessManagerService;
import com.tesco.pma.cycle.LocalTestConfig;
import com.tesco.pma.cycle.dao.PMColleagueCycleDAO;
import com.tesco.pma.cycle.dao.PMCycleDAO;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.process.api.PMProcessStatus;
import com.tesco.pma.process.service.PMProcessService;
import com.tesco.pma.service.BatchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import static com.tesco.pma.cycle.api.PMCycleStatus.ACTIVE;
import static com.tesco.pma.cycle.api.PMCycleStatus.INACTIVE;
import static com.tesco.pma.util.TestDataUtil.BPM_PROCESS_ID;
import static com.tesco.pma.util.TestDataUtil.COLLEAGUE_CYCLE_UUID;
import static com.tesco.pma.util.TestDataUtil.CYCLE_UUID;
import static com.tesco.pma.util.TestDataUtil.PROCESS_UUID;
import static com.tesco.pma.util.TestDataUtil.USER_UUID;
import static com.tesco.pma.util.TestDataUtil.buildColleagueCycle;
import static com.tesco.pma.util.TestDataUtil.buildCycle;
import static com.tesco.pma.util.TestDataUtil.buildRuntimeProcess;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(classes = LocalTestConfig.class)
@ExtendWith(MockitoExtension.class)
class PMColleagueCycleServiceImplTest {
    @MockBean
    private BatchService batchService;
    @MockBean
    private PMColleagueCycleDAO dao;
    @MockBean
    private PMProcessService pmProcessService;
    @MockBean
    private ProcessManagerService processManagerService;
    @MockBean
    private PMCycleDAO cycleDAO;

    @SpyBean
    private PMColleagueCycleServiceImpl service;

    @Test
    void get() {
        var cycle = buildColleagueCycle(COLLEAGUE_CYCLE_UUID);

        when(dao.read(COLLEAGUE_CYCLE_UUID)).thenReturn(cycle);

        var actual = service.get(COLLEAGUE_CYCLE_UUID);
        assertEquals(cycle, actual);
    }

    @Test
    void getNotFound() {

        when(dao.read(COLLEAGUE_CYCLE_UUID)).thenReturn(null);
        assertThrows(NotFoundException.class, () -> service.get(COLLEAGUE_CYCLE_UUID));
    }

    @Test
    void getByCycleUuid() {
        var cycle = buildColleagueCycle(CYCLE_UUID);

        when(dao.getByParams(eq(CYCLE_UUID), eq(COLLEAGUE_CYCLE_UUID), any())).thenReturn(of(cycle));

        var actual = service.getByCycleUuid(CYCLE_UUID, COLLEAGUE_CYCLE_UUID, ACTIVE);
        assertEquals(cycle, actual.get(0));
    }

    @Test
    void getByCycleUuidWithoutTimelinePoint() {
        var cycle = buildColleagueCycle(CYCLE_UUID);

        when(dao.getByParams(eq(CYCLE_UUID), eq(COLLEAGUE_CYCLE_UUID), any())).thenReturn(of(cycle));

        var actual = service.getByCycleUuid(CYCLE_UUID, COLLEAGUE_CYCLE_UUID, ACTIVE);
        assertEquals(cycle, actual.get(0));
    }

    @Test
    void create() {
        var cycle = buildColleagueCycle(null);

        when(dao.create(cycle)).thenReturn(1);

        service.create(cycle);

        assertNotNull(cycle.getUuid());
        assertNotNull(cycle.getCreationTime());
    }

    @Test
    void update() {
        var cycle = buildColleagueCycle(COLLEAGUE_CYCLE_UUID);
        when(dao.update(cycle)).thenReturn(1);

        var actual = service.update(cycle);
        assertEquals(cycle, actual);
    }

    @Test
    void updateNotFound() {
        var cycle = buildColleagueCycle(COLLEAGUE_CYCLE_UUID);
        when(dao.update(cycle)).thenReturn(0);

        assertThrows(NotFoundException.class, () -> service.update(cycle));
    }


    @Test
    void delete() {
        when(dao.delete(COLLEAGUE_CYCLE_UUID)).thenReturn(1);
        service.delete(COLLEAGUE_CYCLE_UUID);

        verify(dao).delete(COLLEAGUE_CYCLE_UUID);
    }

    @Test
    void deleteNotFound() {
        when(dao.delete(COLLEAGUE_CYCLE_UUID)).thenReturn(0);

        assertThrows(NotFoundException.class, () -> service.delete(COLLEAGUE_CYCLE_UUID));
    }

    @Test
    void changeStatusForColleague() {
        when(dao.changeStatusForColleague(USER_UUID, ACTIVE, INACTIVE)).thenReturn(1);

        service.changeStatusForColleague(USER_UUID, ACTIVE, INACTIVE);

        verify(dao).changeStatusForColleague(USER_UUID, ACTIVE, INACTIVE);
    }

    @Test
    void changeStatusForColleagueNotFound() {
        when(dao.changeStatusForColleague(USER_UUID, ACTIVE, INACTIVE)).thenReturn(0);

        assertThrows(NotFoundException.class, () -> service.changeStatusForColleague(USER_UUID, ACTIVE, INACTIVE));
    }

    @Test
    void start() throws Exception {
        var cycle = buildCycle();
        var processes = of(buildRuntimeProcess());
        when(cycleDAO.read(eq(CYCLE_UUID), any())).thenReturn(cycle);
        when(pmProcessService.findByCycleUuidAndStatus(eq(CYCLE_UUID), any())).thenReturn(processes);
        when(processManagerService.runProcessById(eq(BPM_PROCESS_ID), any())).thenReturn(BPM_PROCESS_ID);

        service.start(CYCLE_UUID, USER_UUID);

        verify(pmProcessService).findByCycleUuidAndStatus(eq(CYCLE_UUID), any());
        verify(processManagerService).runProcessById(eq(BPM_PROCESS_ID), any());
    }
}