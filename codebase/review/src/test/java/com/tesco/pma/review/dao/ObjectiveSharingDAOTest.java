package com.tesco.pma.review.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.dao.AbstractDAOTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectiveSharingDAOTest extends AbstractDAOTest {
    private static final UUID PERFORMANCE_CYCLE_UUID = UUID.fromString("10000000-1000-0000-0000-000000000000");
    private static final UUID COLLEAGUE_UUID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID COLLEAGUE_UUID_2 = UUID.fromString("10000000-0000-0000-0000-000000000002");

    @Autowired
    private ObjectiveSharingDAO instance;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "shared_objective_init.xml"})
    void shareObjectives() {
        var managerShareObjectives = instance.isColleagueShareObjectives(COLLEAGUE_UUID, PERFORMANCE_CYCLE_UUID);
        Assertions.assertThat(managerShareObjectives).isFalse();

        var count = instance.shareObjectives(COLLEAGUE_UUID, PERFORMANCE_CYCLE_UUID);
        assertThat(count).isEqualTo(1);

        managerShareObjectives = instance.isColleagueShareObjectives(COLLEAGUE_UUID, PERFORMANCE_CYCLE_UUID);
        Assertions.assertThat(managerShareObjectives).isTrue();
    }

    @Test
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "shared_objective_init.xml"})
    void stopSharingObjectives() {
        var managerShareObjectives = instance.isColleagueShareObjectives(COLLEAGUE_UUID_2, PERFORMANCE_CYCLE_UUID);
        Assertions.assertThat(managerShareObjectives).isTrue();

        var count = instance.stopSharingObjectives(COLLEAGUE_UUID_2, PERFORMANCE_CYCLE_UUID);
        assertThat(count).isEqualTo(1);

        managerShareObjectives = instance.isColleagueShareObjectives(COLLEAGUE_UUID_2, PERFORMANCE_CYCLE_UUID);
        Assertions.assertThat(managerShareObjectives).isFalse();
    }
}
