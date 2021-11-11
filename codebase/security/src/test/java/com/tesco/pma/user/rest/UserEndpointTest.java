package com.tesco.pma.user.rest;

import com.tesco.pma.api.User;
import com.tesco.pma.exception.ErrorCodes;
import com.tesco.pma.rest.AbstractEndpointTest;
import com.tesco.pma.user.UserService;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserEndpoint.class)
class UserEndpointTest extends AbstractEndpointTest {
    static final EasyRandom RANDOM = new EasyRandom();
    static final String ERRORS_0_CODE_JSON_PATH = "$.errors[0].code";

    @MockBean
    protected UserService mockUserService;

    @Test
    void getUserByColleagueUuidSucceeded() throws Exception {
        final var user = randomUser();
        final var colleagueUuid = user.getColleagueUuid();
        when(mockUserService.findUserByColleagueUuid(eq(colleagueUuid)))
                .thenReturn(Optional.of(user));

        mvc.perform(get("/users/{colleagueUuid}", colleagueUuid)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data.colleagueUuid", equalTo(colleagueUuid.toString())));
    }

    @Test
    @SuppressWarnings("java:S2699")
    void getUserByColleagueUuidNotFound() throws Exception {
        final var colleagueUuid = randomUuid();
        when(mockUserService.findUserByColleagueUuid(eq(colleagueUuid)))
                .thenReturn(Optional.empty());

        mvc.perform(get("/users/{colleagueUuid}", colleagueUuid)
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
        when(mockUserService.findUserByIamId(eq(iamId)))
                .thenReturn(Optional.of(user));

        mvc.perform(get("/users/iam-ids/{iamId}", iamId)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data.colleagueUuid", equalTo(user.getColleagueUuid().toString())));
    }

    @Test
    @SuppressWarnings("java:S2699")
    void getUserByIamIdNotFound() throws Exception {
        final var iamId = RANDOM.nextObject(String.class);
        when(mockUserService.findUserByIamId(eq(iamId)))
                .thenReturn(Optional.empty());

        mvc.perform(get("/users/iam-ids/{iamId}", iamId)
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
                .with(user(user.getColleagueUuid().toString()))
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data.colleagueUuid", equalTo(user.getColleagueUuid().toString())));

        final var authenticationCaptor = ArgumentCaptor.forClass(Authentication.class);
        verify(mockUserService).findUserByAuthentication(authenticationCaptor.capture());
        assertThat(authenticationCaptor.getValue().getName()).isEqualTo(user.getColleagueUuid().toString());
    }

    @Test
    void getMeNotFound() throws Exception {
        final var userName = "test-user";
        when(mockUserService.findUserByAuthentication(any()))
                .thenReturn(Optional.empty());

        mvc.perform(get("/users/me")
                .with(user(userName))
                .accept(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath(ERRORS_0_CODE_JSON_PATH, equalTo(ErrorCodes.USER_NOT_FOUND.getCode())))
                .andExpect(jsonPath("$.errors[0].message", containsString("authentication.name")))
                .andExpect(jsonPath("$.errors[0].message", containsString(userName)));

        final var authenticationCaptor = ArgumentCaptor.forClass(Authentication.class);
        verify(mockUserService).findUserByAuthentication(authenticationCaptor.capture());
        assertThat(authenticationCaptor.getValue().getName()).isEqualTo(userName);
    }

    private UUID randomUuid() {
        return UUID.randomUUID();
    }

    private User randomUser() {
        return RANDOM.nextObject(User.class);
    }

}