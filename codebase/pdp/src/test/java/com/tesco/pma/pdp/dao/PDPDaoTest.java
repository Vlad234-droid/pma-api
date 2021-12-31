package com.tesco.pma.pdp.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.ExpectedDataSet;
import com.tesco.pma.api.MapJson;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.pdp.domain.PDPGoal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.pdp.api.PDPGoalStatus.DRAFT;
import static com.tesco.pma.pdp.api.PDPGoalStatus.PUBLISHED;
import static org.assertj.core.api.Assertions.assertThat;

public class PDPDaoTest extends AbstractDAOTest {

    private static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/pdp/dao/";

    private static final UUID GOAL_UUID_1 = UUID.fromString("6d37262f-3a00-4706-a74b-6bf98be65765");
    private static final UUID GOAL_UUID_2 = UUID.fromString("7d37262f-3a00-4706-a74b-6bf98be65765");
    private static final int GOAL_NUMBER_1 = 1;
    private static final int GOAL_NUMBER_2 = 2;
    private static final UUID COLLEAGUE_UUID = UUID.fromString("ce245be1-1f43-4d5f-85dc-db6e2cce0c2a");
    private static final LocalDate ACHIEVEMENT_DATE = LocalDate.parse("2021-12-29");
    private static final MapJson PROPERTIES = new MapJson(Map.of("pm_pdp_test_property1", "P1", "pm_pdp_test_property2", "P2"));


    @Autowired
    private PDPDao instance;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }

    @Test
    @DataSet(BASE_PATH_TO_DATA_SET + "goals_init.xml")
    void readGoalByColleagueAndNumber() {
        final var result = instance.readGoalByColleagueAndNumber(COLLEAGUE_UUID, GOAL_NUMBER_2);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(buildGoal(GOAL_UUID_2, GOAL_NUMBER_2));
    }

    @Test
    @DataSet(BASE_PATH_TO_DATA_SET + "goals_init.xml")
    void readByUuid() {
        final var result = instance.readGoalByUuid(COLLEAGUE_UUID, GOAL_UUID_1);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(buildGoal(GOAL_UUID_1, GOAL_NUMBER_1));
    }

    @Test
    @DataSet(BASE_PATH_TO_DATA_SET + "goals_init.xml")
    void readGoalsByColleague() {
        final var result = instance.readGoalsByColleague(COLLEAGUE_UUID);

        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(buildGoals(GOAL_UUID_1, GOAL_UUID_2));
    }

    @Test
    @DataSet(BASE_PATH_TO_DATA_SET + "cleanup.xml")
    @ExpectedDataSet(BASE_PATH_TO_DATA_SET + "goal_create_expected.xml")
    void createGoal() {
        final var goal = buildGoal(GOAL_UUID_1, GOAL_NUMBER_1);

        final var rowsInserted = instance.createGoal(goal);

        assertThat(rowsInserted).isOne();
    }

    @Test
    @DataSet(BASE_PATH_TO_DATA_SET + "goals_init.xml")
    @ExpectedDataSet(BASE_PATH_TO_DATA_SET + "goal_update_expected.xml")
    void updateGoal() {
        final var goal = buildGoal(GOAL_UUID_1, GOAL_NUMBER_1);
        goal.setAchievementDate(LocalDate.parse("2021-12-30"));
        goal.setStatus(DRAFT);

        final var rowsInserted = instance.updateGoal(goal);

        assertThat(rowsInserted).isOne();
    }

    private PDPGoal buildGoal(UUID uuid, int number) {
        return new PDPGoal(uuid, COLLEAGUE_UUID, number, PROPERTIES, ACHIEVEMENT_DATE, PUBLISHED);
    }

    private List<PDPGoal> buildGoals(UUID uuid1, UUID uuid2) {
        return List.of(buildGoal(uuid1, GOAL_NUMBER_1), buildGoal(uuid2, GOAL_NUMBER_2));
    }
}