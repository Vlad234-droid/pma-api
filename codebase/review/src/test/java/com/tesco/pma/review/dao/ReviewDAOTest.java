package com.tesco.pma.review.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.tesco.pma.api.MapJson;
import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.review.domain.ColleagueTimeline;
import com.tesco.pma.review.domain.PMCycleReviewTypeProperties;
import com.tesco.pma.review.domain.PMCycleTimelinePoint;
import com.tesco.pma.review.domain.Review;
import com.tesco.pma.review.domain.ReviewStats;
import com.tesco.pma.review.domain.SimplifiedReview;
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

import static com.tesco.pma.cycle.api.PMTimelinePointStatus.APPROVED;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.DECLINED;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.DRAFT;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.WAITING_FOR_APPROVAL;
import static com.tesco.pma.cycle.api.PMReviewType.MYR;
import static com.tesco.pma.cycle.api.PMReviewType.OBJECTIVE;
import static com.tesco.pma.cycle.api.model.PMElementType.REVIEW;
import static com.tesco.pma.cycle.api.model.PMElementType.TIMELINE_POINT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

class ReviewDAOTest extends AbstractDAOTest {
    private static final UUID REVIEW_UUID = UUID.fromString("ddb9ab0b-f50f-4442-8900-b03777ee0011");
    private static final UUID DECLINED_REVIEW_UUID = UUID.fromString("ddb9ab0b-f50f-4442-8900-b03777ee0012");
    private static final UUID MYR_REVIEW_UUID = UUID.fromString("ddb9ab0b-f50f-4442-8900-b03777ee0025");
    private static final UUID REVIEW_UUID_NOT_EXIST = UUID.fromString("ddb9ab0b-f50f-4442-8900-000000000000");
    private static final UUID TIMELINE_POINT_UUID = UUID.fromString("10000000-0000-0000-1000-000000000000");
    private static final UUID MYR_TIMELINE_POINT_UUID = UUID.fromString("10000000-0000-0000-2000-000000000000");
    private static final UUID MANAGER_UUID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID COLLEAGUE_UUID = UUID.fromString("10000000-0000-0000-0000-000000000000");
    private static final UUID TIMELINE_POINT_UUID_NOT_EXIST = UUID.fromString("ccb9ab0b-f50f-4442-8900-000000000000");
    private static final UUID CYCLE_UUID = UUID.fromString("10000000-1000-0000-0000-000000000000");
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
    private static final String CHANGE_STATUS_REASON_DECLINED = "To DECLINED 2";
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
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "pm_colleague_cycle_init.xml",
            "pm_timeline_point_init.xml"})
    @ExpectedDataSet("pm_review_create_expected_1.xml")
    void createSucceeded() {
        final var review = Review.builder()
                .uuid(REVIEW_UUID)
                .tlPointUuid(TIMELINE_POINT_UUID)
                .type(OBJECTIVE)
                .number(NUMBER_1)
                .properties(REVIEW_PROPERTIES_INIT)
                .status(PMTimelinePointStatus.DRAFT)
                .build();

        final int rowsInserted = instance.create(review);

        assertThat(rowsInserted).isOne();
    }

    @Test
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "pm_colleague_cycle_init.xml",
            "pm_timeline_point_init.xml",
            "pm_review_init.xml"})
    void createAlreadyExist() {

        final var review = Review.builder()
                .uuid(REVIEW_UUID)
                .tlPointUuid(TIMELINE_POINT_UUID)
                .type(OBJECTIVE)
                .number(NUMBER_1)
                .properties(REVIEW_PROPERTIES_INIT)
                .status(PMTimelinePointStatus.DRAFT)
                .build();

        Assertions.assertThatThrownBy(() -> instance.create(review))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "pm_colleague_cycle_init.xml",
            "pm_timeline_point_init.xml",
            "pm_review_init.xml"})
    void read() {
        final var result = instance.read(REVIEW_UUID);

        assertThat(result)
                .asInstanceOf(type(Review.class))
                .returns(TIMELINE_POINT_UUID, from(Review::getTlPointUuid))
                .returns(OBJECTIVE, from(Review::getType))
                .returns(NUMBER_1, from(Review::getNumber))
                .returns(REVIEW_PROPERTIES_INIT, from(Review::getProperties))
                .returns(DRAFT, from(Review::getStatus));
    }


    @Test
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "pm_colleague_cycle_init.xml",
            "pm_timeline_point_init.xml",
            "pm_review_init.xml",
            "pm_review_change_status_hi_init.xml"})
    void getReviewDeclinedChangeReason() {
        final var result = instance.read(DECLINED_REVIEW_UUID);

        assertThat(result)
                .asInstanceOf(type(Review.class))
                .returns(TIMELINE_POINT_UUID, from(Review::getTlPointUuid))
                .returns(OBJECTIVE, from(Review::getType))
                .returns(NUMBER_2, from(Review::getNumber))
                .returns(REVIEW_PROPERTIES_INIT, from(Review::getProperties))
                .returns(DECLINED, from(Review::getStatus))
                .returns(CHANGE_STATUS_REASON_DECLINED, from(Review::getChangeStatusReason));
    }


    @Test
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "pm_colleague_cycle_init.xml",
            "pm_timeline_point_init.xml",
            "pm_review_init.xml"})
    @ExpectedDataSet("pm_review_update_expected_1.xml")
    void updateSucceeded() {
        final var review = Review.builder()
                .uuid(REVIEW_UUID)
                .tlPointUuid(TIMELINE_POINT_UUID)
                .type(OBJECTIVE)
                .number(NUMBER_1)
                .properties(REVIEW_PROPERTIES_UPDATE)
                .status(DRAFT)
                .build();

        final var result = instance.update(review, List.of(DRAFT, DECLINED, APPROVED));

        assertThat(result).isOne();
    }

    @Test
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "pm_colleague_cycle_init.xml",
            "pm_timeline_point_init.xml",
            "pm_review_init.xml"})
    void updateNotExist() {
        final var review = Review.builder()
                .tlPointUuid(TIMELINE_POINT_UUID_NOT_EXIST)
                .type(OBJECTIVE)
                .number(NUMBER_1)
                .properties(REVIEW_PROPERTIES_UPDATE)
                .status(WAITING_FOR_APPROVAL)
                .build();

        final var result = instance.update(review, List.of(DRAFT, DECLINED, APPROVED));

        assertThat(result).isZero();
    }

    @Test
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "pm_colleague_cycle_init.xml",
            "pm_timeline_point_init.xml",
            "pm_review_init.xml"})
    @ExpectedDataSet("pm_review_delete_expected_1.xml")
    void deleteSucceeded() {
        final var result = instance.delete(
                REVIEW_UUID,
                List.of(DRAFT, DECLINED, APPROVED));
        assertThat(result).isOne();
    }

    @Test
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "pm_colleague_cycle_init.xml",
            "pm_timeline_point_init.xml",
            "pm_review_init.xml"})
    @ExpectedDataSet("pm_review_init.xml")
    void deleteNotExist() {
        final var result = instance.delete(
                REVIEW_UUID_NOT_EXIST,
                List.of(DRAFT, DECLINED, APPROVED));
        assertThat(result).isZero();
    }


    @Test
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "pm_colleague_cycle_init.xml",
            "pm_timeline_point_init.xml",
            "pm_review_init.xml"})
    void getByParams() {
        var reviews = instance.getByParams(
                MYR_TIMELINE_POINT_UUID,
                MYR,
                DRAFT,
                null
        );

        assertThat(reviews)
                .singleElement()
                .asInstanceOf(type(Review.class))
                .returns(MYR_TIMELINE_POINT_UUID, from(Review::getTlPointUuid))
                .returns(MYR, from(Review::getType))
                .returns(NUMBER_1, from(Review::getNumber))
                .returns(REVIEW_PROPERTIES_INIT, from(Review::getProperties))
                .returns(DRAFT, from(Review::getStatus));
    }

    @Test
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "pm_colleague_cycle_init.xml",
            "pm_timeline_point_init.xml",
            "pm_review_init.xml",
            "cleanup.xml"})
    void getReviewsByColleague() {
        final var reviews = List.of(
                Review.builder()
                        .uuid(REVIEW_UUID)
                        .tlPointUuid(TIMELINE_POINT_UUID)
                        .type(OBJECTIVE)
                        .status(DRAFT)
                        .number(NUMBER_1)
                        .properties(REVIEW_PROPERTIES_INIT)
                        .build(),
                Review.builder()
                        .uuid(DECLINED_REVIEW_UUID)
                        .tlPointUuid(TIMELINE_POINT_UUID)
                        .type(OBJECTIVE)
                        .status(DECLINED)
                        .number(NUMBER_2)
                        .properties(REVIEW_PROPERTIES_INIT)
                        .build(),
                Review.builder()
                        .uuid(MYR_REVIEW_UUID)
                        .tlPointUuid(MYR_TIMELINE_POINT_UUID)
                        .type(MYR)
                        .status(DRAFT)
                        .number(NUMBER_1)
                        .properties(REVIEW_PROPERTIES_INIT)
                        .build()
        );

        final var result = instance.getReviewsByColleague(CYCLE_UUID, COLLEAGUE_UUID);

        assertThat(result).isEqualTo(reviews);
    }

    @Test
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "pm_colleague_cycle_init.xml",
            "pm_timeline_point_init.xml",
            "pm_review_init.xml"})
    void getTeamReviews() {
        final var simplifiedReviews = List.of(
                SimplifiedReview.builder()
                        .uuid(REVIEW_UUID)
                        .type(OBJECTIVE)
                        .status(DRAFT)
                        .number(NUMBER_1)
                        .build(),
                SimplifiedReview.builder()
                        .uuid(DECLINED_REVIEW_UUID)
                        .type(OBJECTIVE)
                        .status(DECLINED)
                        .number(NUMBER_2)
                        .build(),
                SimplifiedReview.builder()
                        .uuid(MYR_REVIEW_UUID)
                        .type(MYR)
                        .status(DRAFT)
                        .number(NUMBER_1)
                        .build()
        );

        final var result = instance.getTeamReviews(MANAGER_UUID);

        assertThat(result)
                .singleElement()
                .asInstanceOf(type(ColleagueTimeline.class))
                .returns(null, from(ColleagueTimeline::getTimeline))
                .returns(simplifiedReviews, from(ColleagueTimeline::getReviews));
    }

    @Test
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "pm_colleague_cycle_init.xml",
            "pm_timeline_point_init.xml",
            "pm_review_init.xml"})
    @ExpectedDataSet("pm_review_update_status_1.xml")
    void updateStatusByParamsSucceeded() {

        final var result = instance.updateStatusByParams(
                TIMELINE_POINT_UUID,
                OBJECTIVE,
                NUMBER_1,
                WAITING_FOR_APPROVAL,
                Collections.singleton(DRAFT));

        assertThat(result).isOne();
    }

    @Test
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "pm_colleague_cycle_init.xml",
            "pm_timeline_point_init.xml",
            "pm_review_init.xml"})
    @ExpectedDataSet("pm_review_delete_expected_1.xml")
    void deleteByParamsSucceeded() {
        final var result = instance.deleteByParams(
                TIMELINE_POINT_UUID,
                OBJECTIVE,
                null,
                NUMBER_1,
                List.of(DRAFT, DECLINED, APPROVED));
        assertThat(result).isOne();
    }

    @Test
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "pm_colleague_cycle_init.xml",
            "pm_timeline_point_init.xml",
            "pm_review_init.xml"})
    @ExpectedDataSet("pm_review_init.xml")
    void deleteByParamsNotExist() {
        final var result = instance.deleteByParams(
                TIMELINE_POINT_UUID_NOT_EXIST,
                OBJECTIVE,
                null,
                NUMBER_1,
                List.of(DRAFT, DECLINED, APPROVED));
        assertThat(result).isZero();
    }

    @Test
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "pm_colleague_cycle_init.xml",
            "pm_timeline_point_init.xml",
            "pm_review_init.xml"})
    @ExpectedDataSet("pm_review_delete_renumerate_expected_1.xml")
    void deleteAndRenumerateReviewsSucceeded() {
        instance.deleteByParams(
                TIMELINE_POINT_UUID,
                OBJECTIVE,
                null,
                NUMBER_1,
                List.of(DRAFT, DECLINED, APPROVED));
        final var result = instance.renumerateReviews(
                TIMELINE_POINT_UUID,
                OBJECTIVE,
                NUMBER_2);
        assertThat(result).isOne();
    }

    @Test
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "pm_colleague_cycle_init.xml",
            "pm_timeline_point_init.xml",
            "pm_review_init.xml"})
    void getReviewStats() {
        final var result = instance.getReviewStats(
                TIMELINE_POINT_UUID,
                OBJECTIVE);

        assertThat(result)
                .asInstanceOf(type(ReviewStats.class))
                .returns(TIMELINE_POINT_UUID, from(ReviewStats::getTlPointUuid))
                .returns(OBJECTIVE, from(ReviewStats::getType))
                .returns(Map.of(DRAFT, 1, DECLINED, 1), from(ReviewStats::getMapStatusStats));
    }

    @Test
    @DataSet({"pm_cycle_init.xml", "cleanup.xml"})
    void getPMCycleReviewProperties() {
        final var result = instance.getPMCycleReviewTypeProperties(CYCLE_UUID, OBJECTIVE);

        assertThat(result)
                .asInstanceOf(type(PMCycleReviewTypeProperties.class))
                .returns(CYCLE_UUID, from(PMCycleReviewTypeProperties::getCycleUuid))
                .returns(OBJECTIVE, from(PMCycleReviewTypeProperties::getType))
                .returns(3, from(PMCycleReviewTypeProperties::getMin))
                .returns(5, from(PMCycleReviewTypeProperties::getMax));
    }

    @Test
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "pm_colleague_cycle_init.xml",
            "pm_timeline_point_init.xml",
            "pm_review_init.xml"})
    void getTimeline() {
        final var result = instance.getTimeline(CYCLE_UUID);

        final var objectives = PMCycleTimelinePoint.builder()
                .cycleUuid(CYCLE_UUID)
                .code(OBJECTIVES_CODE_NAME)
                .description(OBJECTIVES_CODE_NAME)
                .type(REVIEW)
                .reviewType(OBJECTIVE)
                .startDate(LocalDate.of(2021, 4, 1))
                .build();

        final var q3 = PMCycleTimelinePoint.builder()
                .cycleUuid(CYCLE_UUID)
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
