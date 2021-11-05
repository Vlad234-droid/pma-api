package com.tesco.pma.cycle.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.dao.AbstractDAOTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.BasicJsonTester;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static com.tesco.pma.cycle.api.PMCycleStatus.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PMCycleDAOTest extends AbstractDAOTest {

    private static final UUID COLLEAGUE_UUID = UUID.fromString("d1810821-d1a9-48b5-9745-d0841151911f");
    private static final UUID CYCLE_UUID = UUID.fromString("5d8a71fe-9cc6-4f3a-9ab6-75f08e6886d4");

    private final BasicJsonTester json = new BasicJsonTester(getClass());

    @Autowired
    private PMCycleDAO dao;

    @Autowired
    private ObjectMapper objectMapper;

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

        assertThat(byColleague.get(0)).returns(CYCLE_UUID, from(PMCycle::getUuid));
    }

    @Test
    @DataSet("pm_colleague_cycle_init.xml")
    void getMetadata() throws Exception {
        var metadata = IOUtils.toString(Objects.requireNonNull(getClass()
                .getResourceAsStream("/com/tesco/pma/cycle/dao/type_1_metadata.json")), StandardCharsets.UTF_8);
        assertEquals(1, dao.updateMetadata(CYCLE_UUID, metadata));
        var actual = dao.read(CYCLE_UUID);
        var expectedJson = json.from(metadata);
        assertThat(expectedJson).isEqualToJson(actual.getJsonMetadata());
    }
}