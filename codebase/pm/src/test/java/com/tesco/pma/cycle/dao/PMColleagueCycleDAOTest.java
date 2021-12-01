package com.tesco.pma.cycle.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.cycle.api.PMColleagueCycle;
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
import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = PMCycleTypeHandlerConfig.class)
class PMColleagueCycleDAOTest extends AbstractDAOTest {

    private static final UUID COLLEAGUE_UUID = UUID.fromString("d1810821-d1a9-48b5-9745-d0841151911f");
    private static final UUID COLLEAGUE_CYCLE_UUID = UUID.fromString("98c23a14-8a46-41f0-bfcf-312a17c7dae2");
    private static final UUID COLLEAGUE_CYCLE_UUID_2 = UUID.fromString("9193e171-49e9-492c-a56f-6a68916722f0");
    private static final UUID CYCLE_UUID = UUID.fromString("5d8a71fe-9cc6-4f3a-9ab6-75f08e6886d4");

    @Autowired
    private PMColleagueCycleDAO dao;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }

    @Test
    @DataSet("pm_colleague_cycle_init.xml")
    void read() {
        var cc = dao.read(COLLEAGUE_CYCLE_UUID);
        assertThat(cc)
                .returns(COLLEAGUE_CYCLE_UUID, PMColleagueCycle::getUuid)
                .returns(COLLEAGUE_UUID, PMColleagueCycle::getColleagueUuid)
                .returns(CYCLE_UUID, PMColleagueCycle::getCycleUuid)
                .returns(ACTIVE, PMColleagueCycle::getStatus);
    }

    @Test
    @DataSet("pm_colleague_cycle_init.xml")
    void getByCycleUuid() {
        var cc = dao.getByCycleUuid(CYCLE_UUID);
        assertThat(cc)
                .hasSize(2);
    }

    @Test
    @DataSet("pm_colleague_cycle_init.xml")
    void saveAll() {
        var ccUuid1 = UUID.randomUUID();
        var ccUuid2 = UUID.randomUUID();

        assertThat(dao.getByCycleUuid(CYCLE_UUID))
                .hasSize(2);

        var saved = dao.saveAll(List.of(createCycle(ccUuid1), createCycle(ccUuid2)));

        assertThat(saved).isEqualTo(2);

        assertThat(dao.getByCycleUuid(CYCLE_UUID))
                .hasSize(4);

        var cc1 = dao.read(ccUuid1);
        assertThat(cc1)
                .returns(ccUuid1, PMColleagueCycle::getUuid)
                .returns(COLLEAGUE_UUID, PMColleagueCycle::getColleagueUuid)
                .returns(CYCLE_UUID, PMColleagueCycle::getCycleUuid)
                .returns(ACTIVE, PMColleagueCycle::getStatus);

        var cc2 = dao.read(ccUuid2);
        assertThat(cc2)
                .returns(ccUuid2, PMColleagueCycle::getUuid)
                .returns(COLLEAGUE_UUID, PMColleagueCycle::getColleagueUuid)
                .returns(CYCLE_UUID, PMColleagueCycle::getCycleUuid)
                .returns(ACTIVE, PMColleagueCycle::getStatus);

    }

    @Test
    @DataSet("pm_colleague_cycle_init.xml")
    void create() {
        var ccUuid = UUID.randomUUID();

        var created = dao.create(createCycle(ccUuid));

        assertThat(created).isEqualTo(1);

        var cc = dao.read(ccUuid);
        assertThat(cc)
                .returns(ccUuid, PMColleagueCycle::getUuid)
                .returns(COLLEAGUE_UUID, PMColleagueCycle::getColleagueUuid)
                .returns(CYCLE_UUID, PMColleagueCycle::getCycleUuid)
                .returns(ACTIVE, PMColleagueCycle::getStatus);
    }

    @Test
    @DataSet("pm_colleague_cycle_init.xml")
    void delete() {

        var deleted = dao.delete(COLLEAGUE_CYCLE_UUID_2);

        assertThat(deleted).isEqualTo(1);

        var cc = dao.read(COLLEAGUE_CYCLE_UUID_2);
        assertThat(cc).isNull();
    }

    private PMColleagueCycle createCycle(UUID uuid) {
        return PMColleagueCycle.builder()
                .status(ACTIVE)
                .uuid(uuid)
                .cycleUuid(CYCLE_UUID)
                .colleagueUuid(COLLEAGUE_UUID)
                .startTime(Instant.now())
                .endTime(Instant.now())
                .build();
    }
}