package com.tesco.pma.reports.review.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.pagination.Condition;
import com.tesco.pma.pagination.RequestQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;

import static com.tesco.pma.cycle.api.PMTimelinePointStatus.APPROVED;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.DECLINED;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.DRAFT;
import static com.tesco.pma.pagination.Condition.Operand.EQUALS;
import static com.tesco.pma.pagination.Condition.Operand.IN;
import static com.tesco.pma.reports.review.dao.ReviewReportingDAOTest.BASE_PATH_TO_DATA_SET;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataSet({BASE_PATH_TO_DATA_SET + "colleague_init.xml",
          BASE_PATH_TO_DATA_SET + "pm_cycle_init.xml",
          BASE_PATH_TO_DATA_SET + "pm_colleague_cycle_init.xml",
          BASE_PATH_TO_DATA_SET + "pm_timeline_point_init.xml",
          BASE_PATH_TO_DATA_SET + "pm_review_init.xml"})
class ReviewReportingDAOTest extends AbstractDAOTest {

    static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/reports/review/dao/";

    private static final String COLLEAGUE_UUID = "10000000-0000-0000-0000-000000000000";
    private static final Integer YEAR = 2021;
    private static final String YEAR_PROPERTY = "year";
    private static final String STATUSES_PROPERTY = "statuses";

    @Autowired
    private ReviewReportingDAO instance;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }

    @Test
    void getLinkedObjectivesData() {
        final var requestQuery = new RequestQuery();
        requestQuery.setFilters(List.of(new Condition(YEAR_PROPERTY, EQUALS, YEAR),
                                        new Condition(STATUSES_PROPERTY, IN, List.of(APPROVED.getCode(), DRAFT.getCode()))));

        final var result = instance.getLinkedObjectivesData(requestQuery);

        assertNotNull(result);

        assertEquals(2, result.size());
        final var data = result.get(1);
        assertAll("report",
                () -> assertEquals(COLLEAGUE_UUID, data.getColleagueUUID()),
                () -> assertEquals("string 2", data.getIamId()),
                () -> assertEquals("first_name", data.getFirstName()),
                () -> assertEquals("last_name", data.getLastName()),
                () -> assertEquals("WL4", data.getWorkLevel()),
                () -> assertEquals("Team lead", data.getJobTitle()),
                () -> assertEquals("first_name last_name", data.getLineManager()),
                () -> assertEquals(1, data.getObjectiveNumber()),
                () -> assertEquals(APPROVED, data.getStatus()),
                () -> assertEquals("Title init", data.getObjectiveTitle()),
                () -> assertEquals("Strategic Priority", data.getStrategicPriority()),
                () -> assertEquals("How achieved objective", data.getHowAchieved()),
                () -> assertEquals("How overachieved objective", data.getHowOverAchieved()));
    }

    @Test
    void getLinkedObjectivesDataNotExist() {
        final var requestQuery = new RequestQuery();
        requestQuery.setFilters(List.of(new Condition(YEAR_PROPERTY, EQUALS, YEAR),
                                        new Condition(STATUSES_PROPERTY, IN, List.of(DECLINED.getCode()))));
        final var result = instance.getLinkedObjectivesData(requestQuery);

        assertTrue(result.isEmpty());
    }

    @Test
    void getColleagueTargeting() {
        final var requestQuery = new RequestQuery();
        requestQuery.setFilters(List.of(new Condition(YEAR_PROPERTY, EQUALS, YEAR),
                new Condition(STATUSES_PROPERTY, IN, List.of(APPROVED.getCode(), DRAFT.getCode()))));

        final var result = instance.getColleagueTargeting(requestQuery);

        assertNotNull(result);

        assertEquals(1, result.size());
        final var data = result.get(0);
        var tags = data.getTags();
        assertFalse(tags.isEmpty());

        assertAll("colleagueTargeting",
                () -> assertEquals("10000000-0000-0000-0000-000000000000", data.getUuid().toString()),
                () -> assertEquals("first_name", data.getFirstName()),
                () -> assertEquals("last_name", data.getLastName()),
                () -> assertNull(data.getMiddleName()),
                () -> assertNull(data.getLineManager()),
                () -> assertEquals("Team lead", data.getJobName()),
                () -> assertEquals("Bank", data.getBusinessType()),

                () -> assertEquals("1", tags.get("has_objective_approved")),
                () -> assertEquals("0", tags.get("has_objective_submitted")),
                () -> assertEquals("1", tags.get("has_myr_approved")),
                () -> assertEquals("0", tags.get("has_eyr_approved")),
                () -> assertEquals("0", tags.get("myr_how_rating")),
                () -> assertEquals("0", tags.get("myr_what_rating")),
                () -> assertEquals("0", tags.get("eyr_how_rating")),
                () -> assertEquals("0", tags.get("eyr_what_rating")),
                () -> assertEquals("0", tags.get("has_myr_submitted")),
                () -> assertEquals("0", tags.get("has_eyr_submitted")),
                () -> assertEquals("0", tags.get("must_create_eyr")),
                () -> assertEquals("1", tags.get("must_create_objective")),
                () -> assertEquals("1", tags.get("must_create_myr")),
                () -> assertEquals("0", tags.get("is_new_to_business")));
    }

    @Test
    void getColleagueTargetingNotExist() {
        final var requestQuery = new RequestQuery();
        requestQuery.setFilters(List.of(new Condition(YEAR_PROPERTY, EQUALS, 2022),
                new Condition(STATUSES_PROPERTY, IN, List.of(APPROVED.getCode(), DRAFT.getCode()))));

        final var result = instance.getColleagueTargeting(requestQuery);

        assertTrue(result.isEmpty());
    }

    @Test
    void getColleagueTargetingAnniversary() {
        final var requestQuery = new RequestQuery();
        requestQuery.setFilters(List.of(new Condition(YEAR_PROPERTY, EQUALS, YEAR)));

        final var result = instance.getColleagueTargetingAnniversary(requestQuery);

        assertNotNull(result);

        assertEquals(1, result.size());
        final var data = result.get(0);
        var tags = data.getTags();
        assertFalse(tags.isEmpty());

        assertAll("colleagueTargetingAnniversary",
                () -> assertEquals("40000000-0000-0000-0000-000000000000", data.getUuid().toString()),
                () -> assertEquals("first_name_2", data.getFirstName()),
                () -> assertEquals("last_name_2", data.getLastName()),
                () -> assertNull(data.getLineManager()),
                () -> assertEquals("Team lead", data.getJobName()),
                () -> assertEquals("Bank", data.getBusinessType()),

                () -> assertEquals("1", tags.get("has_eyr_approved_1_quarter")),
                () -> assertEquals("0", tags.get("has_eyr_approved_2_quarter")),
                () -> assertEquals("0", tags.get("has_eyr_approved_3_quarter")),
                () -> assertEquals("1", tags.get("has_eyr_approved_4_quarter")));
    }

    @Test
    void getColleagueTargetingAnniversaryDataNotExist() {
        final var requestQuery = new RequestQuery();
        requestQuery.setFilters(List.of(new Condition(YEAR_PROPERTY, EQUALS, 2022)));

        final var result = instance.getColleagueTargetingAnniversary(requestQuery);

        assertTrue(result.isEmpty());
    }
}