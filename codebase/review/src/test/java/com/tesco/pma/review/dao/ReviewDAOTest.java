package com.tesco.pma.review.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.tesco.pma.api.MapJson;
import com.tesco.pma.cycle.api.PMReviewStatus;
import com.tesco.pma.dao.AbstractDAOTest;
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
import static com.tesco.pma.cycle.api.PMReviewType.MYR;
import static com.tesco.pma.cycle.api.PMReviewType.OBJECTIVE;
import static com.tesco.pma.cycle.api.model.PMElementType.REVIEW;
import static com.tesco.pma.cycle.api.model.PMElementType.TIMELINE_POINT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

class ReviewDAOTest extends AbstractDAOTest {
    private static final UUID REVIEW_UUID = UUID.fromString("ddb9ab0b-f50f-4442-8900-b03777ee0011");
    private static final UUID COLLEAGUE_UUID = UUID.fromString("ccb9ab0b-f50f-4442-8900-b03777ee00ec");
    private static final UUID COLLEAGUE_UUID_NOT_EXIST = UUID.fromString("ccb9ab0b-f50f-4442-8900-000000000000");
    private static final UUID PERFORMANCE_CYCLE_UUID = UUID.fromString("0c5d9cb1-22cf-4fcd-a19a-9e70df6bc941");
    private static final Integer NUMBER_1 = 1;
    private static final Integer NUMBER_2 = 2;
    private static final String TITLE_PROPERTY_NAME = "title";
    private static final String TITLE_UPDATE = "Title update";
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
    private static final UUID MANAGER_UUID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID MANAGER_UUID_2 = UUID.fromString("10000000-0000-0000-0000-000000000002");

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
    @DataSet({"pm_cycle_init.xml", "review_init.xml"})
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
    @DataSet({"pm_cycle_init.xml", "review_init.xml"})
    void getReviews() {
        final var result = instance.getReviews(
                PERFORMANCE_CYCLE_UUID,
                COLLEAGUE_UUID,
                MYR);

        assertThat(result.get(0))
                .asInstanceOf(type(Review.class))
                .returns(COLLEAGUE_UUID, from(Review::getColleagueUuid))
                .returns(PERFORMANCE_CYCLE_UUID, from(Review::getPerformanceCycleUuid))
                .returns(MYR, from(Review::getType))
                .returns(NUMBER_1, from(Review::getNumber))
                .returns(REVIEW_PROPERTIES_INIT, from(Review::getProperties))
                .returns(DRAFT, from(Review::getStatus));
    }

    @Test
    @DataSet({"pm_cycle_init.xml", "review_init.xml"})
    void getReviewsByColleague() {
        final var result = instance.getReviewsByColleague(
                PERFORMANCE_CYCLE_UUID,
                COLLEAGUE_UUID);

        assertThat(result.size())
                .isEqualTo(3);
    }

    @Test
    @DataSet({"pm_cycle_init.xml", "review_init.xml"})
    void getReviewByUuid() {
        final var result = instance.read(REVIEW_UUID);

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
    @DataSet({"pm_cycle_init.xml", "review_init.xml"})
    void getReviewNotExist() {
        final var result = instance.getReview(
                PERFORMANCE_CYCLE_UUID,
                COLLEAGUE_UUID_NOT_EXIST,
                OBJECTIVE,
                NUMBER_1);

        assertThat(result).isNull();
    }

    @Test
    @DataSet({"pm_cycle_init.xml", "cleanup.xml"})
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

        final int rowsInserted = instance.create(review);

        assertThat(rowsInserted).isOne();
    }

    @Test
    @DataSet({"pm_cycle_init.xml", "review_init.xml"})
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

        Assertions.assertThatThrownBy(() -> instance.create(review))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    @DataSet({"pm_cycle_init.xml", "review_init.xml"})
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
    @DataSet({"pm_cycle_init.xml", "review_init.xml"})
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
    @DataSet({"review_init.xml"})
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
    @DataSet({"pm_cycle_init.xml", "review_init.xml"})
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

        final var result = instance.update(review, List.of(DRAFT, DECLINED, APPROVED));

        assertThat(result).isOne();
    }

    @Test
    @DataSet({"pm_cycle_init.xml", "review_init.xml"})
    void updateReviewNotExist() {
        final var review = Review.builder()
                .colleagueUuid(COLLEAGUE_UUID_NOT_EXIST)
                .performanceCycleUuid(PERFORMANCE_CYCLE_UUID)
                .type(OBJECTIVE)
                .number(NUMBER_1)
                .properties(REVIEW_PROPERTIES_UPDATE)
                .status(WAITING_FOR_APPROVAL)
                .build();

        final var result = instance.update(review, List.of(DRAFT, DECLINED, APPROVED));

        assertThat(result).isZero();
    }

    @Test
    @DataSet({"pm_cycle_init.xml", "review_init.xml"})
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
    @DataSet({"pm_cycle_init.xml", "review_init.xml"})
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

    @Test
    @DataSet({"pm_cycle_init.xml", "objective_sharing_init.xml"})
    void shareManagerObjective() {
        var managerShareObjectives = instance.isManagerShareObjectives(MANAGER_UUID, PERFORMANCE_CYCLE_UUID);
        Assertions.assertThat(managerShareObjectives).isFalse();

        var count = instance.shareManagerObjective(MANAGER_UUID, PERFORMANCE_CYCLE_UUID);
        assertThat(count).isEqualTo(1);

        managerShareObjectives = instance.isManagerShareObjectives(MANAGER_UUID, PERFORMANCE_CYCLE_UUID);
        Assertions.assertThat(managerShareObjectives).isTrue();
    }

    @Test
    @DataSet({"pm_cycle_init.xml", "objective_sharing_init.xml"})
    void stopSharingManagerObjective() {
        var managerShareObjectives = instance.isManagerShareObjectives(MANAGER_UUID_2, PERFORMANCE_CYCLE_UUID);
        Assertions.assertThat(managerShareObjectives).isTrue();

        var count = instance.stopSharingManagerObjective(MANAGER_UUID_2, PERFORMANCE_CYCLE_UUID);
        assertThat(count).isEqualTo(1);

        managerShareObjectives = instance.isManagerShareObjectives(MANAGER_UUID_2, PERFORMANCE_CYCLE_UUID);
        Assertions.assertThat(managerShareObjectives).isFalse();
    }

    @Test
    @DataSet({"pm_cycle_init.xml", "objective_sharing_init.xml"})
    void getManagerSharedObjectives() {
        var reviews = instance.getManagerSharedObjectives(MANAGER_UUID_2, PERFORMANCE_CYCLE_UUID);

        assertThat(reviews)
                .singleElement()
                .asInstanceOf(type(Review.class))
                .returns(MANAGER_UUID_2, from(Review::getColleagueUuid))
                .returns(PERFORMANCE_CYCLE_UUID, from(Review::getPerformanceCycleUuid))
                .returns(OBJECTIVE, from(Review::getType))
                .returns(NUMBER_1, from(Review::getNumber))
                .returns(REVIEW_PROPERTIES_INIT, from(Review::getProperties))
                .returns(APPROVED, from(Review::getStatus));
    }
}
