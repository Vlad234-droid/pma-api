package com.tesco.pma.reporting.review.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.reporting.review.domain.ObjectiveLinkedReviewReport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Instant;

import static com.tesco.pma.cycle.api.PMTimelinePointStatus.APPROVED;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.DECLINED;
import static com.tesco.pma.reporting.review.dao.ReviewReportingDAOTest.BASE_PATH_TO_DATA_SET;
import static org.assertj.core.api.Assertions.assertThat;

@DataSet({BASE_PATH_TO_DATA_SET + "colleague_init.xml",
          BASE_PATH_TO_DATA_SET + "pm_cycle_init.xml",
          BASE_PATH_TO_DATA_SET + "pm_colleague_cycle_init.xml",
          BASE_PATH_TO_DATA_SET + "pm_timeline_point_init.xml",
          BASE_PATH_TO_DATA_SET + "pm_review_init.xml"})
class ReviewReportingDAOTest extends AbstractDAOTest {

    static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/reporting/review/dao/";

    private static final String COLLEAGUE_UUID = "10000000-0000-0000-0000-000000000000";
    private static final Instant START_TIME = Instant.parse("2021-09-20T14:18:42.615Z");
    private static final Instant END_TIME = Instant.parse("2021-09-20T19:30:42.615Z");

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
        final var result = instance.getLinkedObjectivesData(START_TIME, END_TIME, APPROVED);

        assertThat(result).isNotNull();

        assertThat(result)
                .returns(COLLEAGUE_UUID, ObjectiveLinkedReviewReport::getColleagueUUID)
                .returns("string 2", ObjectiveLinkedReviewReport::getIamId)
                .returns("first_name", ObjectiveLinkedReviewReport::getFirstName)
                .returns("last_name", ObjectiveLinkedReviewReport::getLastName)
                .returns("WL4", ObjectiveLinkedReviewReport::getWorkLevel)
                .returns("Team lead", ObjectiveLinkedReviewReport::getJobTitle)
                .returns("first_name last_name", ObjectiveLinkedReviewReport::getLineManager)
                .returns(1, ObjectiveLinkedReviewReport::getObjectiveNumber)
                .returns(APPROVED.getCode(), ObjectiveLinkedReviewReport::getStatus)
                .returns("\"Title init\"", ObjectiveLinkedReviewReport::getObjectiveTitle)
                .returns("\"Strategic Priority\"", ObjectiveLinkedReviewReport::getStrategicPriority)
                .returns("\"How achieved objective\"", ObjectiveLinkedReviewReport::getHowAchieved)
                .returns("\"How overachieved objective\"", ObjectiveLinkedReviewReport::getHowOverAchieved);
    }

    @Test
    void getLinkedObjectivesDataNotExist() {
        final var result = instance.getLinkedObjectivesData(START_TIME, END_TIME, DECLINED);

        assertThat(result).isNull();
    }
}