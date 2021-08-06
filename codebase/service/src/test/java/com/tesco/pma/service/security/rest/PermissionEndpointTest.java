package com.tesco.pma.service.security.rest;

import com.tesco.pma.api.security.SubsidiaryPermission;
import com.tesco.pma.exception.AlreadyExistsException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.rest.AbstractEndpointTest;
import com.tesco.pma.service.security.SubsidiaryPermissionService;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PermissionEndpoint.class)
@SuppressWarnings("PMD")
class PermissionEndpointTest extends AbstractEndpointTest {
    static final EasyRandom RANDOM = new EasyRandom();
    static final SubsidiaryPermission VALID_SUBSIDIARY_PERMISSION = SubsidiaryPermission.of(UUID.fromString("1a2a7ac9-0a0a-abe3-856b-c7be9b9f5a12"),
            UUID.fromString("5d9bbac9-850a-45e3-856b-50be9b9f563c"), "SubsidiaryManager");
    static final String BASE_PATH = "/subsidiaries/{subsidiaryUuid}/permissions";
    static final String MODIFY_PERMISSION_PATH = BASE_PATH + "/colleagues/{colleagueUuid}/roles/{role}";

    @MockBean
    private SubsidiaryPermissionService mockSubsidiaryPermissionService;

    @Test
    void grantSubsidiaryPermissionSucceeded() throws Exception {
        mvc.perform(post(MODIFY_PERMISSION_PATH, VALID_SUBSIDIARY_PERMISSION.getSubsidiaryUuid(),
                VALID_SUBSIDIARY_PERMISSION.getColleagueUuid(), VALID_SUBSIDIARY_PERMISSION.getRole()))
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json(SUCCESS_RESPONSE_WITHOUT_DATA, true))
                .andExpect(status().isOk());

        verify(mockSubsidiaryPermissionService).grantSubsidiaryPermission(VALID_SUBSIDIARY_PERMISSION);
    }

    @Test
    void grantSubsidiaryPermissionSubsidiaryNotFound() throws Exception { // NOSONAR Spring MVC test assertion used
        doThrow(NotFoundException.class).when(mockSubsidiaryPermissionService)
                .grantSubsidiaryPermission(VALID_SUBSIDIARY_PERMISSION);

        mvc.perform(post(MODIFY_PERMISSION_PATH, VALID_SUBSIDIARY_PERMISSION.getSubsidiaryUuid(),
                VALID_SUBSIDIARY_PERMISSION.getColleagueUuid(), VALID_SUBSIDIARY_PERMISSION.getRole()))
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(status().isNotFound());
    }

    @Test
    void grantSubsidiaryPermissionPermissionAlreadyExists() throws Exception { // NOSONAR Spring MVC test assertion used
        doThrow(AlreadyExistsException.class).when(mockSubsidiaryPermissionService)
                .grantSubsidiaryPermission(VALID_SUBSIDIARY_PERMISSION);

        mvc.perform(post(MODIFY_PERMISSION_PATH, VALID_SUBSIDIARY_PERMISSION.getSubsidiaryUuid(),
                VALID_SUBSIDIARY_PERMISSION.getColleagueUuid(), VALID_SUBSIDIARY_PERMISSION.getRole()))
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(status().isConflict());
    }

    @Test
    void revokeSubsidiaryPermissionSucceeded() throws Exception {
        mvc.perform(delete(MODIFY_PERMISSION_PATH, VALID_SUBSIDIARY_PERMISSION.getSubsidiaryUuid(),
                VALID_SUBSIDIARY_PERMISSION.getColleagueUuid(), VALID_SUBSIDIARY_PERMISSION.getRole()))
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json(SUCCESS_RESPONSE_WITHOUT_DATA, true))
                .andExpect(status().isOk());

        verify(mockSubsidiaryPermissionService).revokeSubsidiaryPermission(VALID_SUBSIDIARY_PERMISSION);
    }

    @Test
    void revokeSubsidiaryPermissionSubsidiaryPermissionNotFound() throws Exception { // NOSONAR Spring MVC test assertion used
        doThrow(NotFoundException.class).when(mockSubsidiaryPermissionService)
                .revokeSubsidiaryPermission(VALID_SUBSIDIARY_PERMISSION);

        mvc.perform(delete(MODIFY_PERMISSION_PATH, VALID_SUBSIDIARY_PERMISSION.getSubsidiaryUuid(),
                VALID_SUBSIDIARY_PERMISSION.getColleagueUuid(), VALID_SUBSIDIARY_PERMISSION.getRole()))
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSubsidiaryPermissionsWithNotEmptyData() throws Exception {
        final var subsidiaryPermissions = RANDOM.objects(SubsidiaryPermission.class, 5).collect(Collectors.toSet());
        when(mockSubsidiaryPermissionService.findSubsidiaryPermissionsForSubsidiary(VALID_SUBSIDIARY_PERMISSION.getSubsidiaryUuid()))
                .thenReturn(subsidiaryPermissions);

        mvc.perform(get(BASE_PATH, VALID_SUBSIDIARY_PERMISSION.getSubsidiaryUuid()))
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.success", equalTo(true)))
                .andExpect(jsonPath("$.data.length()", equalTo(5)))
                .andExpect(jsonPath("$.data[*].subsidiaryUuid").value(containsInAnyOrder(subsidiaryPermissions.stream()
                        .map(SubsidiaryPermission::getSubsidiaryUuid).map(UUID::toString).toArray())))
                .andExpect(status().isOk());
    }

    @Test
    void getSubsidiaryPermissionsWithEmptyData() throws Exception {
        when(mockSubsidiaryPermissionService.findSubsidiaryPermissionsForSubsidiary(VALID_SUBSIDIARY_PERMISSION.getSubsidiaryUuid()))
                .thenReturn(Collections.emptySet());

        mvc.perform(get(BASE_PATH, VALID_SUBSIDIARY_PERMISSION.getSubsidiaryUuid()))
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(content().json(SUCCESS_RESPONSE_WITH_EMPTY_ARRAY_DATA, true))
                .andExpect(status().isOk());
    }

    @Test
    void getSubsidiaryPermissionsInvalidSubsidiaryUuidParam() throws Exception {
        mvc.perform(get(BASE_PATH, "not-valid-uuid"))
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.success", equalTo(false)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(mockSubsidiaryPermissionService);
    }
}