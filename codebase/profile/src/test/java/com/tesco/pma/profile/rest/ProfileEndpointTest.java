package com.tesco.pma.profile.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesco.pma.api.User;
import com.tesco.pma.profile.domain.ProfileAttribute;
import com.tesco.pma.profile.rest.model.ProfileResponse;
import com.tesco.pma.profile.service.ProfileService;
import com.tesco.pma.rest.AbstractEndpointTest;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.StatusResultMatchers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProfileEndpoint.class, properties = {
        "tesco.application.security.enabled=false",
})
class ProfileEndpointTest extends AbstractEndpointTest {

    private static final EasyRandom RANDOM = new EasyRandom();

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    protected MockMvc mvc;

    @MockBean
    private ProfileService mockProfileService;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getProfileByColleagueUuidShouldReturnProfileBy() throws Exception {
        final var user = randomUser();
        final var colleagueUuid = user.getColleagueUuid();

        when(mockProfileService.findProfileByColleagueUuid(colleagueUuid))
                .thenReturn(Optional.of(randomProfileResponse()));

        mvc.perform(get("/profiles/{colleagueUuid}", colleagueUuid)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));

    }

    @Test
    void updateProfileAttributesShouldReturnUpdatedProfileAttributes() throws Exception {

        List<ProfileAttribute> profileAttributes = profileAttributes(3);

        // given
        when(mockProfileService.updateProfileAttributes(profileAttributes))
                .thenReturn(profileAttributes);

        // when
        ResultActions resultActions = mvc.perform(put("/profiles")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(profileAttributes))
                        .accept(APPLICATION_JSON));

        // then
        andExpect(resultActions, status().isOk());

    }

    @Test
    void createProfileAttributesShouldReturnInsertedProfileAttributes() throws Exception {

        List<ProfileAttribute> profileAttributes = profileAttributes(3);

        // given
        when(mockProfileService.createProfileAttributes(profileAttributes))
                .thenReturn(profileAttributes);

        // when
        ResultActions resultActions = mvc.perform(post("/profiles")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profileAttributes))
                .accept(APPLICATION_JSON));

        // then
        andExpect(resultActions, status().isCreated());

    }

    @Test
    void deleteProfileAttributesShouldReturnDeletedProfileAttributes() throws Exception {

        List<ProfileAttribute> profileAttributes = profileAttributes(3);

        // given
        when(mockProfileService.deleteProfileAttributes(profileAttributes))
                .thenReturn(profileAttributes);

        // when
        ResultActions resultActions = mvc.perform(delete("/profiles")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profileAttributes))
                .accept(APPLICATION_JSON));

        // then
        andExpect(resultActions, status().isOk());

    }

    private UUID randomUuid() {
        return UUID.randomUUID();
    }

    private User randomUser() {
        return RANDOM.nextObject(User.class);
    }

    private ProfileResponse randomProfileResponse() {
        return RANDOM.nextObject(ProfileResponse.class);
    }

    private List<ProfileAttribute> profileAttributes(int size) {
        return IntStream.rangeClosed(1, size)
                .mapToObj(this::profileAttribute)
                .collect(Collectors.toList());
    }

    private ProfileAttribute profileAttribute(int index) {
        return randomProfileAttribute();
    }

    private ProfileAttribute randomProfileAttribute() {
        return RANDOM.nextObject(ProfileAttribute.class);
    }

    private void andExpect(ResultActions resultActions,
                           ResultMatcher status) throws Exception {
        resultActions
                .andExpect(status)
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data[0].colleagueUuid").exists())
                .andExpect(jsonPath("$.data[1].colleagueUuid").exists())
                .andExpect(jsonPath("$.data[2].colleagueUuid").isString());
    }

}