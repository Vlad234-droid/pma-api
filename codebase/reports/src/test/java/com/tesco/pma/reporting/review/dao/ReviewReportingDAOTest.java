package com.tesco.pma.reporting.review.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.dao.AbstractDAOTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

import static com.tesco.pma.cycle.api.PMTimelinePointStatus.APPROVED;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.DECLINED;
import static org.assertj.core.api.Assertions.assertThat;

class ReviewReportingDAOTest extends AbstractDAOTest {

    private static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/reporting/review/dao/";

    private static final String COLLEAGUE_UUID = "10000000-0000-0000-0000-000000000000";
    private static final UUID TIMELINE_POINT_UUID = UUID.fromString("10000000-0000-0000-2000-000000000000");

    @Autowired
    private ReviewReportingDAO instance;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "colleague_init.xml",
              BASE_PATH_TO_DATA_SET + "pm_cycle_init.xml",
              BASE_PATH_TO_DATA_SET + "pm_colleague_cycle_init.xml",
              BASE_PATH_TO_DATA_SET + "pm_timeline_point_init.xml",
              BASE_PATH_TO_DATA_SET + "pm_review_init.xml"})
    void getLinkedObjectivesData() {
        final var result = instance.getLinkedObjectivesData(TIMELINE_POINT_UUID, APPROVED);

        assertThat(result).isNotNull();
        assertThat(result.getEmployeeUUID()).isEqualTo(COLLEAGUE_UUID);
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "colleague_init.xml",
              BASE_PATH_TO_DATA_SET + "pm_cycle_init.xml",
              BASE_PATH_TO_DATA_SET + "pm_colleague_cycle_init.xml",
              BASE_PATH_TO_DATA_SET + "pm_timeline_point_init.xml",
              BASE_PATH_TO_DATA_SET + "pm_review_init.xml"})
    void getLinkedObjectivesDataNotExist() {
        final var result = instance.getLinkedObjectivesData(TIMELINE_POINT_UUID, DECLINED);

        assertThat(result).isNull();
    }
}