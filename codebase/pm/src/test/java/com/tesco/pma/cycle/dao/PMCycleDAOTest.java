package com.tesco.pma.cycle.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.database.rider.core.api.dataset.CompareOperation;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.colleague.api.ColleagueSimple;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.api.PMCycleType;
import com.tesco.pma.cycle.dao.config.PMCycleTypeHandlerConfig;
import com.tesco.pma.dao.AbstractDAOTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.BasicJsonTester;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static com.tesco.pma.cycle.api.PMCycleStatus.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ContextConfiguration(classes = PMCycleTypeHandlerConfig.class)
class PMCycleDAOTest extends AbstractDAOTest {

    private static final UUID COLLEAGUE_UUID = UUID.fromString("d1810821-d1a9-48b5-9745-d0841151911f");
    private static final UUID CYCLE_UUID = UUID.fromString("5d8a71fe-9cc6-4f3a-9ab6-75f08e6886d4");
    private static final UUID CYCLE_UUID_3 = UUID.fromString("5d8a71fe-9cc6-4f3a-9ab6-75f08e6886d5");
    private static final UUID CYCLE_CREATE_UUID = UUID.fromString("5ff53f32-39c8-4a14-86ba-58b87c8da4e6");
    private static final UUID TEMPLATE_UUID = UUID.fromString("bd36be33-25f4-4db7-90e9-0df0e6e8f04a");
    public static final String TEST_KEY = "TestKey";
    public static final String TEST_CYCLE_NAME = "TestCycleName";
    public static final String SDF_PATTERN = "yyyy-MM-dd";
    public static final String UPDATED_NAME = "updated_name";

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
    @ExpectedDataSet(value = "pm_create_cycle_expected_1.xml", compareOperation = CompareOperation.CONTAINS)
    void createPMCycle() throws ParseException {
        Instant testTime = new SimpleDateFormat(SDF_PATTERN, Locale.ENGLISH).parse("2016-12-31").toInstant();
        dao.insertOrUpdatePMCycle(createCycle(CYCLE_CREATE_UUID), testTime);
    }

    @Test
    @DataSet("pm_colleague_cycle_init.xml")
    @ExpectedDataSet(value = "pm_update_cycle_status_expected_1.xml", compareOperation = CompareOperation.CONTAINS)
    void changeCycleStatus() {
        dao.updateStatus(CYCLE_UUID, PMCycleStatus.INACTIVE, null);
    }


    @Test
    @DataSet("pm_colleague_cycle_init.xml")
    void getMetadata() throws Exception {
        var metadata = IOUtils.toString(Objects.requireNonNull(getClass()
                .getResourceAsStream("/com/tesco/pma/cycle/dao/type_1_metadata.json")), StandardCharsets.UTF_8);
        assertEquals(1, dao.updateMetadata(CYCLE_UUID, metadata));
        var actual = dao.getByUUID(CYCLE_UUID, null);
        var expectedJson = json.from(metadata);
        assertThat(expectedJson).isEqualToJson(actual.getJsonMetadata());
    }

    @Test
    @DataSet("pm_cycle_edit_init.xml")
    @ExpectedDataSet(value = "pm_cycle_edit_expected.xml", compareOperation = CompareOperation.CONTAINS)
    void update() {
        var actualCycle = dao.getByUUID(CYCLE_UUID, null);
        actualCycle.setName(UPDATED_NAME);
        dao.update(actualCycle, null);
    }


    private PMCycle createCycle(UUID uuid) {

        return PMCycle.builder()
                .name(TEST_CYCLE_NAME)
                .status(ACTIVE)
                .type(PMCycleType.HIRING)
                .uuid(CYCLE_CREATE_UUID)
                .createdBy(ColleagueSimple
                        .builder()
                        .uuid(COLLEAGUE_UUID)
                        .build())
                .uuid(uuid)
                .entryConfigKey(TEST_KEY)
                .templateUUID(TEMPLATE_UUID)
                .build();
    }
}