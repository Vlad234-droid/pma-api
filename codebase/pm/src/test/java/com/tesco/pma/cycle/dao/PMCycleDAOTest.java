package com.tesco.pma.cycle.dao;

import com.github.database.rider.core.api.dataset.CompareOperation;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.tesco.pma.colleague.api.ColleagueSimple;
import com.tesco.pma.cycle.api.PMCycle;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.api.PMCycleType;
import com.tesco.pma.cycle.dao.config.PMCycleTypeHandlerConfig;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.file.api.File;
import com.tesco.pma.pagination.RequestQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static com.tesco.pma.api.DictionaryFilter.includeFilter;
import static com.tesco.pma.cycle.api.PMCycleStatus.*;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ContextConfiguration(classes = PMCycleTypeHandlerConfig.class)
class PMCycleDAOTest extends AbstractDAOTest {

    private static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/cycle/dao/";

    private static final UUID COLLEAGUE_UUID = UUID.fromString("d1810821-d1a9-48b5-9745-d0841151911f");
    private static final UUID CYCLE_UUID = UUID.fromString("10000000-0000-0000-0000-000000000000");
    private static final UUID CYCLE_EDIT_UUID = UUID.fromString("5d8a71fe-9cc6-4f3a-9ab6-75f08e6886d4");
    private static final UUID CYCLE_CREATE_UUID = UUID.fromString("5ff53f32-39c8-4a14-86ba-58b87c8da4e6");
    private static final UUID TEMPLATE_UUID = UUID.fromString("bd36be33-25f4-4db7-90e9-0df0e6e8f04a");
    public static final String TEST_KEY = "TestKey";
    public static final String TEST_CYCLE_NAME = "TestCycleName";
    public static final String SDF_PATTERN = "yyyy-MM-dd";
    public static final String UPDATED_NAME = "updated_name";

    @Autowired
    private PMCycleDAO dao;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "pm_cycle_init.xml",
            BASE_PATH_TO_DATA_SET + "pm_colleague_cycle_init.xml"})
    void getByColleague() {
        List<PMCycle> byColleague = dao.getByColleague(COLLEAGUE_UUID, null);
        assertEquals(2, byColleague.size());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "pm_cycle_init.xml",
            BASE_PATH_TO_DATA_SET + "pm_colleague_cycle_init.xml"})
    void getActiveByColleague() {
        List<PMCycle> byColleague = dao.getByColleague(COLLEAGUE_UUID, includeFilter(Set.of(ACTIVE)));
        assertEquals(1, byColleague.size());
        assertEquals(CYCLE_UUID, byColleague.get(0).getUuid());
    }

    @Test
    @ExpectedDataSet(value = BASE_PATH_TO_DATA_SET + "pm_create_cycle_expected_1.xml", compareOperation = CompareOperation.CONTAINS)
    void createPMCycle() throws ParseException {
        Instant testTime = new SimpleDateFormat(SDF_PATTERN, Locale.ENGLISH).parse("2016-12-31").toInstant();
        assertEquals(1, dao.intCreate(createCycle(CYCLE_CREATE_UUID), testTime));
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "colleague_init.xml",
            BASE_PATH_TO_DATA_SET + "pm_cycle_edit_init.xml"})
    @ExpectedDataSet(value = BASE_PATH_TO_DATA_SET + "pm_cycle_edit_expected_2.xml", compareOperation = CompareOperation.CONTAINS)
    void updateExistingPMCycle() {
        var actualCycle = dao.read(CYCLE_EDIT_UUID, null);
        actualCycle.setName(UPDATED_NAME);
        assertEquals(1, dao.update(actualCycle, includeFilter(DRAFT)));
    }

    @Test
    @DataSet(BASE_PATH_TO_DATA_SET + "pm_cycle_edit_init.xml")
    void updateExistingCycleInUnacceptableStatus() {
        var actualCycle = dao.read(CYCLE_EDIT_UUID, null);
        actualCycle.setName(UPDATED_NAME);
        int updated = dao.update(actualCycle, includeFilter(ACTIVE, INACTIVE, COMPLETED));
        assertEquals(0, updated);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "pm_cycle_init.xml",
            BASE_PATH_TO_DATA_SET + "pm_colleague_cycle_init.xml"})
    @ExpectedDataSet(value = BASE_PATH_TO_DATA_SET + "pm_update_cycle_status_expected_1.xml", compareOperation = CompareOperation.CONTAINS)
    void changeCycleStatus() {
        assertEquals(1, dao.updateStatus(CYCLE_UUID, PMCycleStatus.INACTIVE, null));
    }

    @Test
    @DataSet(BASE_PATH_TO_DATA_SET + "pm_cycle_init.xml")
    void getAllByStatus() {
        var rq = new RequestQuery();
        rq.addFilters("status_in", of("ACTIVE", "TERMINATED"));
        var actual = dao.findAll(rq, false);
        assertEquals(1, actual.size());
        assertEquals(CYCLE_UUID, actual.get(0).getUuid());
    }

    @Test
    @DataSet(BASE_PATH_TO_DATA_SET + "pm_cycle_init.xml")
    void getAllByTypeLowerCase() {
        var rq = new RequestQuery();
        rq.addFilters("type_ne", "hiring");
        var actual = dao.findAll(rq, false);
        assertEquals(1, actual.size());
        assertEquals(CYCLE_UUID, actual.get(0).getUuid());
    }

    @Test
    @DataSet(BASE_PATH_TO_DATA_SET + "pm_cycle_edit_init.xml")
    @ExpectedDataSet(value = BASE_PATH_TO_DATA_SET + "pm_cycle_edit_expected.xml", compareOperation = CompareOperation.CONTAINS)
    void update() {
        var actualCycle = dao.read(CYCLE_EDIT_UUID, null);
        actualCycle.setName(UPDATED_NAME);
        assertEquals(1, dao.update(actualCycle, null));
    }

    private PMCycle createCycle(UUID uuid) {
        File template = new File();
        template.setUuid(TEMPLATE_UUID);

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
                .template(template)
                .build();
    }
}