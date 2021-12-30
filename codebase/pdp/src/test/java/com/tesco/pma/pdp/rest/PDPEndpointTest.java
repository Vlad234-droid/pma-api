package com.tesco.pma.pdp.rest;

import com.tesco.pma.api.MapJson;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.pdp.domain.PDPGoal;
import com.tesco.pma.pdp.service.PDPService;
import com.tesco.pma.rest.AbstractEndpointTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.pdp.api.PDPGoalStatus.PUBLISHED;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PDPEndpoint.class)
@WithMockUser(username = PDPEndpointTest.COLLEAGUE_UUID_STR)
public class PDPEndpointTest extends AbstractEndpointTest {

    static final String COLLEAGUE_UUID_STR = "ce245be1-1f43-4d5f-85dc-db6e2cce0c2a";

    private static final UUID GOAL_UUID_1 = UUID.fromString("6d37262f-3a00-4706-a74b-6bf98be65765");
    private static final UUID GOAL_UUID_2 = UUID.fromString("7d37262f-3a00-4706-a74b-6bf98be65765");
    private static final UUID COLLEAGUE_UUID = UUID.fromString("ce245be1-1f43-4d5f-85dc-db6e2cce0c2a");

    private static final String PDP_GOALS_URL = "/pdp/goals";
    private static final int GOAL_NUMBER_1 = 1;
    private static final int GOAL_NUMBER_2 = 2;

    private static final String PDP_GOALS_GET_REQUEST_JSON_FILE_NAME = "pm_pdp_goals_get_request.json";
    private static final String PDP_GOALS_UPDATE_REQUEST_JSON_FILE_NAME = "pm_pdp_goals_update_request.json";
    private static final String PDP_GOAL_GET_OK_RESPONSE_JSON_FILE_NAME = "pm_pdp_goal_get_ok_response.json";
    private static final String PDP_GOALS_GET_OK_RESPONSE_JSON_FILE_NAME = "pm_pdp_goals_get_ok_response.json";
    private static final LocalDate ACHIEVEMENT_DATE = LocalDate.parse("2021-12-29");
    private static final MapJson PROPERTIES = new MapJson(Map.of("pm_pdp_test_property1", "P1", "pm_pdp_test_property2", "P2"));

    @MockBean
    private PDPService pdpService;

    @Test
    void updateGoals() throws Exception {
        var goals = List.of(buildGoal(GOAL_UUID_1, GOAL_NUMBER_1), buildGoal(GOAL_UUID_2, GOAL_NUMBER_2));
        when(pdpService.updateGoals(COLLEAGUE_UUID, goals)).thenReturn(goals);

        var result = performPut(PDP_GOALS_UPDATE_REQUEST_JSON_FILE_NAME, status().isOk(), PDP_GOALS_URL);

        assertResponseContent(result.getResponse(), PDP_GOALS_GET_OK_RESPONSE_JSON_FILE_NAME);
    }

    @Test
    void updateGoalsUnsuccess() throws Exception {
        var goals = List.of(buildGoal(GOAL_UUID_1, GOAL_NUMBER_1), buildGoal(GOAL_UUID_2, GOAL_NUMBER_2));
        doThrow(NotFoundException.class).when(pdpService).updateGoals(COLLEAGUE_UUID, goals);

        performPut(PDP_GOALS_UPDATE_REQUEST_JSON_FILE_NAME, status().isNotFound(), PDP_GOALS_URL);
    }

    @Test
    void deleteGoals() throws Exception {
        doNothing().when(pdpService).deleteGoals(COLLEAGUE_UUID, List.of(GOAL_UUID_1, GOAL_UUID_2));

        performPost(PDP_GOALS_GET_REQUEST_JSON_FILE_NAME, status().isOk(), PDP_GOALS_URL + "/delete");
    }

    @Test
    void deleteGoalsUnsuccess() throws Exception {
        doThrow(NotFoundException.class).when(pdpService).deleteGoals(COLLEAGUE_UUID, List.of(GOAL_UUID_1, GOAL_UUID_2));

        performPost(PDP_GOALS_GET_REQUEST_JSON_FILE_NAME, status().isNotFound(), PDP_GOALS_URL + "/delete");
    }

    @Test
    void getGoalByColleagueAndNumber() throws Exception {
        when(pdpService.getGoal(COLLEAGUE_UUID, GOAL_NUMBER_1)).thenReturn(buildGoal(GOAL_UUID_1, GOAL_NUMBER_1));

        var result = performGet(status().isOk(), PDP_GOALS_URL + "/numbers/{number}", GOAL_NUMBER_1);

        assertResponseContent(result.getResponse(), PDP_GOAL_GET_OK_RESPONSE_JSON_FILE_NAME);
    }

    @Test
    void getGoalByColleagueAndNumberUnsuccess() throws Exception {
        when(pdpService.getGoal(COLLEAGUE_UUID, GOAL_NUMBER_1)).thenThrow(NotFoundException.class);

        performGet(status().isNotFound(), PDP_GOALS_URL + "/numbers/{number}", GOAL_NUMBER_1);
    }

    @Test
    void getGoalByUuid() throws Exception {
        when(pdpService.getGoal(COLLEAGUE_UUID, GOAL_UUID_1)).thenReturn(buildGoal(GOAL_UUID_1, GOAL_NUMBER_1));

        var result = performGet(status().isOk(), PDP_GOALS_URL + "/{goalUuid}", GOAL_UUID_1);

        assertResponseContent(result.getResponse(), PDP_GOAL_GET_OK_RESPONSE_JSON_FILE_NAME);
    }

    @Test
    void getGoalByUuidUnsuccess() throws Exception {
        when(pdpService.getGoal(COLLEAGUE_UUID, GOAL_UUID_1)).thenThrow(NotFoundException.class);

        performGet(status().isNotFound(), PDP_GOALS_URL + "/{goalUuid}", GOAL_UUID_1);
    }

    @Test
    void getGoalsByColleague() throws Exception {
        when(pdpService.getGoals(COLLEAGUE_UUID))
                .thenReturn(List.of(buildGoal(GOAL_UUID_1, GOAL_NUMBER_1), buildGoal(GOAL_UUID_2, GOAL_NUMBER_2)));

        var result = performGet(status().isOk(), PDP_GOALS_URL);

        assertResponseContent(result.getResponse(), PDP_GOALS_GET_OK_RESPONSE_JSON_FILE_NAME);
    }

    @Test
    void getGoalsByColleagueUnsuccess() throws Exception {
        when(pdpService.getGoals(COLLEAGUE_UUID)).thenThrow(NotFoundException.class);

        performGet(status().isNotFound(), PDP_GOALS_URL);
    }

    private PDPGoal buildGoal(UUID uuid, int number) {
        return new PDPGoal(uuid, COLLEAGUE_UUID, number, PROPERTIES, ACHIEVEMENT_DATE, PUBLISHED);
    }
}