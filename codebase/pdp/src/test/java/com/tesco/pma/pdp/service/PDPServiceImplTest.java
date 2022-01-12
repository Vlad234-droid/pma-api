package com.tesco.pma.pdp.service;

import com.tesco.pma.api.MapJson;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.pdp.LocalServiceTestConfig;
import com.tesco.pma.pdp.dao.PDPDao;
import com.tesco.pma.pdp.domain.PDPGoal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.pdp.api.PDPGoalStatus.PUBLISHED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(classes = {LocalServiceTestConfig.class, PDPServiceImpl.class})
@ExtendWith(MockitoExtension.class)
public class PDPServiceImplTest {

    private static final UUID GOAL_UUID_1 = UUID.fromString("6d37262f-3a00-4706-a74b-6bf98be65765");
    private static final UUID GOAL_UUID_2 = UUID.fromString("7d37262f-3a00-4706-a74b-6bf98be65765");
    private static final int GOAL_NUMBER_1 = 1;
    private static final int GOAL_NUMBER_2 = 2;
    private static final UUID COLLEAGUE_UUID = UUID.fromString("ce245be1-1f43-4d5f-85dc-db6e2cce0c2a");
    private static final LocalDate ACHIEVEMENT_DATE = LocalDate.parse("2021-12-29");
    private static final MapJson PROPERTIES = new MapJson(Map.of("pm_pdp_test_property1", "P1", "pm_pdp_test_property2", "P2"));

    @Autowired
    private NamedMessageSourceAccessor messageSourceAccessor;

    @Autowired
    private PDPServiceImpl pdpService;

    @MockBean
    private PDPDao pdpDao;

    @Test
    void createGoals() {
        var goals = buildGoals(GOAL_UUID_1, GOAL_UUID_2);
        when(pdpDao.createGoal(any())).thenReturn(1);

        var result = pdpService.createGoals(COLLEAGUE_UUID, goals);

        assertEquals(goals, result);
    }

    @Test
    void shouldThrowDatabaseConstraintViolationExceptionWhenCreateGoalsWithDuplicateKey() {
        when(pdpDao.createGoal(any())).thenThrow(DuplicateKeyException.class);

        assertThrows(DatabaseConstraintViolationException.class, () ->
                pdpService.createGoals(COLLEAGUE_UUID, buildGoals(GOAL_UUID_1, GOAL_UUID_2)));
    }

    @Test
    void updateGoals() {
        var goals = buildGoals(GOAL_UUID_1, GOAL_UUID_2);
        when(pdpDao.updateGoal(any())).thenReturn(1);

        var result = pdpService.updateGoals(COLLEAGUE_UUID, goals);

        assertEquals(goals, result);
    }

    @Test
    void shouldThrowDatabaseConstraintViolationExceptionWhenUpdateGoalsWithDuplicateKey() {
        when(pdpDao.updateGoal(any())).thenThrow(DuplicateKeyException.class);

        assertThrows(DatabaseConstraintViolationException.class, () ->
                pdpService.updateGoals(COLLEAGUE_UUID, buildGoals(GOAL_UUID_1, GOAL_UUID_2)));
    }

    @Test
    void updateGoalsThrowsNotFoundExceptionWhenDaoReturnsNotOne() {
        when(pdpDao.updateGoal(any())).thenReturn(0);

        assertThrows(NotFoundException.class, () ->
                pdpService.updateGoals(COLLEAGUE_UUID, buildGoals(GOAL_UUID_1, GOAL_UUID_2)));
    }

    @Test
    void deleteGoals() {
        var goals = List.of(GOAL_UUID_1, GOAL_UUID_2);
        when(pdpDao.deleteGoalByUuidAndColleague(eq(COLLEAGUE_UUID), any())).thenReturn(1);

        pdpService.deleteGoals(COLLEAGUE_UUID, goals);

        verify(pdpDao, times(goals.size())).deleteGoalByUuidAndColleague(eq(COLLEAGUE_UUID), any());
    }

    @Test
    void deleteGoalsThrowsNotFoundExceptionWhenDaoReturnsNotOne() {
        when(pdpDao.deleteGoalByUuidAndColleague(eq(COLLEAGUE_UUID), any())).thenReturn(0);

        assertThrows(NotFoundException.class, () ->
                pdpService.deleteGoals(COLLEAGUE_UUID, List.of(GOAL_UUID_1, GOAL_UUID_2)));
    }

    @Test
    void getGoalByColleagueAndNumber() {
        var goal = buildGoal(GOAL_UUID_1, GOAL_NUMBER_1);
        when(pdpDao.readGoalByColleagueAndNumber(COLLEAGUE_UUID, GOAL_NUMBER_1)).thenReturn(goal);

        var result = pdpService.getGoal(COLLEAGUE_UUID, GOAL_NUMBER_1);

        assertEquals(goal, result);
    }

    @Test
    void getGoalByColleagueAndNumberThrowsNotFoundExceptionWhenDaoReturnsNull() {
        when(pdpDao.readGoalByColleagueAndNumber(COLLEAGUE_UUID, GOAL_NUMBER_1)).thenReturn(null);

        assertThrows(NotFoundException.class, () ->
                pdpService.getGoal(COLLEAGUE_UUID, GOAL_NUMBER_1));
    }

    @Test
    void getGoalByUuid() {
        var goal = buildGoal(GOAL_UUID_1, GOAL_NUMBER_1);
        when(pdpDao.readGoalByUuid(COLLEAGUE_UUID, GOAL_UUID_1)).thenReturn(goal);

        var result = pdpService.getGoal(COLLEAGUE_UUID, GOAL_UUID_1);

        assertEquals(goal, result);
    }

    @Test
    void getGoalByUuidThrowsNotFoundExceptionWhenDaoReturnsNull() {
        when(pdpDao.readGoalByUuid(COLLEAGUE_UUID, GOAL_UUID_1)).thenReturn(null);

        assertThrows(NotFoundException.class, () ->
                pdpService.getGoal(COLLEAGUE_UUID, GOAL_UUID_1));
    }

    @Test
    void getGoals() {
        var goals = buildGoals(GOAL_UUID_1, GOAL_UUID_2);
        when(pdpDao.readGoalsByColleague(COLLEAGUE_UUID)).thenReturn(goals);

        var result = pdpService.getGoals(COLLEAGUE_UUID);

        assertEquals(goals, result);
    }

    private PDPGoal buildGoal(UUID uuid, int number) {
        return new PDPGoal(uuid, COLLEAGUE_UUID, number, PROPERTIES, ACHIEVEMENT_DATE, PUBLISHED);
    }

    private List<PDPGoal> buildGoals(UUID uuid1, UUID uuid2) {
        return List.of(buildGoal(uuid1, GOAL_NUMBER_1), buildGoal(uuid2, GOAL_NUMBER_2));
    }
}