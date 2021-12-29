package com.tesco.pma.pdp.rest;

import com.tesco.pma.api.MapJson;
import com.tesco.pma.pdp.api.PDPGoalStatus;
import com.tesco.pma.pdp.domain.PDPGoal;
import com.tesco.pma.pdp.service.PDPService;
import com.tesco.pma.rest.AbstractEndpointTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PDPEndpoint.class)
public class PDPEndpointTest extends AbstractEndpointTest {

    private static final UUID GOAL_UUID_1 = UUID.fromString("6d37262f-3a00-4706-a74b-6bf98be65765");
    public static final UUID COLLEAGUE_UUID = UUID.fromString("ce245be1-1f43-4d5f-85dc-db6e2cce0c2a");

    private static final String PDP_URL = "/pdp";

    @MockBean
    private PDPService pdpService;

    @Test
    void getByUuid() throws Exception {
        when(pdpService.getGoal(COLLEAGUE_UUID, GOAL_UUID_1)).thenReturn(buildGoal(GOAL_UUID_1, 1));

        var result = performGetWith(user(COLLEAGUE_UUID.toString()),
                status().isOk(), PDP_URL + "/goals/{goalUuid}", GOAL_UUID_1);

        assertResponseContent(result.getResponse(), "pdp_goal_get_ok_response.json");
    }

    private PDPGoal buildGoal(UUID uuid, int number) {
        var properties = new MapJson(Map.of("pm_pdp_test_property1", "P1", "pm_pdp_test_property2", "P2"));
        return new PDPGoal(uuid, COLLEAGUE_UUID, number, properties, LocalDate.now(), PDPGoalStatus.PUBLISHED);
    }
}