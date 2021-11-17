package com.tesco.pma.review.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.tesco.pma.api.OrgObjectiveStatus;
import com.tesco.pma.api.MapJson;
import com.tesco.pma.cycle.api.PMReviewStatus;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.review.domain.OrgObjective;
import com.tesco.pma.review.domain.PMCycleReviewTypeProperties;
import com.tesco.pma.review.domain.PMCycleTimelinePoint;
import com.tesco.pma.review.domain.Review;
import com.tesco.pma.review.domain.ReviewStats;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.cycle.api.PMReviewStatus.APPROVED;
import static com.tesco.pma.cycle.api.PMReviewStatus.DECLINED;
import static com.tesco.pma.cycle.api.PMReviewStatus.DRAFT;
import static com.tesco.pma.cycle.api.PMReviewStatus.WAITING_FOR_APPROVAL;
import static com.tesco.pma.cycle.api.PMReviewType.OBJECTIVE;
import static com.tesco.pma.cycle.api.model.PMElementType.REVIEW;
import static com.tesco.pma.cycle.api.model.PMElementType.TIMELINE_POINT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

class ReviewDAOTest extends AbstractDAOTest {

    private static final UUID ORG_OBJECTIVE_UUID = UUID.fromString("aab9ab0b-f50f-4442-8900-b03777ee0012");
    private static final UUID ORG_OBJECTIVE_UUID_2 = UUID.fromString("aab9ab0b-f50f-4442-8900-b03777ee0011");
    private static final UUID ORG_OBJECTIVE_UUID_NOT_EXIST = UUID.fromString("aab9ab0b-f50f-4442-8900-000000000000");
    private static final UUID REVIEW_UUID = UUID.fromString("ddb9ab0b-f50f-4442-8900-b03777ee0011");
    private static final UUID COLLEAGUE_UUID = UUID.fromString("ccb9ab0b-f50f-4442-8900-b03777ee00ec");
    private static final UUID COLLEAGUE_UUID_NOT_EXIST = UUID.fromString("ccb9ab0b-f50f-4442-8900-000000000000");
    private static final UUID PERFORMANCE_CYCLE_UUID = UUID.fromString("0c5d9cb1-22cf-4fcd-a19a-9e70df6bc941");
    private static final Integer NUMBER_1 = 1;
    private static final Integer NUMBER_2 = 2;
    private static final String TITLE_PROPERTY_NAME = "title";
    private static final String TITLE_1 = "Title #1";
    private static final String TITLE_UPDATE = "Title update";
    private static final String ORG_TITLE_UPDATE = "Title #1 updated";
    private static final String TITLE_INIT = "Title init";
    private static final String DESCRIPTION_PROPERTY_NAME = "description";
    private static final String DESCRIPTION_INIT = "Description init";
    private static final String DESCRIPTION_UPDATE = "Description update";
    private static final String MEETS_PROPERTY_NAME = "meets";
    private static final String MEETS_INIT = "Meets init";
    private static final String MEETS_UPDATE = "Meets update";
    private static final String EXCEEDS_PROPERTY_NAME = "exceeds";
    private static final String EXCEEDS_INIT = "Exceeds init";
    private static final String EXCEEDS_UPDATE = "Exceeds update";
    private static final Integer VERSION_1 = 1;
    private static final Integer VERSION_3 = 3;
    private static final String USER_INIT = "Init user";
    private static final String TIME_INIT = "2021-09-20 10:45:12.448057";
    private static final String OBJECTIVES_CODE_NAME = "Objectives";
    private static final String Q3_CODE_NAME = "Q3";
    private static final MapJson REVIEW_PROPERTIES_INIT = new MapJson(
            Map.of(TITLE_PROPERTY_NAME, TITLE_INIT,
                    DESCRIPTION_PROPERTY_NAME, DESCRIPTION_INIT,
                    MEETS_PROPERTY_NAME, MEETS_INIT,
                    EXCEEDS_PROPERTY_NAME, EXCEEDS_INIT
            ));
    private static final MapJson REVIEW_PROPERTIES_UPDATE = new MapJson(
            Map.of(TITLE_PROPERTY_NAME, TITLE_UPDATE,
                    DESCRIPTION_PROPERTY_NAME, DESCRIPTION_UPDATE,
                    MEETS_PROPERTY_NAME, MEETS_UPDATE,
                    EXCEEDS_PROPERTY_NAME, EXCEEDS_UPDATE
            ));

    @Autowired
    private ReviewDAO instance;

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
    @ExpectedDataSet("org_objective_create_expected_1.xml")
    void createOrgObjectiveSucceeded() {

        final var orgObjective = OrgObjective.builder()
                .uuid(ORG_OBJECTIVE_UUID)
                .number(NUMBER_1)
                .status(OrgObjectiveStatus.DRAFT)
                .title(TITLE_1)
                .version(VERSION_1)
                .build();

        final int rowsInserted = instance.createOrgObjective(orgObjective);

        assertThat(rowsInserted).isOne();
    }

    @Test
    @DataSet("org_objective_init.xml")
    void createOrgObjectiveAlreadyExist() {

        final var orgObjective = OrgObjective.builder()
                .uuid(ORG_OBJECTIVE_UUID_2)
                .number(NUMBER_1)
                .status(OrgObjectiveStatus.DRAFT)
                .title(TITLE_1)
                .version(VERSION_1)
                .build();

        Assertions.assertThatThrownBy(() -> instance.createOrgObjective(orgObjective))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    @DataSet("org_objective_init.xml")
    void getOrgObjective() {
        final var result = instance.getOrgObjective(ORG_OBJECTIVE_UUID_2);

        assertThat(result)
                .asInstanceOf(type(OrgObjective.class))
                .returns(NUMBER_1, from(OrgObjective::getNumber))
                .returns(OrgObjectiveStatus.DRAFT, from(OrgObjective::getStatus))
                .returns(VERSION_1, from(OrgObjective::getVersion));
    }

    @Test
    @DataSet("org_objective_init.xml")
    void getOrgObjectiveNotExist() {
        final var result = instance.getOrgObjective(ORG_OBJECTIVE_UUID_NOT_EXIST);

        assertThat(result).isNull();
    }

    @Test
    @DataSet("org_objective_init.xml")
    void getOrgObjectives() {
        final var result = instance.getOrgObjectives();

        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(1);

        assertThat(result.get(0))
                .returns(NUMBER_1, from(OrgObjective::getNumber))
                .returns(OrgObjectiveStatus.DRAFT, from(OrgObjective::getStatus))
                .returns(ORG_TITLE_UPDATE, from(OrgObjective::getTitle))
                .returns(VERSION_3, from(OrgObjective::getVersion));
    }

    @Test
    @DataSet("org_objective_init.xml")
    void deleteOrgObjectiveNotExist() {
        final var result = instance.deleteOrgObjective(ORG_OBJECTIVE_UUID_NOT_EXIST);
        assertThat(result).isZero();
    }

    @Test
    @DataSet("org_objective_init.xml")
    void deleteOrgObjectiveSucceeded() {
        final var result = instance.deleteOrgObjective(ORG_OBJECTIVE_UUID_2);
        assertThat(result).isOne();
    }

    @Test
    @DataSet("org_objective_init.xml")
    void getMaxVersionOrgObjective() {
        final var result = instance.getMaxVersionOrgObjective();
        assertThat(result).isEqualTo(3);
    }

    @Test
    @DataSet("org_objective_init.xml")
    @ExpectedDataSet("org_objective_publish_expected_1.xml")
    void publishOrgObjectiveSucceeded() {
        final var result = instance.publishOrgObjectives();
        assertThat(result).isOne();
    }

    @Test
    @DataSet("org_objective_init.xml")
    @ExpectedDataSet("org_objective_unpublish_expected_1.xml")
    void unpublishOrgObjectiveSucceeded() {
        final var result = instance.unpublishOrgObjectives();
        assertThat(result).isOne();
    }

    @Test
    @DataSet({"org_objective_init.xml", "pm_cycle_init.xml", "review_init.xml"})
    void getReview() {
        final var result = instance.getReview(
                PERFORMANCE_CYCLE_UUID,
                COLLEAGUE_UUID,
                OBJECTIVE,
                NUMBER_1);

        assertThat(result)
                .asInstanceOf(type(Review.class))
                .returns(COLLEAGUE_UUID, from(Review::getColleagueUuid))
                .returns(PERFORMANCE_CYCLE_UUID, from(Review::getPerformanceCycleUuid))
                .returns(OBJECTIVE, from(Review::getType))
                .returns(NUMBER_1, from(Review::getNumber))
                .returns(REVIEW_PROPERTIES_INIT, from(Review::getProperties))
                .returns(DRAFT, from(Review::getStatus));
    }

    @Test
    @DataSet({"org_objective_init.xml", "pm_cycle_init.xml", "review_init.xml"})
    void getReviewNotExist() {
        final var result = instance.getReview(
                PERFORMANCE_CYCLE_UUID,
                COLLEAGUE_UUID_NOT_EXIST,
                OBJECTIVE,
                NUMBER_1);

        assertThat(result).isNull();
    }

    @Test
    @DataSet({"org_objective_init.xml", "pm_cycle_init.xml", "cleanup.xml"})
    @ExpectedDataSet("review_create_expected_1.xml")
    void createReviewSucceeded() {
        final var review = Review.builder()
                .uuid(REVIEW_UUID)
                .colleagueUuid(COLLEAGUE_UUID)
                .performanceCycleUuid(PERFORMANCE_CYCLE_UUID)
                .type(OBJECTIVE)
                .number(NUMBER_1)
                .properties(REVIEW_PROPERTIES_INIT)
                .status(PMReviewStatus.DRAFT)
                .build();

        final int rowsInserted = instance.createReview(review);

        assertThat(rowsInserted).isOne();
    }

    @Test
    @DataSet({"org_objective_init.xml", "pm_cycle_init.xml", "review_init.xml"})
    void createReviewAlreadyExist() {

        final var review = Review.builder()
                .uuid(REVIEW_UUID)
                .colleagueUuid(COLLEAGUE_UUID)
                .performanceCycleUuid(PERFORMANCE_CYCLE_UUID)
                .type(OBJECTIVE)
                .number(NUMBER_1)
                .properties(REVIEW_PROPERTIES_INIT)
                .status(PMReviewStatus.DRAFT)
                .build();

        Assertions.assertThatThrownBy(() -> instance.createReview(review))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    @DataSet({"org_objective_init.xml", "pm_cycle_init.xml", "review_init.xml"})
    void deleteReviewNotExist() {
        final var result = instance.deleteReview(
                PERFORMANCE_CYCLE_UUID,
                COLLEAGUE_UUID_NOT_EXIST,
                OBJECTIVE,
                NUMBER_1,
                List.of(DRAFT, DECLINED, APPROVED));
        assertThat(result).isZero();
    }

    @Test
    @DataSet({"org_objective_init.xml", "pm_cycle_init.xml", "review_init.xml"})
    void deleteReviewSucceeded() {
        final var result = instance.deleteReview(
                PERFORMANCE_CYCLE_UUID,
                COLLEAGUE_UUID,
                OBJECTIVE,
                NUMBER_1,
                List.of(DRAFT, DECLINED, APPROVED));
        assertThat(result).isOne();
    }

    @Test
    @DataSet({"org_objective_init.xml", "review_init.xml"})
    @ExpectedDataSet("review_delete_renumerate_expected_1.xml")
    void deleteAndRenumerateReviewsSucceeded() {
        instance.deleteReview(
                PERFORMANCE_CYCLE_UUID,
                COLLEAGUE_UUID,
                OBJECTIVE,
                NUMBER_1,
                List.of(DRAFT, DECLINED, APPROVED));
        final var result = instance.renumerateReviews(
                PERFORMANCE_CYCLE_UUID,
                COLLEAGUE_UUID,
                OBJECTIVE,
                NUMBER_2);
        assertThat(result).isOne();
    }

    @Test
    @DataSet({"org_objective_init.xml", "pm_cycle_init.xml", "review_init.xml"})
    @ExpectedDataSet("review_update_expected_1.xml")
    void updateReviewSucceeded() {
        final var review = Review.builder()
                .uuid(REVIEW_UUID)
                .colleagueUuid(COLLEAGUE_UUID)
                .performanceCycleUuid(PERFORMANCE_CYCLE_UUID)
                .type(OBJECTIVE)
                .number(NUMBER_1)
                .properties(REVIEW_PROPERTIES_UPDATE)
                .status(DRAFT)
                .build();

        final var result = instance.updateReview(review, List.of(DRAFT, DECLINED, APPROVED));

        assertThat(result).isOne();
    }

    @Test
    @DataSet({"org_objective_init.xml", "pm_cycle_init.xml", "review_init.xml"})
    void updateReviewNotExist() {
        final var review = Review.builder()
                .colleagueUuid(COLLEAGUE_UUID_NOT_EXIST)
                .performanceCycleUuid(PERFORMANCE_CYCLE_UUID)
                .type(OBJECTIVE)
                .number(NUMBER_1)
                .properties(REVIEW_PROPERTIES_UPDATE)
                .status(WAITING_FOR_APPROVAL)
                .build();

        final var result = instance.updateReview(review, List.of(DRAFT, DECLINED, APPROVED));

        assertThat(result).isZero();
    }

    @Test
    @DataSet({"org_objective_init.xml", "pm_cycle_init.xml", "review_init.xml"})
    @ExpectedDataSet("review_update_status_1.xml")
    void updateReviewStatusSucceeded() {

        final var result = instance.updateReviewStatus(
                PERFORMANCE_CYCLE_UUID,
                COLLEAGUE_UUID,
                OBJECTIVE,
                NUMBER_1,
                WAITING_FOR_APPROVAL,
                Collections.singleton(DRAFT));

        assertThat(result).isOne();
    }

    @Test
    @DataSet({"org_objective_init.xml", "pm_cycle_init.xml", "review_init.xml"})
    @ExpectedDataSet("review_unlink_org_objective_expected.xml")
    void updateReviewUnlinkOrgObjective() {
        final var review = Review.builder()
                .uuid(REVIEW_UUID)
                .colleagueUuid(COLLEAGUE_UUID)
                .performanceCycleUuid(PERFORMANCE_CYCLE_UUID)
                .type(OBJECTIVE)
                .number(NUMBER_1)
                .properties(REVIEW_PROPERTIES_INIT)
                .status(PMReviewStatus.DRAFT)
                .build();

        final var result = instance.updateReview(review, List.of(DRAFT, DECLINED, APPROVED));

        assertThat(result).isOne();
    }

    @Test
    @DataSet({"pm_cycle_init.xml", "cleanup.xml"})
    void getPMCycleReviewProperties() {
        final var result = instance.getPMCycleReviewTypeProperties(PERFORMANCE_CYCLE_UUID, OBJECTIVE);

        assertThat(result)
                .asInstanceOf(type(PMCycleReviewTypeProperties.class))
                .returns(PERFORMANCE_CYCLE_UUID, from(PMCycleReviewTypeProperties::getCycleUuid))
                .returns(OBJECTIVE, from(PMCycleReviewTypeProperties::getType))
                .returns(3, from(PMCycleReviewTypeProperties::getMin))
                .returns(5, from(PMCycleReviewTypeProperties::getMax));
    }

    @Test
    @DataSet({"org_objective_init.xml", "pm_cycle_init.xml", "review_init.xml"})
    void getReviewStats() {
        final var result = instance.getReviewStats(
                PERFORMANCE_CYCLE_UUID,
                COLLEAGUE_UUID,
                OBJECTIVE);

        assertThat(result)
                .asInstanceOf(type(ReviewStats.class))
                .returns(PERFORMANCE_CYCLE_UUID, from(ReviewStats::getCycleUuid))
                .returns(COLLEAGUE_UUID, from(ReviewStats::getColleagueUuid))
                .returns(OBJECTIVE, from(ReviewStats::getType))
                .returns(Map.of(DRAFT, 2), from(ReviewStats::getMapStatusStats));
    }

    @Test
    @DataSet({"pm_cycle_init.xml", "cleanup.xml"})
    void getTimeline() {
        final var result = instance.getTimeline(PERFORMANCE_CYCLE_UUID);

        final var objectives = PMCycleTimelinePoint.builder()
                .cycleUuid(PERFORMANCE_CYCLE_UUID)
                .code(OBJECTIVES_CODE_NAME)
                .description(OBJECTIVES_CODE_NAME)
                .type(REVIEW)
                .reviewType(OBJECTIVE)
                .startDate(LocalDate.of(2021, 4, 1))
                .build();

        final var q3 = PMCycleTimelinePoint.builder()
                .cycleUuid(PERFORMANCE_CYCLE_UUID)
                .code(Q3_CODE_NAME)
                .description(Q3_CODE_NAME)
                .type(TIMELINE_POINT)
                .startDate(LocalDate.of(2022, 1, 1))
                .build();

        assertThat(result.get(0))
                .isEqualTo(objectives);

        assertThat(result.get(3))
                .isEqualTo(q3);
    }
}
