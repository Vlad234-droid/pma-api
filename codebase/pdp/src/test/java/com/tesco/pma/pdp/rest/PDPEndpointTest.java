package com.tesco.pma.pdp.rest;

import com.tesco.pma.api.MapJson;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.file.api.File;
import com.tesco.pma.pdp.LocalTestConfig;
import com.tesco.pma.pdp.api.PDPGoal;
import com.tesco.pma.pdp.service.PDPService;
import com.tesco.pma.rest.AbstractEndpointTest;
import com.tesco.pma.util.ResourceProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.pdp.api.PDPGoalStatus.PUBLISHED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PDPEndpoint.class)
@ContextConfiguration(classes = {LocalTestConfig.class, PDPEndpoint.class})
@WithMockUser(username = PDPEndpointTest.COLLEAGUE_UUID_STR)
class PDPEndpointTest extends AbstractEndpointTest {

    static final String COLLEAGUE_UUID_STR = "ce245be1-1f43-4d5f-85dc-db6e2cce0c2a";

    private static final UUID GOAL_UUID_1 = UUID.fromString("6d37262f-3a00-4706-a74b-6bf98be65765");
    private static final UUID GOAL_UUID_2 = UUID.fromString("7d37262f-3a00-4706-a74b-6bf98be65765");
    private static final UUID COLLEAGUE_UUID = UUID.fromString(COLLEAGUE_UUID_STR);
    private static final UUID FILE_UUID = UUID.fromString("1d37262f-3a00-4706-a74b-6bf98be65767");

    private static final String PDP_GOALS_URL = "/pdp/goals";
    private static final String PDP_GOAL_URL = PDP_GOALS_URL + "/{goalUuid}";
    private static final String PDP_GOAL_EARLY_DATE_URL = PDP_GOALS_URL + "/early-achievement-date";
    private static final int GOAL_NUMBER_1 = 1;
    private static final int GOAL_NUMBER_2 = 2;

    private static final String PDP_GOALS_UPDATE_REQUEST_JSON_FILE_NAME = "pm_pdp_goals_update_request.json";
    private static final String PDP_GOAL_GET_OK_RESPONSE_JSON_FILE_NAME = "pm_pdp_goal_get_ok_response.json";
    private static final String PDP_GOALS_GET_OK_RESPONSE_JSON_FILE_NAME = "pm_pdp_goals_get_ok_response.json";
    private static final String PDP_GOALS_UPDATE_OK_RESPONSE_JSON_FILE_NAME = "pm_pdp_goals_update_ok_response.json";
    private static final LocalDate ACHIEVEMENT_DATE = LocalDate.parse("2021-12-29");
    private static final MapJson PROPERTIES = new MapJson(Map.of("pm_pdp_test_property1", "P1", "pm_pdp_test_property2", "P2"));

    private static final String PDP_FORM_PATH = "pdp/forms";
    private static final String PDP_FORM_NAME = "standard_pdp.form";

    private static final UUID PDP_TEMPLATE_UUID = UUID.fromString("c8727e57-8844-4db5-b1b3-7548b7582244");
    private static final String PDP_TEMPLATE_PATH = "pdp/templates";
    private static final String PDP_TEMPLATE_FILE_NAME = "Personal Development Plan Template.pptx";
    private static final String PDP_GOAL_EARLY_DATE_GET_RESPONSE_JSON_FILE_NAME = "pm_pdp_goal_early_date_get_ok_response.json";

    @MockBean
    private PDPService pdpService;

    @MockBean
    private ResourceProvider resourceProvider;

    @MockBean
    private NamedMessageSourceAccessor messages;

    @Test
    void createGoals() throws Exception {
        var goals = buildGoals(GOAL_UUID_1, GOAL_UUID_2);
        when(pdpService.createGoals(COLLEAGUE_UUID, goals)).thenReturn(goals);

        var result = performPost(PDP_GOALS_UPDATE_REQUEST_JSON_FILE_NAME, status().isCreated(), PDP_GOALS_URL);

        assertResponseContent(result.getResponse(), PDP_GOALS_UPDATE_OK_RESPONSE_JSON_FILE_NAME);
    }

    @Test
    void createGoalsUnsuccessIfBadRequest() throws Exception { //NOSONAR used MockMvc checks
        doThrow(DatabaseConstraintViolationException.class).when(pdpService).createGoals(COLLEAGUE_UUID,
                buildGoals(GOAL_UUID_1, GOAL_UUID_2));

        performPost(PDP_GOALS_UPDATE_REQUEST_JSON_FILE_NAME, status().isBadRequest(), PDP_GOALS_URL);
    }

    @Test
    void updateGoals() throws Exception {
        var goals = buildGoals(GOAL_UUID_1, GOAL_UUID_2);
        when(pdpService.updateGoals(COLLEAGUE_UUID, goals)).thenReturn(goals);

        var result = performPut(PDP_GOALS_UPDATE_REQUEST_JSON_FILE_NAME, status().isOk(), PDP_GOALS_URL);

        assertResponseContent(result.getResponse(), PDP_GOALS_UPDATE_OK_RESPONSE_JSON_FILE_NAME);
    }

    @Test
    void updateGoalsUnsuccessIfGoalIsNotFound() throws Exception { //NOSONAR used MockMvc checks
        doThrow(NotFoundException.class).when(pdpService).updateGoals(COLLEAGUE_UUID, buildGoals(GOAL_UUID_1, GOAL_UUID_2));

        performPut(PDP_GOALS_UPDATE_REQUEST_JSON_FILE_NAME, status().isNotFound(), PDP_GOALS_URL);
    }

    @Test
    void updateGoalsUnsuccessIfGoalIfBadRequest() throws Exception { //NOSONAR used MockMvc checks
        doThrow(DatabaseConstraintViolationException.class).when(pdpService).updateGoals(COLLEAGUE_UUID,
                buildGoals(GOAL_UUID_1, GOAL_UUID_2));

        performPut(PDP_GOALS_UPDATE_REQUEST_JSON_FILE_NAME, status().isBadRequest(), PDP_GOALS_URL);
    }

    @Test
    void deleteGoal() throws Exception { //NOSONAR used MockMvc checks
        doNothing().when(pdpService).deleteGoal(COLLEAGUE_UUID, GOAL_UUID_1);

        performDelete(status().isOk(), PDP_GOAL_URL, GOAL_UUID_1);
    }

    @Test
    void deleteGoalUnsuccessIfGoalIsNotFound() throws Exception { //NOSONAR used MockMvc checks
        doThrow(NotFoundException.class).when(pdpService).deleteGoal(COLLEAGUE_UUID, GOAL_UUID_1);

        performDelete(status().isNotFound(), PDP_GOAL_URL, GOAL_UUID_1);
    }

    @Test
    void getGoalByColleagueAndNumber() throws Exception {
        when(pdpService.getGoal(COLLEAGUE_UUID, GOAL_NUMBER_1)).thenReturn(buildGoal(GOAL_UUID_1, GOAL_NUMBER_1));
        when(resourceProvider.readFile(PDP_FORM_PATH, PDP_FORM_NAME)).thenReturn(file(FILE_UUID, "test json"));

        var result = performGet(status().isOk(), PDP_GOALS_URL + "/numbers/{number}", GOAL_NUMBER_1);

        assertResponseContent(result.getResponse(), PDP_GOAL_GET_OK_RESPONSE_JSON_FILE_NAME);
    }

    @Test
    void getGoalByColleagueAndNumberUnsuccessIfGoalIsNotFound() throws Exception { //NOSONAR used MockMvc checks
        when(pdpService.getGoal(COLLEAGUE_UUID, GOAL_NUMBER_1)).thenThrow(NotFoundException.class);

        performGet(status().isNotFound(), PDP_GOALS_URL + "/numbers/{number}", GOAL_NUMBER_1);
    }

    @Test
    void getGoalByUuid() throws Exception {
        when(pdpService.getGoal(COLLEAGUE_UUID, GOAL_UUID_1)).thenReturn(buildGoal(GOAL_UUID_1, GOAL_NUMBER_1));
        when(resourceProvider.readFile(PDP_FORM_PATH, PDP_FORM_NAME)).thenReturn(file(FILE_UUID, "test json"));

        var result = performGet(status().isOk(), PDP_GOAL_URL, GOAL_UUID_1);

        assertResponseContent(result.getResponse(), PDP_GOAL_GET_OK_RESPONSE_JSON_FILE_NAME);
    }

    @Test
    void getGoalByUuidUnsuccessIfGoalIsNotFound() throws Exception { //NOSONAR used MockMvc checks
        when(pdpService.getGoal(COLLEAGUE_UUID, GOAL_UUID_1)).thenThrow(NotFoundException.class);

        performGet(status().isNotFound(), PDP_GOAL_URL, GOAL_UUID_1);
    }

    @Test
    void getGoalsByColleague() throws Exception {
        when(pdpService.getGoals(COLLEAGUE_UUID)).thenReturn(buildGoals(GOAL_UUID_1, GOAL_UUID_2));
        when(resourceProvider.readFile(PDP_FORM_PATH, PDP_FORM_NAME)).thenReturn(file(FILE_UUID, "test json"));

        var result = performGet(status().isOk(), PDP_GOALS_URL);

        assertResponseContent(result.getResponse(), PDP_GOALS_GET_OK_RESPONSE_JSON_FILE_NAME);
    }

    @Test
    void downloadTemplate() throws Exception {
        var content = "0123456789";
        when(resourceProvider.readFile(PDP_TEMPLATE_PATH, PDP_TEMPLATE_FILE_NAME)).thenReturn(file(PDP_TEMPLATE_UUID, content));

        var result = performGet(status().isOk(), MediaType.APPLICATION_OCTET_STREAM,"/pdp/template");
        assertNotNull(result);
        assertNotNull(result.getResponse());
        assertEquals(content, result.getResponse().getContentAsString());
    }

    @Test
    void getEarlyAchievementDate() throws Exception {
        when(pdpService.getEarlyAchievementDate(COLLEAGUE_UUID)).thenReturn(ACHIEVEMENT_DATE);

        var result = performGet(status().isOk(), PDP_GOAL_EARLY_DATE_URL, GOAL_UUID_1);

        assertResponseContent(result.getResponse(), PDP_GOAL_EARLY_DATE_GET_RESPONSE_JSON_FILE_NAME);
    }

    @Test
    void getEarlyAchievementDateIfGoalIsNotFound() throws Exception { //NOSONAR used MockMvc checks
        when(pdpService.getEarlyAchievementDate(COLLEAGUE_UUID)).thenThrow(NotFoundException.class);

        performGet(status().isNotFound(), PDP_GOAL_EARLY_DATE_URL, GOAL_UUID_1);
    }

    private PDPGoal buildGoal(UUID uuid, int number) {
        return new PDPGoal(uuid, COLLEAGUE_UUID, number, PROPERTIES, ACHIEVEMENT_DATE, PUBLISHED);
    }

    private List<PDPGoal> buildGoals(UUID uuid1, UUID uuid2) {
        return List.of(buildGoal(uuid1, GOAL_NUMBER_1), buildGoal(uuid2, GOAL_NUMBER_2));
    }

    private File file(UUID fileUuid, String content) {
        var file = new File();
        file.setUuid(fileUuid);
        file.setFileContent(content.getBytes());
        file.setFileLength(content.length());
        return file;
    }

}