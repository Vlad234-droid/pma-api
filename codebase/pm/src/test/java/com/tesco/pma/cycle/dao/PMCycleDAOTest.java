package com.tesco.pma.cycle.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.dao.AbstractDAOTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.tesco.pma.cycle.api.PMCycleStatus.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;

class PMCycleDAOTest extends AbstractDAOTest {

    private static final UUID COLLEAGUE_UUID = UUID.fromString("d1810821-d1a9-48b5-9745-d0841151911f");
    private static final UUID COLLEAGUE_ACTIVE_CYCLE_UUID = UUID.fromString("98c23a14-8a46-41f0-bfcf-312a17c7dae2");
    private static final UUID PM_ACTIVE_CYCLE_UUID = UUID.fromString("5d8a71fe-9cc6-4f3a-9ab6-75f08e6886d4");


    @Autowired
    private PMCycleDAO dao;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }

    @Test
    @DataSet("pm_colleague_cycle_init.xml")
    void getByColleague() {
        List<PMCycle> byColleague = dao.getByColleague(COLLEAGUE_UUID, null);
        assertThat(byColleague).isNotEmpty();
        assertThat(byColleague.size()).isEqualTo(2);
    }

    @Test
    @DataSet("pm_colleague_cycle_init.xml")
    void getActiveByColleague() {
        List<PMCycle> byColleague = dao.getByColleague(COLLEAGUE_UUID, DictionaryFilter.includeFilter(Set.of(ACTIVE)));
        assertThat(byColleague).isNotEmpty();
        assertThat(byColleague.size()).isEqualTo(1);

        assertThat(byColleague.get(0))
                .returns(PM_ACTIVE_CYCLE_UUID, from(PMCycle::getUuid));
    }

}