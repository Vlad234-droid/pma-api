package com.tesco.pma.user.rest;

import com.tesco.pma.TestConfig;
import com.tesco.pma.api.User;
import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.exception.ErrorCodes;
import com.tesco.pma.rest.AbstractEndpointTest;
import com.tesco.pma.user.UserService;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserEndpoint.class)
@ContextConfiguration(classes = TestConfig.class)
class UserEndpointTest extends AbstractEndpointTest {
    private static final UUID COLLEAGUE_UUID = UUID.fromString("10000001-1001-1001-1001-100000000001");

    static final EasyRandom RANDOM = new EasyRandom();
    static final String ERRORS_0_CODE_JSON_PATH = "$.errors[0].code";

    @MockBean
    protected UserService mockUserService;

    @ParameterizedTest
    @CsvSource({
            "200,Admin",
            "200,Colleague",
            "200,LineManager",
            "200,PeopleTeam",
            "200,TalentAdmin",
            "200,ProcessManager"
    })
    void getUserByColleagueUuidSucceeded(int status, String role) throws Exception {
        final var user = randomUser();
        final var colleagueUuid = user.getColleague().getColleagueUUID();
        when(mockUserService.findUserByColleagueUuid(colleagueUuid))
                .thenReturn(Optional.of(user));

        mvc.perform(get("/users/{colleagueUuid}", colleagueUuid)
                        .with(roles(List.of(role), COLLEAGUE_UUID.toString()))
                        .accept(APPLICATION_JSON))
                .andExpect(status().is(status))
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data.colleague.colleagueUUID", equalTo(colleagueUuid.toString())));
    }

    @Test
    @SuppressWarnings("java:S2699")
    void getUserByColleagueUuidNotFound() throws Exception {
        final var colleagueUuid = randomUuid();
        when(mockUserService.findUserByColleagueUuid(colleagueUuid))
                .thenReturn(Optional.empty());

        mvc.perform(get("/users/{colleagueUuid}", colleagueUuid)
                        .with(colleague(colleagueUuid.toString()))
                        .accept(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath(ERRORS_0_CODE_JSON_PATH, equalTo(ErrorCodes.USER_NOT_FOUND.getCode())));
    }

    @Test
    @SuppressWarnings("java:S2699")
    void getUserByIamIdSucceeded() throws Exception {
        final var user = randomUser();
        final var iamId = RANDOM.nextObject(String.class);
        when(mockUserService.findUserByIamId(iamId))
                .thenReturn(Optional.of(user));

        mvc.perform(get("/users/iam-ids/{iamId}", iamId)
                        .with(colleague(COLLEAGUE_UUID.toString()))
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data.colleague.colleagueUUID", equalTo(user.getColleague().getColleagueUUID().toString())));
    }

    @Test
    @SuppressWarnings("java:S2699")
    void getUserByIamIdNotFound() throws Exception {
        final var iamId = RANDOM.nextObject(String.class);
        when(mockUserService.findUserByIamId(iamId))
                .thenReturn(Optional.empty());

        mvc.perform(get("/users/iam-ids/{iamId}", iamId)
                        .with(colleague(COLLEAGUE_UUID.toString()))
                        .accept(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath(ERRORS_0_CODE_JSON_PATH, equalTo(ErrorCodes.USER_NOT_FOUND.getCode())));
    }

    @Test
    void getMeUserFound() throws Exception {
        final var user = randomUser();
        when(mockUserService.findUserByAuthentication(any()))
                .thenReturn(Optional.of(user));

        mvc.perform(get("/users/me")
                        .with(colleague(user.getColleague().getColleagueUUID().toString()))
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data.colleague.colleagueUUID", equalTo(user.getColleague().getColleagueUUID().toString())));

        final var authenticationCaptor = ArgumentCaptor.forClass(Authentication.class);
        verify(mockUserService).findUserByAuthentication(authenticationCaptor.capture());
        assertThat(authenticationCaptor.getValue().getName()).isEqualTo(user.getColleague().getColleagueUUID().toString());
    }

    @Test
    void getMeNotFound() throws Exception {
        final var subject = randomUuid().toString();
        when(mockUserService.findUserByAuthentication(any()))
                .thenReturn(Optional.empty());

        mvc.perform(get("/users/me")
                        .with(colleague(subject))
                        .accept(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath(ERRORS_0_CODE_JSON_PATH, equalTo(ErrorCodes.USER_NOT_FOUND.getCode())))
                .andExpect(jsonPath("$.errors[0].message", containsString("authentication.name")))
                .andExpect(jsonPath("$.errors[0].message", containsString(subject)));

        final var authenticationCaptor = ArgumentCaptor.forClass(Authentication.class);
        verify(mockUserService).findUserByAuthentication(authenticationCaptor.capture());
        assertThat(authenticationCaptor.getValue().getName()).isEqualTo(subject);
    }

    private UUID randomUuid() {
        return UUID.randomUUID();
    }

    private User randomUser() {
        var colleague = new Colleague();
        colleague.setColleagueUUID(COLLEAGUE_UUID);

        var user = new User();
        user.setColleague(colleague);
        return user;
    }

}