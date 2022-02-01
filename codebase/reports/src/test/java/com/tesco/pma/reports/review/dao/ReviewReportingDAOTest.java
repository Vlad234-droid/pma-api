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
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        requestQuery.setFilters(List.of(new Condition("year", EQUALS, YEAR),
                                        new Condition("statuses", IN, List.of(APPROVED.getCode(), DRAFT.getCode()))));

        final var result = instance.getLinkedObjectivesData(requestQuery);

        assertNotNull(result);

        assertEquals(2, result.size());

        assertAll("report",
                () -> assertEquals(COLLEAGUE_UUID, result.get(1).getColleagueUUID()),
                () -> assertEquals("string 2", result.get(1).getIamId()),
                () -> assertEquals("first_name", result.get(1).getFirstName()),
                () -> assertEquals("last_name", result.get(1).getLastName()),
                () -> assertEquals("WL4", result.get(1).getWorkLevel()),
                () -> assertEquals("Team lead", result.get(1).getJobTitle()),
                () -> assertEquals("first_name last_name", result.get(1).getLineManager()),
                () -> assertEquals(1, result.get(1).getObjectiveNumber()),
                () -> assertEquals(APPROVED, result.get(1).getStatus()),
                () -> assertEquals("\"Title init\"", result.get(1).getObjectiveTitle()),
                () -> assertEquals("\"Strategic Priority\"", result.get(1).getStrategicPriority()),
                () -> assertEquals("\"How achieved objective\"", result.get(1).getHowAchieved()),
                () -> assertEquals("\"How overachieved objective\"", result.get(1).getHowOverAchieved()));
    }

    @Test
    void getLinkedObjectivesDataNotExist() {
        final var requestQuery = new RequestQuery();
        requestQuery.setFilters(List.of(new Condition("year", EQUALS, YEAR),
                                        new Condition("statuses", IN, List.of(DECLINED.getCode()))));
        final var result = instance.getLinkedObjectivesData(requestQuery);

        assertTrue(result.isEmpty());
    }

    @Test
    void getColleagueTargeting() {
        final var requestQuery = new RequestQuery();
        requestQuery.setFilters(List.of(new Condition("year", EQUALS, YEAR),
                new Condition("statuses", IN, List.of(APPROVED.getCode(), DRAFT.getCode()))));

        final var result = instance.getColleagueTargeting(requestQuery);

        System.out.println(result);
    }
}