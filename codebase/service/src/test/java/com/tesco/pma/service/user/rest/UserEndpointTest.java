package com.tesco.pma.service.user.rest;

import com.tesco.pma.api.User;
import com.tesco.pma.exception.ErrorCodes;
import com.tesco.pma.security.UserRoleNames;
import com.tesco.pma.rest.AbstractEndpointTest;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.tesco.pma.service.user.UserIncludes.SUBSIDIARY_PERMISSIONS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
    static final String INCLUDES_PARAM_NAME = "includes";
    static final String ERRORS_0_CODE_JSON_PATH = "$.errors[0].code";

    @Test
    void getUserByColleagueUuidSucceeded() throws Exception {
        final var user = randomUser();
        final var colleagueUuid = user.getColleagueUuid();
        when(mockUserService.findUserByColleagueUuid(eq(colleagueUuid), eq(Set.of(SUBSIDIARY_PERMISSIONS))))
                .thenReturn(Optional.of(user));

        mvc.perform(get("/users/{colleagueUuid}", colleagueUuid)
                .queryParam(INCLUDES_PARAM_NAME, SUBSIDIARY_PERMISSIONS.name())
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data.colleagueUuid", equalTo(colleagueUuid.toString())));
    }

    @Test
    @SuppressWarnings("java:S2699")
    void getUserByColleagueUuidNotFound() throws Exception {
        final var colleagueUuid = randomUuid();
        when(mockUserService.findUserByColleagueUuid(eq(colleagueUuid), eq(Set.of(SUBSIDIARY_PERMISSIONS))))
                .thenReturn(Optional.empty());

        mvc.perform(get("/users/{colleagueUuid}", colleagueUuid)
                .queryParam(INCLUDES_PARAM_NAME, SUBSIDIARY_PERMISSIONS.name())
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
        when(mockUserService.findUserByIamId(eq(iamId), eq(Set.of(SUBSIDIARY_PERMISSIONS))))
                .thenReturn(Optional.of(user));

        mvc.perform(get("/users/iam-ids/{iamId}", iamId)
                .queryParam(INCLUDES_PARAM_NAME, SUBSIDIARY_PERMISSIONS.name())
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data.colleagueUuid", equalTo(user.getColleagueUuid().toString())));
    }

    @Test
    @SuppressWarnings("java:S2699")
    void getUserByIamIdNotFound() throws Exception {
        final var iamId = RANDOM.nextObject(String.class);
        when(mockUserService.findUserByIamId(eq(iamId), eq(Set.of(SUBSIDIARY_PERMISSIONS))))
                .thenReturn(Optional.empty());

        mvc.perform(get("/users/iam-ids/{iamId}", iamId)
                .queryParam(INCLUDES_PARAM_NAME, SUBSIDIARY_PERMISSIONS.name())
                .accept(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath(ERRORS_0_CODE_JSON_PATH, equalTo(ErrorCodes.USER_NOT_FOUND.getCode())));
    }

    @Test
    @SuppressWarnings("java:S2699")
    void getUsersHasSubsidiaryPermissionSucceeded() throws Exception {
        final var subsidiaryUuid = randomUuid();
        final var role = UserRoleNames.SUBSIDIARY_MANAGER;
        final var user1 = randomUser();
        final var user2 = randomUser();
        when(mockUserService.findUsersHasSubsidiaryPermission(eq(subsidiaryUuid), eq(role), eq(Set.of(SUBSIDIARY_PERMISSIONS))))
                .thenReturn(List.of(user1, user2));

        mvc.perform(get("/users")
                .queryParam(INCLUDES_PARAM_NAME, SUBSIDIARY_PERMISSIONS.name())
                .queryParam("subsidiaryPermission.subsidiaryUuid", subsidiaryUuid.toString())
                .queryParam("subsidiaryPermission.role", role)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data[0].colleagueUuid", equalTo(user1.getColleagueUuid().toString())))
                .andExpect(jsonPath("$.data[1].colleagueUuid", equalTo(user2.getColleagueUuid().toString())));
    }

    @Test
    void getUsersHasSubsidiaryPermissionNotAllowedRole() throws Exception {
        mvc.perform(get("/users")
                .queryParam("subsidiaryPermission.subsidiaryUuid", randomUuid().toString())
                .queryParam("subsidiaryPermission.role", "dummy-role")
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath(ERRORS_0_CODE_JSON_PATH, equalTo(ErrorCodes.CONSTRAINT_VIOLATION.getCode())));

        verify(mockUserService, never()).findUsersHasSubsidiaryPermission(any(), any(), anyCollection());
    }

    @Test
    void getMeUserFound() throws Exception {
        final var user = randomUser();
        when(mockUserService.findUserByAuthentication(any(), anyCollection()))
                .thenReturn(Optional.of(user));

        mvc.perform(get("/users/me")
                .with(user(user.getColleagueUuid().toString()))
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data.colleagueUuid", equalTo(user.getColleagueUuid().toString())));

        final var authenticationCaptor = ArgumentCaptor.forClass(Authentication.class);
        verify(mockUserService).findUserByAuthentication(authenticationCaptor.capture(), eq(Collections.emptySet()));
        assertThat(authenticationCaptor.getValue().getName()).isEqualTo(user.getColleagueUuid().toString());
    }

    @Test
    void getMeNotFound() throws Exception {
        final var userName = "test-user";
        when(mockUserService.findUserByAuthentication(any(), anyCollection()))
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
        verify(mockUserService).findUserByAuthentication(authenticationCaptor.capture(), eq(Collections.emptySet()));
        assertThat(authenticationCaptor.getValue().getName()).isEqualTo(userName);
    }

    private UUID randomUuid() {
        return UUID.randomUUID();
    }

    private User randomUser() {
        return RANDOM.nextObject(User.class);
    }

}