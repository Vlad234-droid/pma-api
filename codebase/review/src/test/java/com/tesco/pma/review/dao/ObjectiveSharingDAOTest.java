package com.tesco.pma.review.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.api.MapJson;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.review.domain.Review;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.cycle.api.PMReviewStatus.APPROVED;
import static com.tesco.pma.cycle.api.PMReviewType.OBJECTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

class ObjectiveSharingDAOTest extends AbstractDAOTest {
    private static final UUID PERFORMANCE_CYCLE_UUID = UUID.fromString("0c5d9cb1-22cf-4fcd-a19a-9e70df6bc941");
    private static final Integer NUMBER_1 = 1;
    private static final String TITLE_PROPERTY_NAME = "title";
    private static final String TITLE_INIT = "Title init";
    private static final String DESCRIPTION_PROPERTY_NAME = "description";
    private static final String DESCRIPTION_INIT = "Description init";
    private static final String MEETS_PROPERTY_NAME = "meets";
    private static final String MEETS_INIT = "Meets init";
    private static final String EXCEEDS_PROPERTY_NAME = "exceeds";
    private static final String EXCEEDS_INIT = "Exceeds init";
    private static final MapJson REVIEW_PROPERTIES_INIT = new MapJson(
            Map.of(TITLE_PROPERTY_NAME, TITLE_INIT,
                    DESCRIPTION_PROPERTY_NAME, DESCRIPTION_INIT,
                    MEETS_PROPERTY_NAME, MEETS_INIT,
                    EXCEEDS_PROPERTY_NAME, EXCEEDS_INIT
            ));

    private static final UUID COLLEAGUE_UUID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID COLLEAGUE_UUID_2 = UUID.fromString("10000000-0000-0000-0000-000000000002");

    @Autowired
    private ObjectiveSharingDAO instance;

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
    @DataSet({"pm_cycle_init.xml", "objective_sharing_init.xml"})
    void shareObjectives() {
        var managerShareObjectives = instance.isColleagueShareObjectives(COLLEAGUE_UUID, PERFORMANCE_CYCLE_UUID);
        Assertions.assertThat(managerShareObjectives).isFalse();

        var count = instance.shareObjectives(COLLEAGUE_UUID, PERFORMANCE_CYCLE_UUID);
        assertThat(count).isEqualTo(1);

        managerShareObjectives = instance.isColleagueShareObjectives(COLLEAGUE_UUID, PERFORMANCE_CYCLE_UUID);
        Assertions.assertThat(managerShareObjectives).isTrue();
    }

    @Test
    @DataSet({"pm_cycle_init.xml", "objective_sharing_init.xml"})
    void stopSharingObjectives() {
        var managerShareObjectives = instance.isColleagueShareObjectives(COLLEAGUE_UUID_2, PERFORMANCE_CYCLE_UUID);
        Assertions.assertThat(managerShareObjectives).isTrue();

        var count = instance.stopSharingObjectives(COLLEAGUE_UUID_2, PERFORMANCE_CYCLE_UUID);
        assertThat(count).isEqualTo(1);

        managerShareObjectives = instance.isColleagueShareObjectives(COLLEAGUE_UUID_2, PERFORMANCE_CYCLE_UUID);
        Assertions.assertThat(managerShareObjectives).isFalse();
    }

    @Test
    @DataSet({"pm_cycle_init.xml", "objective_sharing_init.xml"})
    void getColleagueSharedObjectives() {
        var reviews = instance.getColleagueSharedObjectives(COLLEAGUE_UUID_2, PERFORMANCE_CYCLE_UUID);

        assertThat(reviews)
                .singleElement()
                .asInstanceOf(type(Review.class))
                .returns(COLLEAGUE_UUID_2, from(Review::getColleagueUuid))
                .returns(PERFORMANCE_CYCLE_UUID, from(Review::getPerformanceCycleUuid))
                .returns(OBJECTIVE, from(Review::getType))
                .returns(NUMBER_1, from(Review::getNumber))
                .returns(REVIEW_PROPERTIES_INIT, from(Review::getProperties))
                .returns(APPROVED, from(Review::getStatus));
    }
}
