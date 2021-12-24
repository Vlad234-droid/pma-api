package com.tesco.pma.process.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.cycle.dao.config.PMCycleTypeHandlerConfig;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.process.api.PMProcessStatus;
import com.tesco.pma.process.api.PMRuntimeProcess;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;
import java.util.UUID;

import static com.tesco.pma.api.DictionaryFilter.includeFilter;
import static com.tesco.pma.process.api.PMProcessStatus.COMPLETED;
import static com.tesco.pma.process.api.PMProcessStatus.REGISTERED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 13.10.2021 Time: 22:40
 */
@ContextConfiguration(classes = PMCycleTypeHandlerConfig.class)
class PMRuntimeProcessDAOTest extends AbstractDAOTest {

    private static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/cycle/dao/";

    private static final UUID PM_UUID = UUID.fromString("4f2ab073-2c31-11ec-916b-0242391d2e7a");
    private static final UUID NEW_UUID = UUID.fromString("4f2ab073-2c31-11ec-916b-0242391d2e7c");
    private static final String BPM_UUID = "bf2ab073-2c31-11ec-916b-0242391d2e7c";
    private static final String NEW_BPM_UUID = "cf2ab073-2c31-11ec-916b-0242391d2e7c";
    private static final UUID CYCLE_UUID = UUID.fromString("10000000-0000-0000-0000-000000000000");
    private static final String BUSINESS_KEY = "PROCESS_NAME";

    @Autowired
    private PMRuntimeProcessDAO dao;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }

    @Test
    @DataSet(BASE_PATH_TO_DATA_SET + "pm_cycle_init.xml")
    void create() {
        assertEquals(1, dao.create(new PMRuntimeProcess(NEW_UUID, PMProcessStatus.STARTED, CYCLE_UUID,
                NEW_BPM_UUID, BUSINESS_KEY, null)));

        var actual = dao.read(NEW_UUID);
        checkProcess(actual, NEW_UUID, PMProcessStatus.STARTED, NEW_BPM_UUID);
        checkHistory(actual, 1, 0);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "pm_cycle_init.xml",
            BASE_PATH_TO_DATA_SET + "pm_rt_process_init.xml"})
    void read() {
        var actual = dao.read(PM_UUID);
        checkProcess(actual, PM_UUID, REGISTERED, BPM_UUID);
        checkHistory(actual, 1, 0);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "pm_cycle_init.xml",
            BASE_PATH_TO_DATA_SET + "pm_rt_process_init.xml"})
    void updateStatusFailed() {
        assertEquals(0, dao.updateStatus(PM_UUID, COMPLETED,
                includeFilter(PMProcessStatus.STARTED)));

        var actual = dao.read(PM_UUID);
        checkProcess(actual, PM_UUID, REGISTERED, BPM_UUID);
        checkHistory(actual, 1, 0);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "pm_cycle_init.xml",
            BASE_PATH_TO_DATA_SET + "pm_rt_process_init.xml"})
    void updateStatus() {
        assertEquals(1, dao.updateStatus(PM_UUID, PMProcessStatus.STARTED,
                includeFilter(REGISTERED)));

        var actual = dao.read(PM_UUID);
        checkProcess(actual, PM_UUID, PMProcessStatus.STARTED, BPM_UUID);
        checkHistory(actual, 2, 1);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "pm_cycle_init.xml",
            BASE_PATH_TO_DATA_SET + "pm_rt_process_init.xml"})
    void findByBusinessKey() {
        List<PMRuntimeProcess> processes = dao.findByBusinessKey(BUSINESS_KEY);
        assertEquals(3, processes.size());
        assertTrue(processes.get(0).getLastUpdateTime().isAfter(processes.get(1).getLastUpdateTime()));
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "pm_cycle_init.xml",
            BASE_PATH_TO_DATA_SET + "pm_rt_process_init.xml"})
    void findByCycleUuidAndStatus() {
        List<PMRuntimeProcess> processes = dao.findByCycleUuidAndStatus(CYCLE_UUID, includeFilter(COMPLETED));
        assertEquals(1, processes.size());
    }

    private void checkProcess(PMRuntimeProcess actual, UUID pmUuid, PMProcessStatus status, String bpmUuid) {
        assertNotNull(actual);
        assertEquals(pmUuid, actual.getId());
        assertEquals(status, actual.getStatus());
        assertEquals(CYCLE_UUID, actual.getCycleUuid());
        assertEquals(bpmUuid, actual.getBpmProcessId());
        assertEquals(BUSINESS_KEY, actual.getBusinessKey());
        assertNotNull(actual.getLastUpdateTime());
    }

    private void checkHistory(PMRuntimeProcess actual, int amount, int checking) {
        var hi = dao.readHistory(actual.getId());
        assertNotNull(hi);
        assertEquals(amount, hi.size());
        var rec = hi.get(checking);
        assertEquals(actual.getId(), rec.getId());
        assertEquals(actual.getStatus(), rec.getStatus());
        assertEquals(actual.getLastUpdateTime(), rec.getUpdateTime());
    }
}