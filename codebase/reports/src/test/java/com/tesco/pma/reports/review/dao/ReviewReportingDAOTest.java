package com.tesco.pma.reports.review.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.pagination.Condition;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.reports.review.domain.ObjectiveLinkedReviewData;
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
import static org.assertj.core.api.Assertions.assertThat;

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
                                        new Condition("statuses", IN, List.of(APPROVED.getId(), DRAFT.getId()))));

        final var result = instance.getLinkedObjectivesData(requestQuery);

        assertThat(result).isNotNull();

        assertThat(result)
                .hasSize(2)
                .element(1)
                .returns(COLLEAGUE_UUID, ObjectiveLinkedReviewData::getColleagueUUID)
                .returns("string 2", ObjectiveLinkedReviewData::getIamId)
                .returns("first_name", ObjectiveLinkedReviewData::getFirstName)
                .returns("last_name", ObjectiveLinkedReviewData::getLastName)
                .returns("WL4", ObjectiveLinkedReviewData::getWorkLevel)
                .returns("Team lead", ObjectiveLinkedReviewData::getJobTitle)
                .returns("first_name last_name", ObjectiveLinkedReviewData::getLineManager)
                .returns(1, ObjectiveLinkedReviewData::getObjectiveNumber)
                .returns(APPROVED, ObjectiveLinkedReviewData::getStatus)
                .returns("\"Title init\"", ObjectiveLinkedReviewData::getObjectiveTitle)
                .returns("\"Strategic Priority\"", ObjectiveLinkedReviewData::getStrategicPriority)
                .returns("\"How achieved objective\"", ObjectiveLinkedReviewData::getHowAchieved)
                .returns("\"How overachieved objective\"", ObjectiveLinkedReviewData::getHowOverAchieved);
    }

    @Test
    void getLinkedObjectivesDataNotExist() {
        final var requestQuery = new RequestQuery();
        requestQuery.setFilters(List.of(new Condition("year", EQUALS, YEAR),
                                        new Condition("statuses", IN, List.of(DECLINED.getId()))));
        final var result = instance.getLinkedObjectivesData(requestQuery);

        assertThat(result).isEmpty();
    }
}