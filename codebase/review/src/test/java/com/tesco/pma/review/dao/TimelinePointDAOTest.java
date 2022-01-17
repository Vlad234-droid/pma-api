package com.tesco.pma.review.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.tesco.pma.api.MapJson;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.review.domain.TimelinePoint;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.cycle.api.PMReviewType.MYR;
import static com.tesco.pma.cycle.api.PMReviewType.OBJECTIVE;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.DRAFT;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.WAITING_FOR_APPROVAL;
import static com.tesco.pma.cycle.api.model.PMElementType.REVIEW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TimelinePointDAOTest extends AbstractDAOTest {
    private static final UUID CYCLE_UUID = UUID.fromString("10000000-1000-0000-0000-000000000000");
    private static final UUID COLLEAGUE_UUID = UUID.fromString("10000000-0000-0000-0000-000000000000");
    private static final UUID TIMELINE_POINT_UUID = UUID.fromString("10000000-0000-0000-1000-000000000000");
    private static final UUID MYR_TIMELINE_POINT_UUID = UUID.fromString("10000000-0000-0000-2000-000000000000");
    private static final UUID TIMELINE_POINT_UUID_NOT_EXIST = UUID.fromString("ccb9ab0b-f50f-4442-8900-000000000000");
    private static final UUID COLLEAGUE_CYCLE_UUID = UUID.fromString("10000000-0000-1000-0000-000000000000");
    private static final UUID COLLEAGUE_CYCLE_UUID_NOT_EXIST = UUID.fromString("10000000-0000-2222-0000-000000000000");
    private static final String OBJECTIVE_CODE = "Objective";
    private static final String MYR_CODE = "Myr";
    public static final Instant START_TIME = Instant.parse("2021-09-20T10:45:12.00Z");
    public static final Instant MYR_START_TIME = Instant.parse("2022-03-20T10:45:12.00Z");
    public static final Instant START_TIME_UPDATE = Instant.parse("2021-09-20T11:45:12.00Z");
    public static final Instant END_TIME = Instant.parse("2021-09-21T10:45:12.00Z");
    public static final Instant MYR_END_TIME = Instant.parse("2022-03-21T10:45:12.00Z");
    public static final Instant END_TIME_UPDATE = Instant.parse("2021-09-21T11:45:12.00Z");
    private static final String TITLE_PROPERTY_NAME = "title";
    private static final String TITLE_UPDATE = "Title update";
    private static final String TITLE_INIT = "Title init";
    private static final String DESCRIPTION_PROPERTY_NAME = "description";
    private static final String DESCRIPTION_INIT = "Description init";
    private static final String DESCRIPTION_UPDATE = "Description update";
    private static final MapJson TIMELINE_POINT_PROPERTIES_INIT = new MapJson(
            Map.of(TITLE_PROPERTY_NAME, TITLE_INIT,
                    DESCRIPTION_PROPERTY_NAME, DESCRIPTION_INIT
            ));
    private static final MapJson TIMELINE_POINT_PROPERTIES_UPDATE = new MapJson(
            Map.of(TITLE_PROPERTY_NAME, TITLE_UPDATE,
                    DESCRIPTION_PROPERTY_NAME, DESCRIPTION_UPDATE
            ));

    @Autowired
    private TimelinePointDAO instance;

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
            "pm_colleague_cycle_init.xml"})
    @ExpectedDataSet("pm_timeline_point_create_expected_1.xml")
    void createSucceeded() {
        final var tlPoint = TimelinePoint.builder()
                .uuid(TIMELINE_POINT_UUID)
                .colleagueCycleUuid(COLLEAGUE_CYCLE_UUID)
                .code(OBJECTIVE_CODE)
                .description(DESCRIPTION_INIT)
                .type(REVIEW)
                .startTime(START_TIME)
                .endTime(END_TIME)
                .properties(TIMELINE_POINT_PROPERTIES_INIT)
                .status(DRAFT)
                .build();

        final int rowsInserted = instance.create(tlPoint);

        assertThat(rowsInserted).isOne();
    }

    @Test
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "pm_colleague_cycle_init.xml",
            "pm_timeline_point_init.xml"})
    void createAlreadyExist() {

        final var tlPoint = TimelinePoint.builder()
                .uuid(TIMELINE_POINT_UUID)
                .colleagueCycleUuid(COLLEAGUE_CYCLE_UUID)
                .code(OBJECTIVE_CODE)
                .description(DESCRIPTION_INIT)
                .type(REVIEW)
                .startTime(START_TIME)
                .endTime(END_TIME)
                .properties(TIMELINE_POINT_PROPERTIES_INIT)
                .status(DRAFT)
                .build();

        Assertions.assertThatThrownBy(() -> instance.create(tlPoint))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "pm_colleague_cycle_init.xml",
            "pm_timeline_point_init.xml"})
    void read() {
        final var result = instance.read(TIMELINE_POINT_UUID);

        assertThat(result)
                .asInstanceOf(type(TimelinePoint.class))
                .returns(TIMELINE_POINT_UUID, from(TimelinePoint::getUuid))
                .returns(COLLEAGUE_CYCLE_UUID, from(TimelinePoint::getColleagueCycleUuid))
                .returns(OBJECTIVE_CODE, from(TimelinePoint::getCode))
                .returns(DESCRIPTION_INIT, from(TimelinePoint::getDescription))
                .returns(REVIEW, from(TimelinePoint::getType))
                .returns(START_TIME, from(TimelinePoint::getStartTime))
                .returns(END_TIME, from(TimelinePoint::getEndTime))
                .returns(TIMELINE_POINT_PROPERTIES_INIT, from(TimelinePoint::getProperties))
                .returns(DRAFT, from(TimelinePoint::getStatus));
    }


    @Test
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "pm_colleague_cycle_init.xml",
            "pm_timeline_point_init.xml"})
    @ExpectedDataSet("pm_timeline_point_update_expected_1.xml")
    void updateSucceeded() {
        final var tlPoint = TimelinePoint.builder()
                .uuid(TIMELINE_POINT_UUID)
                .colleagueCycleUuid(COLLEAGUE_CYCLE_UUID)
                .code(OBJECTIVE_CODE)
                .description(DESCRIPTION_INIT)
                .type(REVIEW)
                .startTime(START_TIME_UPDATE)
                .endTime(END_TIME_UPDATE)
                .properties(TIMELINE_POINT_PROPERTIES_UPDATE)
                .status(WAITING_FOR_APPROVAL)
                .build();

        final var result = instance.update(tlPoint, List.of(DRAFT));

        assertThat(result).isOne();
    }

    @Test
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "pm_colleague_cycle_init.xml",
            "pm_timeline_point_init.xml"})
    void updateNotExist() {
        final var tlPoint = TimelinePoint.builder()
                .uuid(TIMELINE_POINT_UUID)
                .colleagueCycleUuid(COLLEAGUE_CYCLE_UUID_NOT_EXIST)
                .code(OBJECTIVE_CODE)
                .description(DESCRIPTION_INIT)
                .type(REVIEW)
                .startTime(START_TIME_UPDATE)
                .endTime(END_TIME_UPDATE)
                .properties(TIMELINE_POINT_PROPERTIES_UPDATE)
                .status(WAITING_FOR_APPROVAL)
                .build();

        final var result = instance.update(tlPoint, List.of(DRAFT));

        assertThat(result).isZero();
    }

    @Test
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "pm_colleague_cycle_init.xml",
            "pm_timeline_point_init.xml"})
    @ExpectedDataSet("pm_timeline_point_delete_expected_1.xml")
    void deleteSucceeded() {
        final var result = instance.delete(
                TIMELINE_POINT_UUID,
                List.of(DRAFT));
        assertThat(result).isOne();
    }

    @Test
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "pm_colleague_cycle_init.xml",
            "pm_timeline_point_init.xml"})
    @ExpectedDataSet("pm_timeline_point_init.xml")
    void deleteNotExist() {
        final var result = instance.delete(
                TIMELINE_POINT_UUID_NOT_EXIST,
                List.of(DRAFT));
        assertThat(result).isZero();
    }


    @Test
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "pm_colleague_cycle_init.xml",
            "pm_timeline_point_init.xml"})
    void getByParams() {
        var result = instance.getByParams(
                COLLEAGUE_CYCLE_UUID,
                OBJECTIVE_CODE,
                null
        );

        assertThat(result)
                .singleElement()
                .asInstanceOf(type(TimelinePoint.class))
                .returns(TIMELINE_POINT_UUID, from(TimelinePoint::getUuid))
                .returns(COLLEAGUE_CYCLE_UUID, from(TimelinePoint::getColleagueCycleUuid))
                .returns(OBJECTIVE_CODE, from(TimelinePoint::getCode))
                .returns(DESCRIPTION_INIT, from(TimelinePoint::getDescription))
                .returns(REVIEW, from(TimelinePoint::getType))
                .returns(START_TIME, from(TimelinePoint::getStartTime))
                .returns(END_TIME, from(TimelinePoint::getEndTime))
                .returns(TIMELINE_POINT_PROPERTIES_INIT, from(TimelinePoint::getProperties))
                .returns(DRAFT, from(TimelinePoint::getStatus));
    }

    @Test
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "pm_colleague_cycle_init.xml",
            "pm_timeline_point_init.xml"})
    @ExpectedDataSet("pm_timeline_point_update_status_expected_1.xml")
    void updateStatusByParamsSucceeded() {

        final var result = instance.updateStatusByParams(
                COLLEAGUE_CYCLE_UUID,
                OBJECTIVE_CODE,
                WAITING_FOR_APPROVAL,
                Collections.singleton(DRAFT));

        assertThat(result).isOne();
    }

    @Test
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "pm_colleague_cycle_init.xml",
            "pm_timeline_point_init.xml"})
    @ExpectedDataSet("pm_timeline_point_update_status_expected_1.xml")
    void updateStatus() {

        final var result = instance.updateStatus(
                TIMELINE_POINT_UUID,
                WAITING_FOR_APPROVAL,
                Collections.singleton(DRAFT));

        assertThat(result).isOne();
    }

    @Test
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "pm_colleague_cycle_init.xml",
            "pm_timeline_point_init.xml"})
    @ExpectedDataSet("pm_timeline_point_delete_expected_1.xml")
    void deleteByParamsSucceeded() {
        final var result = instance.deleteByParams(
                COLLEAGUE_CYCLE_UUID,
                OBJECTIVE_CODE,
                null,
                List.of(DRAFT));
        assertThat(result).isOne();
    }

    @Test
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "pm_colleague_cycle_init.xml",
            "pm_timeline_point_init.xml"})
    @ExpectedDataSet("pm_timeline_point_init.xml")
    void deleteByParamsNotExist() {
        final var result = instance.deleteByParams(
                COLLEAGUE_CYCLE_UUID_NOT_EXIST,
                OBJECTIVE_CODE,
                null,
                List.of(DRAFT));
        assertThat(result).isZero();
    }


    @Test
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "pm_colleague_cycle_init.xml",
            "pm_timeline_point_init.xml"})
    void getTimeline() {
        final var result = instance.getTimeline(CYCLE_UUID, COLLEAGUE_UUID);

        var lastUpdatedTime = LocalDateTime.of(2022, 1, 1, 10, 0, 0)
                .toInstant(ZoneOffset.UTC);

        final var objectives = TimelinePoint.builder()
                .uuid(TIMELINE_POINT_UUID)
                .colleagueCycleUuid(COLLEAGUE_CYCLE_UUID)
                .code(OBJECTIVE_CODE)
                .description(DESCRIPTION_INIT)
                .type(REVIEW)
                .startTime(START_TIME)
                .endTime(END_TIME)
                .properties(TIMELINE_POINT_PROPERTIES_INIT)
                .status(DRAFT)
                .reviewType(OBJECTIVE)
                .lastUpdatedTime(lastUpdatedTime)
                .build();

        final var myr = TimelinePoint.builder()
                .uuid(MYR_TIMELINE_POINT_UUID)
                .colleagueCycleUuid(COLLEAGUE_CYCLE_UUID)
                .code(MYR_CODE)
                .description(DESCRIPTION_INIT)
                .type(REVIEW)
                .startTime(MYR_START_TIME)
                .endTime(MYR_END_TIME)
                .properties(TIMELINE_POINT_PROPERTIES_INIT)
                .status(DRAFT)
                .reviewType(MYR)
                .lastUpdatedTime(lastUpdatedTime)
                .build();

        assertThat(result.get(0))
                .isEqualTo(objectives);

        assertThat(result.get(1))
                .isEqualTo(myr);
    }

    @Test
    @DataSet({"colleague_init.xml",
            "pm_cycle_init.xml",
            "pm_colleague_cycle_init.xml",
            "pm_timeline_point_init.xml"})
    void getTimelineByUUID() {
        assertNotNull(instance.getTimelineByUUID(MYR_TIMELINE_POINT_UUID));
    }
}
