package com.tesco.pma.cycle.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.cycle.api.PMColleagueCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.dao.config.PMCycleTypeHandlerConfig;
import com.tesco.pma.dao.AbstractDAOTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.tesco.pma.cycle.api.PMCycleStatus.ACTIVE;
import static com.tesco.pma.cycle.api.PMCycleStatus.INACTIVE;
import static com.tesco.pma.cycle.api.PMCycleStatus.REGISTERED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ContextConfiguration(classes = PMCycleTypeHandlerConfig.class)
class PMColleagueCycleDAOTest extends AbstractDAOTest {

    private static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/cycle/dao/";

    private static final UUID COLLEAGUE_UUID = UUID.fromString("d1810821-d1a9-48b5-9745-d0841151911f");
    private static final UUID COLLEAGUE_UUID_2 = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID COLLEAGUE_CYCLE_UUID = UUID.fromString("98c23a14-8a46-41f0-bfcf-312a17c7dae2");
    private static final UUID COLLEAGUE_CYCLE_UUID_2 = UUID.fromString("9193e171-49e9-492c-a56f-6a68916722f0");
    private static final UUID CYCLE_UUID = UUID.fromString("10000000-0000-0000-0000-000000000000");

    @Autowired
    private PMColleagueCycleDAO dao;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "pm_cycle_init.xml",
            BASE_PATH_TO_DATA_SET + "pm_colleague_cycle_init.xml"})
    void read() {
        assertColleagueCycle(COLLEAGUE_CYCLE_UUID, COLLEAGUE_UUID, ACTIVE, dao.read(COLLEAGUE_CYCLE_UUID));
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "pm_cycle_init.xml",
            BASE_PATH_TO_DATA_SET + "pm_colleague_cycle_init.xml"})
    void getByParams() {
        var cc = dao.getByParams(CYCLE_UUID, COLLEAGUE_UUID, null);
        assertEquals(2, cc.size());

        cc = dao.getByParams(CYCLE_UUID, COLLEAGUE_UUID, ACTIVE);
        assertEquals(1, cc.size());
        assertEquals(UUID.fromString("98c23a14-8a46-41f0-bfcf-312a17c7dae2"), cc.get(0).getUuid());

        cc = dao.getByParams(CYCLE_UUID, COLLEAGUE_UUID, PMCycleStatus.INACTIVE);
        assertEquals(1, cc.size());
        assertEquals(UUID.fromString("9193e171-49e9-492c-a56f-6a68916722f0"), cc.get(0).getUuid());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "pm_cycle_init.xml",
            BASE_PATH_TO_DATA_SET + "pm_colleague_cycle_init.xml"})
    void getByCycleUuidWithoutTimelinePoint() {
        var cc = dao.getByCycleUuidWithoutTimelinePoint(CYCLE_UUID,
                DictionaryFilter.includeFilter(REGISTERED, ACTIVE));
        assertEquals(1, cc.size());
        assertColleagueCycle(UUID.fromString("98c23a14-8a46-41f0-bfcf-312a17c7dae2"), COLLEAGUE_UUID, ACTIVE, cc.get(0));
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "pm_cycle_init.xml",
            BASE_PATH_TO_DATA_SET + "colleague_init.xml"})
    void saveAll() {
        var ccUuid1 = UUID.randomUUID();
        var ccUuid2 = UUID.randomUUID();

        assertTrue(dao.getByParams(null, null, null).isEmpty());
        assertEquals(2, dao.saveAll(List.of(createCycle(ccUuid1, ACTIVE), createCycle(ccUuid2, INACTIVE))));
        assertEquals(2, dao.getByParams(null, null, null).size());

        var cc1 = dao.read(ccUuid1);
        assertColleagueCycle(ccUuid1, COLLEAGUE_UUID_2, ACTIVE, cc1);

        var cc2 = dao.read(ccUuid2);
        assertColleagueCycle(ccUuid2, COLLEAGUE_UUID_2, INACTIVE, cc2);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "pm_cycle_init.xml",
            BASE_PATH_TO_DATA_SET + "colleague_init.xml"})
    void create() {
        var ccUuid = UUID.randomUUID();

        assertEquals(1, dao.create(createCycle(ccUuid)));

        var cc = dao.read(ccUuid);
        assertColleagueCycle(ccUuid, COLLEAGUE_UUID_2, ACTIVE, cc);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "pm_cycle_init.xml",
            BASE_PATH_TO_DATA_SET + "colleague_init.xml"})
    void createDuplicate() {
        var ccUuid = UUID.randomUUID();

        assertEquals(1, dao.create(createCycle(ccUuid)));
        assertEquals(0, dao.create(createCycle(ccUuid)));
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "pm_cycle_init.xml",
            BASE_PATH_TO_DATA_SET + "pm_colleague_cycle_init.xml"})
    void delete() {
        assertEquals(1, dao.delete(COLLEAGUE_CYCLE_UUID_2));
        assertNull(dao.read(COLLEAGUE_CYCLE_UUID_2));
    }

    private void assertColleagueCycle(UUID uuid, UUID colleagueUuid, PMCycleStatus status, PMColleagueCycle colleagueCycle) {
        assertNotNull(colleagueCycle);
        assertEquals(uuid, colleagueCycle.getUuid());
        assertEquals(colleagueUuid, colleagueCycle.getColleagueUuid());
        assertEquals(CYCLE_UUID, colleagueCycle.getCycleUuid());
        assertEquals(status, colleagueCycle.getStatus());
    }

    private PMColleagueCycle createCycle(UUID uuid) {
        return createCycle(uuid, ACTIVE);
    }

    private PMColleagueCycle createCycle(UUID uuid, PMCycleStatus status) {
        return PMColleagueCycle.builder()
                .status(status)
                .uuid(uuid)
                .cycleUuid(CYCLE_UUID)
                .colleagueUuid(COLLEAGUE_UUID_2)
                .startTime(Instant.now())
                .endTime(Instant.now())
                .build();
    }
}