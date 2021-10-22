package com.tesco.pma.review.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.tesco.pma.api.MapJson;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.review.domain.GroupObjective;
import com.tesco.pma.review.domain.Review;
import com.tesco.pma.review.domain.ReviewStatus;
import com.tesco.pma.review.domain.WorkingGroupObjective;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.review.domain.ReviewStatus.DRAFT;
import static com.tesco.pma.review.domain.ReviewStatus.WAITING_FOR_APPROVAL;
import static com.tesco.pma.review.domain.ReviewType.OBJECTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

class ReviewDAOTest extends AbstractDAOTest {

    private static final UUID GROUP_OBJECTIVE_UUID = UUID.fromString("aab9ab0b-f50f-4442-8900-b03777ee0012");
    private static final UUID GROUP_OBJECTIVE_UUID_2 = UUID.fromString("aab9ab0b-f50f-4442-8900-b03777ee0011");
    private static final UUID GROUP_OBJECTIVE_UUID_NOT_EXIST = UUID.fromString("aab9ab0b-f50f-4442-8900-000000000000");
    private static final UUID REVIEW_UUID = UUID.fromString("ddb9ab0b-f50f-4442-8900-b03777ee0011");
    private static final UUID REVIEW_UUID_NOT_EXIST = UUID.fromString("ddb9ab0b-f50f-4442-8900-000000000000");
    private static final UUID BUSINESS_UNIT_UUID = UUID.fromString("ffb9ab0b-f50f-4442-8900-b03777ee00ef");
    private static final UUID BUSINESS_UNIT_UUID_2 = UUID.fromString("ffb9ab0b-f50f-4442-8900-b03777ee00ec");
    private static final UUID BUSINESS_UNIT_UUID_NOT_EXIST = UUID.fromString("ffb9ab0b-f50f-4442-8900-000000000000");
    private static final UUID COLLEAGUE_UUID = UUID.fromString("ccb9ab0b-f50f-4442-8900-b03777ee00ec");
    private static final UUID COLLEAGUE_UUID_NOT_EXIST = UUID.fromString("ccb9ab0b-f50f-4442-8900-000000000000");
    private static final UUID PERFORMANCE_CYCLE_UUID = UUID.fromString("0c5d9cb1-22cf-4fcd-a19a-9e70df6bc941");
    private static final Integer NUMBER_1 = 1;
    private static final String TITLE_PROPERTY_NAME = "title";
    private static final String TITLE_1 = "Title #1";
    private static final String TITLE_UPDATE = "Title update";
    private static final String GROUP_TITLE_UPDATE = "Title #1 updated";
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
    @ExpectedDataSet("group_objective_create_expected_1.xml")
    void createGroupObjectiveSucceeded() {

        final var groupObjective = GroupObjective.builder()
                .uuid(GROUP_OBJECTIVE_UUID)
                .businessUnitUuid(BUSINESS_UNIT_UUID)
                .number(NUMBER_1)
                .title(TITLE_1)
                .version(VERSION_1)
                .build();

        final int rowsInserted = instance.createGroupObjective(groupObjective);

        assertThat(rowsInserted).isOne();
    }

    @Test
    @DataSet("group_objective_init.xml")
    void createGroupObjectiveAlreadyExist() {

        final var groupObjective = GroupObjective.builder()
                .uuid(GROUP_OBJECTIVE_UUID_2)
                .businessUnitUuid(BUSINESS_UNIT_UUID_2)
                .number(NUMBER_1)
                .title(TITLE_1)
                .version(VERSION_1)
                .build();

        Assertions.assertThatThrownBy(() -> instance.createGroupObjective(groupObjective))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    @DataSet("group_objective_init.xml")
    void getGroupObjective() {
        final var result = instance.getGroupObjective(GROUP_OBJECTIVE_UUID_2);

        assertThat(result)
                .asInstanceOf(type(GroupObjective.class))
                .returns(BUSINESS_UNIT_UUID_2, from(GroupObjective::getBusinessUnitUuid))
                .returns(NUMBER_1, from(GroupObjective::getNumber))
                .returns(VERSION_1, from(GroupObjective::getVersion));
    }

    @Test
    @DataSet("group_objective_init.xml")
    void getGroupObjectiveNotExist() {
        final var result = instance.getGroupObjective(GROUP_OBJECTIVE_UUID_NOT_EXIST);

        assertThat(result).isNull();
    }

    @Test
    @DataSet("group_objective_init.xml")
    void getGroupObjectivesByBusinessUnitUuid() {
        final var result = instance.getGroupObjectivesByBusinessUnitUuid(BUSINESS_UNIT_UUID_2);

        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(1);

        assertThat(result.get(0))
                .returns(BUSINESS_UNIT_UUID_2, from(GroupObjective::getBusinessUnitUuid))
                .returns(NUMBER_1, from(GroupObjective::getNumber))
                .returns(GROUP_TITLE_UPDATE, from(GroupObjective::getTitle))
                .returns(VERSION_3, from(GroupObjective::getVersion));
    }

    @Test
    @DataSet("group_objective_init.xml")
    void deleteGroupObjectiveNotExist() {
        final var result = instance.deleteGroupObjective(GROUP_OBJECTIVE_UUID_NOT_EXIST);
        assertThat(result).isZero();
    }

    @Test
    @DataSet("group_objective_init.xml")
    void deleteGroupObjectiveSucceeded() {
        final var result = instance.deleteGroupObjective(GROUP_OBJECTIVE_UUID_2);
        assertThat(result).isOne();
    }

    @Test
    @DataSet("group_objective_init.xml")
    void getMaxVersionGroupObjective() {
        final var result = instance.getMaxVersionGroupObjective(BUSINESS_UNIT_UUID_2);
        assertThat(result).isEqualTo(3);
    }

    @Test
    @DataSet({"group_objective_init.xml", "review_init.xml"})
    void getReview() {
        final var result = instance.getReviewByUuid(REVIEW_UUID);

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
    @DataSet("review_init.xml")
    void getReviewNotExist() {
        final var result = instance.getReviewByUuid(REVIEW_UUID_NOT_EXIST);

        assertThat(result).isNull();
    }

    @Test
    @DataSet({"group_objective_init.xml", "cleanup.xml"})
    @ExpectedDataSet("review_create_expected_1.xml")
    void createReviewSucceeded() {
        final var review = Review.builder()
                .uuid(REVIEW_UUID)
                .colleagueUuid(COLLEAGUE_UUID)
                .performanceCycleUuid(PERFORMANCE_CYCLE_UUID)
                .type(OBJECTIVE)
                .number(NUMBER_1)
                .properties(REVIEW_PROPERTIES_INIT)
                .status(ReviewStatus.DRAFT)
                .build();

        final int rowsInserted = instance.createReview(review);

        assertThat(rowsInserted).isOne();
    }

    @Test
    @DataSet({"group_objective_init.xml", "review_init.xml"})
    void createReviewAlreadyExist() {

        final var review = Review.builder()
                .uuid(REVIEW_UUID)
                .colleagueUuid(COLLEAGUE_UUID)
                .performanceCycleUuid(PERFORMANCE_CYCLE_UUID)
                .type(OBJECTIVE)
                .number(NUMBER_1)
                .properties(REVIEW_PROPERTIES_INIT)
                .status(ReviewStatus.DRAFT)
                .build();

        Assertions.assertThatThrownBy(() -> instance.createReview(review))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    @DataSet({"group_objective_init.xml", "review_init.xml"})
    void deleteReviewNotExist() {
        final var result = instance.deleteReview(REVIEW_UUID_NOT_EXIST);
        assertThat(result).isZero();
    }

    @Test
    @DataSet("review_init.xml")
    void deleteReviewSucceeded() {
        final var result = instance.deleteReview(REVIEW_UUID);
        assertThat(result).isOne();
    }

    @Test
    @DataSet({"group_objective_init.xml", "review_init.xml"})
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

        final var result = instance.updateReview(review);

        assertThat(result).isOne();
    }

    @Test
    @DataSet({"group_objective_init.xml", "review_init.xml"})
    void updateReviewNotExist() {
        final var review = Review.builder()
                .colleagueUuid(COLLEAGUE_UUID_NOT_EXIST)
                .performanceCycleUuid(PERFORMANCE_CYCLE_UUID)
                .type(OBJECTIVE)
                .number(NUMBER_1)
                .properties(REVIEW_PROPERTIES_UPDATE)
                .status(WAITING_FOR_APPROVAL)
                .build();

        final var result = instance.updateReview(review);

        assertThat(result).isZero();
    }

    @Test
    @DataSet({"group_objective_init.xml", "review_init.xml"})
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
    @DataSet({"group_objective_init.xml", "review_init.xml"})
    @ExpectedDataSet("review_unlink_group_objective_expected.xml")
    void updateReviewUnlinkGroupObjective() {
        final var review = Review.builder()
                .uuid(REVIEW_UUID)
                .colleagueUuid(COLLEAGUE_UUID)
                .performanceCycleUuid(PERFORMANCE_CYCLE_UUID)
                .type(OBJECTIVE)
                .number(NUMBER_1)
                .properties(REVIEW_PROPERTIES_INIT)
                .status(ReviewStatus.DRAFT)
                .build();

        final var result = instance.updateReview(review);

        assertThat(result).isOne();
    }

    @Test
    @DataSet({"group_objective_init.xml", "review_without_group_objective_init.xml"})
    @ExpectedDataSet("review_unlink_group_objective_expected.xml")
    void updateReviewLinkGroupObjective() {
        final var review = Review.builder()
                .uuid(REVIEW_UUID)
                .colleagueUuid(COLLEAGUE_UUID)
                .performanceCycleUuid(PERFORMANCE_CYCLE_UUID)
                .type(OBJECTIVE)
                .number(NUMBER_1)
                .properties(REVIEW_PROPERTIES_INIT)
                .status(ReviewStatus.DRAFT)
                .build();

        final var result = instance.updateReview(review);

        assertThat(result).isOne();
    }

    @Test
    @DataSet("cleanup.xml")
    @ExpectedDataSet("working_group_objective_create_expected.xml")
    void createWorkingGroupObjectiveSucceeded() {

        final var workingGroupObjective = WorkingGroupObjective.builder()
                .businessUnitUuid(BUSINESS_UNIT_UUID_2)
                .version(VERSION_1)
                .updaterId(USER_INIT)
                .updateTime(Timestamp.valueOf(TIME_INIT).toInstant())
                .build();

        final var rowsInserted = instance.insertOrUpdateWorkingGroupObjective(workingGroupObjective);

        assertThat(rowsInserted).isOne();
    }

    @Test
    @DataSet("working_group_objective_init.xml")
    void deleteWorkingGroupObjectiveNotExist() {
        final var result = instance.deleteWorkingGroupObjective(BUSINESS_UNIT_UUID_NOT_EXIST);

        assertThat(result).isZero();
    }

    @Test
    @DataSet("working_group_objective_init.xml")
    void deleteWorkingGroupObjectiveSucceeded() {
        final var result = instance.deleteWorkingGroupObjective(BUSINESS_UNIT_UUID_2);

        assertThat(result).isOne();
    }

    @Test
    @DataSet("working_group_objective_init.xml")
    void getWorkingGroupObjective() {
        final var result = instance.getWorkingGroupObjective(BUSINESS_UNIT_UUID_2);

        assertThat(result)
                .asInstanceOf(type(WorkingGroupObjective.class))
                .returns(BUSINESS_UNIT_UUID_2, from(WorkingGroupObjective::getBusinessUnitUuid))
                .returns(VERSION_1, from(WorkingGroupObjective::getVersion))
                .returns(USER_INIT, from(WorkingGroupObjective::getUpdaterId))
                .returns(Timestamp.valueOf(TIME_INIT).toInstant(), from(WorkingGroupObjective::getUpdateTime));
    }

    @Test
    @DataSet("working_group_objective_init.xml")
    void getWorkingGroupObjectiveNotExist() {
        final var result = instance.getWorkingGroupObjective(BUSINESS_UNIT_UUID_NOT_EXIST);

        assertThat(result).isNull();
    }
}
