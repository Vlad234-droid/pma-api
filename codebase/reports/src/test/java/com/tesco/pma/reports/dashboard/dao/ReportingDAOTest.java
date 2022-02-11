package com.tesco.pma.reports.dashboard.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.pagination.Condition;
import com.tesco.pma.pagination.RequestQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;

import static com.tesco.pma.pagination.Condition.Operand.EQUALS;
import static com.tesco.pma.pagination.Condition.Operand.IN;
import static com.tesco.pma.pagination.Condition.Operand.NOT_CONTAINS;
import static com.tesco.pma.reports.dashboard.dao.ReportingDAOTest.BASE_PATH_TO_DATA_SET;

import static com.tesco.pma.reports.ReportingConstants.EYR_HOW_RATING;
import static com.tesco.pma.reports.ReportingConstants.EYR_WHAT_RATING;
import static com.tesco.pma.reports.ReportingConstants.HAS_EYR_APPROVED;
import static com.tesco.pma.reports.ReportingConstants.HAS_EYR_APPROVED_1_QUARTER;
import static com.tesco.pma.reports.ReportingConstants.HAS_EYR_APPROVED_2_QUARTER;
import static com.tesco.pma.reports.ReportingConstants.HAS_EYR_APPROVED_3_QUARTER;
import static com.tesco.pma.reports.ReportingConstants.HAS_EYR_APPROVED_4_QUARTER;
import static com.tesco.pma.reports.ReportingConstants.HAS_EYR_SUBMITTED;
import static com.tesco.pma.reports.ReportingConstants.HAS_MYR_APPROVED;
import static com.tesco.pma.reports.ReportingConstants.HAS_MYR_SUBMITTED;
import static com.tesco.pma.reports.ReportingConstants.HAS_OBJECTIVE_APPROVED;
import static com.tesco.pma.reports.ReportingConstants.HAS_OBJECTIVE_SUBMITTED;
import static com.tesco.pma.reports.ReportingConstants.IS_NEW_TO_BUSINESS;
import static com.tesco.pma.reports.ReportingConstants.MUST_CREATE_EYR;
import static com.tesco.pma.reports.ReportingConstants.MUST_CREATE_MYR;
import static com.tesco.pma.reports.ReportingConstants.MUST_CREATE_OBJECTIVE;
import static com.tesco.pma.reports.ReportingConstants.MYR_HOW_RATING;
import static com.tesco.pma.reports.ReportingConstants.MYR_WHAT_RATING;

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
class ReportingDAOTest extends AbstractDAOTest {

    static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/reports/dashboard/dao/";

    private static final String COLLEAGUE_UUID = "10000000-0000-0000-0000-000000000000";
    private static final Integer YEAR = 2021;
    private static final String YEAR_PROPERTY = "year";
    private static final String MANAGER_UUID = "10000000-0000-0000-0000-00000000000a";

    @Autowired
    private ReportingDAO instance;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }

    @Test
    void getColleagueTargeting() {
        final var requestQuery = new RequestQuery();
        requestQuery.setFilters(List.of(new Condition(YEAR_PROPERTY, EQUALS, YEAR),
                new Condition("manager-uuid", EQUALS, MANAGER_UUID),
                new Condition("work-level", IN, List.of("WL4", "WL5")),
                new Condition("department-id", EQUALS, 2),
                new Condition("work-level", NOT_CONTAINS, "3"),
                new Condition("job-id", EQUALS, 2)));

        final var result = instance.getColleagueTargeting(requestQuery);

        assertNotNull(result);

        assertEquals(1, result.size());
        final var data = result.get(0);
        var tags = data.getTags();
        assertFalse(tags.isEmpty());

        assertAll("colleagueTargeting",
                () -> assertEquals(COLLEAGUE_UUID, data.getUuid().toString()),
                () -> assertEquals("first_name", data.getFirstName()),
                () -> assertEquals("last_name", data.getLastName()),
                () -> assertNull(data.getMiddleName()),
                () -> assertNull(data.getLineManager()),
                () -> assertEquals("Team lead2", data.getJobName()),
                () -> assertEquals("Bank", data.getBusinessType()),

                () -> assertEquals("1", tags.get(HAS_OBJECTIVE_APPROVED)),
                () -> assertEquals("0", tags.get(HAS_OBJECTIVE_SUBMITTED)),
                () -> assertEquals("1", tags.get(HAS_MYR_APPROVED)),
                () -> assertEquals("0", tags.get(HAS_EYR_APPROVED)),
                () -> assertEquals("0", tags.get(MYR_HOW_RATING)),
                () -> assertEquals("0", tags.get(MYR_WHAT_RATING)),
                () -> assertEquals("0", tags.get(EYR_HOW_RATING)),
                () -> assertEquals("0", tags.get(EYR_WHAT_RATING)),
                () -> assertEquals("0", tags.get(HAS_MYR_SUBMITTED)),
                () -> assertEquals("0", tags.get(HAS_EYR_SUBMITTED)),
                () -> assertEquals("0", tags.get(MUST_CREATE_EYR)),
                () -> assertEquals("1", tags.get(MUST_CREATE_OBJECTIVE)),
                () -> assertEquals("1", tags.get(MUST_CREATE_MYR)),
                () -> assertEquals("0", tags.get(IS_NEW_TO_BUSINESS)));
    }

    @Test
    void getColleagueTargetingNotExist() {
        final var requestQuery = new RequestQuery();
        requestQuery.setFilters(List.of(new Condition(YEAR_PROPERTY, EQUALS, 2022)));

        final var result = instance.getColleagueTargeting(requestQuery);

        assertTrue(result.isEmpty());
    }

    @Test
    void getColleagueTargetingAnniversary() {
        final var requestQuery = new RequestQuery();
        requestQuery.setFilters(List.of(new Condition(YEAR_PROPERTY, EQUALS, YEAR),
                new Condition("manager-uuid", IN, List.of(MANAGER_UUID)),
                new Condition("work-level", IN, List.of("WL4", "WL5")),
                new Condition("department-id", EQUALS, 1),
                new Condition("job-id", EQUALS, 1)));

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

                () -> assertEquals("1", tags.get(HAS_EYR_APPROVED_1_QUARTER)),
                () -> assertEquals("0", tags.get(HAS_EYR_APPROVED_2_QUARTER)),
                () -> assertEquals("0", tags.get(HAS_EYR_APPROVED_3_QUARTER)),
                () -> assertEquals("1", tags.get(HAS_EYR_APPROVED_4_QUARTER)));
    }

    @Test
    void getColleagueTargetingAnniversaryDataNotExist() {
        final var requestQuery = new RequestQuery();
        requestQuery.setFilters(List.of(new Condition(YEAR_PROPERTY, EQUALS, 2022)));

        final var result = instance.getColleagueTargetingAnniversary(requestQuery);

        assertTrue(result.isEmpty());
    }
}