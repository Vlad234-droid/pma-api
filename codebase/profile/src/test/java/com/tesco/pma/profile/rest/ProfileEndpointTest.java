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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
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
        andExpect(resultActions, status().isOk(), profileAttributes);

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
        andExpect(resultActions, status().isCreated(), profileAttributes);

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
        andExpect(resultActions, status().isOk(), profileAttributes);

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
                .mapToObj(value ->  profileAttribute())
                .collect(Collectors.toList());
    }

    private ProfileAttribute profileAttribute() {
        return randomProfileAttribute();
    }

    private ProfileAttribute randomProfileAttribute() {
        return RANDOM.nextObject(ProfileAttribute.class);
    }

    private void andExpect(ResultActions resultActions,
                           ResultMatcher status,
                           List<ProfileAttribute> profileAttributes) throws Exception {

        String colleagueUuidExpression = "$.data[%s].colleagueUuid";

        List<String> colleagueUuids = profileAttributes.stream()
                .map(profileAttribute -> profileAttribute.getColleagueUuid().toString())
                .collect(Collectors.toList());

        resultActions
                .andExpect(status)
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath(colleagueUuidExpression, 0).exists())
                .andExpect(jsonPath(colleagueUuidExpression, 1).exists())
                .andExpect(jsonPath(colleagueUuidExpression, 2).isString())
                .andExpect(jsonPath(colleagueUuidExpression, 2).value(is(in(colleagueUuids))));

    }

}