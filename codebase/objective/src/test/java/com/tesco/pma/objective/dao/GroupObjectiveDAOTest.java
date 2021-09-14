package com.tesco.pma.objective.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.objective.domain.GroupObjective;
import com.tesco.pma.objective.domain.ObjectiveStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GroupObjectiveDAOTest extends AbstractDAOTest {

    private static final UUID GROUP_OBJECTIVE_UUID = UUID.fromString("aab9ab0b-f50f-4442-8900-b03777ee0012");
    private static final UUID BUSINESS_UNIT_UUID = UUID.fromString("ffb9ab0b-f50f-4442-8900-b03777ee00ef");
    private static final UUID PERFORMANCE_CYCLE_UUID = UUID.fromString("0c5d9cb1-22cf-4fcd-a19a-9e70df6bc941");
    private static final Integer SEQUENCE_NUMBER = 1;
    private static final String GROUP_OBJECTIVE_TITLE = "Title #1";
    private static final Integer VERSION = 1;

    @Autowired
    private GroupObjectiveDAO instance;

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
    @DataSet("cleanup.xml")
    @ExpectedDataSet("group_objective_insert_expected_1.xml")
    void insertSucceeded() {

        final var groupObjective = createGroupObjective(GROUP_OBJECTIVE_UUID);

        final int rowsInserted = instance.insert(groupObjective);

        assertThat(rowsInserted).isOne();
    }

    private GroupObjective createGroupObjective(UUID uuid) {
        var groupObjective = new GroupObjective();
        groupObjective.setUuid(uuid);
        groupObjective.setBusinessUnitUuid(BUSINESS_UNIT_UUID);
        groupObjective.setPerformanceCycleUuid(PERFORMANCE_CYCLE_UUID);
        groupObjective.setSequenceNumber(SEQUENCE_NUMBER);
        groupObjective.setTitle(GROUP_OBJECTIVE_TITLE);
        groupObjective.setVersion(VERSION);
        groupObjective.setStatus(ObjectiveStatus.DRAFT);
        return groupObjective;
    }
}
