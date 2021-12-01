package com.tesco.pma.review.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.tesco.pma.colleague.api.ColleagueSimple;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.review.domain.AuditOrgObjectiveReport;
import com.tesco.pma.review.domain.Review;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Instant;
import java.util.UUID;

import static com.tesco.pma.api.ActionType.PUBLISH;
import static com.tesco.pma.api.ActionType.SAVE_AS_DRAFT;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.APPROVED;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.WAITING_FOR_APPROVAL;
import static org.assertj.core.api.Assertions.assertThat;

class ReviewAuditLogDAOTest extends AbstractDAOTest {

    private static final UUID COLLEAGUE_UUID = UUID.fromString("10000000-0000-0000-0000-000000000000");
    private static final Instant UPDATE_TIME = Instant.parse("2021-11-18T18:35:24.00Z");
    private static final Instant UPDATE_TIME_2 = Instant.parse("2021-11-18T19:45:24.00Z");
    private static final UUID REVIEW_UUID = UUID.fromString("ddb9ab0b-f50f-4442-8900-b03777ee0011");
    private static final String CHANGE_REASON = "Test reason";
    private static final String FIRST_NAME = "First";
    private static final String LAST_NAME = "Last";
    private static final String MIDDLE_NAME = "Middle";
    private static final Integer LIMIT = 3;
    private static final Integer OFFSET = 0;

    @Autowired
    private ReviewAuditLogDAO instance;

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
    @ExpectedDataSet("pm_org_objective_action_hi_expected_1.xml")
    void intLogOrgObjectiveAction() {
        final var result = instance.intLogOrgObjectiveAction(SAVE_AS_DRAFT, COLLEAGUE_UUID, UPDATE_TIME);
        assertThat(result).isOne();
    }

    @Test
    @DataSet("cleanup.xml")
    @ExpectedDataSet("pm_review_change_status_hi_expected_1.xml")
    void intLogReviewUpdating() {
        final var review = Review.builder()
                .uuid(REVIEW_UUID)
                .status(WAITING_FOR_APPROVAL)
                .build();

        final var result = instance.intLogReviewUpdating(
                review,
                APPROVED,
                CHANGE_REASON,
                COLLEAGUE_UUID,
                UPDATE_TIME);

        assertThat(result).isOne();
    }

    @Test
    @DataSet({"pm_org_objective_action_hi_init.xml", "colleague_init.xml", "cleanup.xml"})
    void getAuditOrgObjectiveReport() {
        var requestQuery = new RequestQuery();
        requestQuery.setLimit(LIMIT);
        requestQuery.setOffset(0);
        final var colleagueSimple = ColleagueSimple.builder()
                .uuid(COLLEAGUE_UUID)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .middleName(MIDDLE_NAME)
                .build();
        final var auditOrgObjectiveReportBuilder = AuditOrgObjectiveReport.builder()
                .updatedBy(colleagueSimple)
                .updatedTime(UPDATE_TIME_2)
                .action(PUBLISH)
                .build();
        final var result = instance.getAuditOrgObjectiveReport(requestQuery);
        assertThat(result.size()).isEqualTo(3);
        assertThat(result.get(0)).isEqualTo(auditOrgObjectiveReportBuilder);
    }

}
