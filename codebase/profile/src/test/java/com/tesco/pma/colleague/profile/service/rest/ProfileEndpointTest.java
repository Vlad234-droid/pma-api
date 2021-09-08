package com.tesco.pma.colleague.profile.service.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesco.pma.colleague.profile.domain.TypedAttribute;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.colleague.profile.service.rest.model.AggregatedColleague;
import com.tesco.pma.rest.AbstractEndpointTest;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.UUID.randomUUID;
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

    private JacksonTester<List<TypedAttribute>> jsonTester;

    @Autowired
    protected MockMvc mvc;

    @MockBean
    private ProfileService mockProfileService;

    private final UUID colleagueUuid = randomUUID();

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        JacksonTester.initFields(this, objectMapper);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getProfileByColleagueUuidShouldReturnProfileBy() throws Exception {

        when(mockProfileService.findProfileByColleagueUuid(colleagueUuid))
                .thenReturn(Optional.of(randomProfileResponse()));

        mvc.perform(get("/colleagues/{colleagueUuid}", colleagueUuid)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));

    }

    @Test
    void updateProfileAttributesShouldReturnUpdatedProfileAttributes() throws Exception {

        List<TypedAttribute> profileAttributes = profileAttributes(3);

        // given
        when(mockProfileService.updateProfileAttributes(colleagueUuid, profileAttributes))
                .thenReturn(profileAttributes);

        // when
        ResultActions resultActions = mvc.perform(put("/colleagues/{colleagueUuid}/attributes", colleagueUuid)
                        .contentType(APPLICATION_JSON)
                        .content(jsonTester.write(profileAttributes).getJson())
                        .accept(APPLICATION_JSON));

        // then
        andExpect(resultActions, status().isOk(), profileAttributes);

    }

    @Test
    void createProfileAttributesShouldReturnInsertedProfileAttributes() throws Exception {

        List<TypedAttribute> profileAttributes = profileAttributes(3);

        // given
        when(mockProfileService.createProfileAttributes(colleagueUuid, profileAttributes))
                .thenReturn(profileAttributes);

        // when
        ResultActions resultActions = mvc.perform(post("/colleagues/{colleagueUuid}/attributes", colleagueUuid)
                .contentType(APPLICATION_JSON)
                .content(jsonTester.write(profileAttributes).getJson())
                .accept(APPLICATION_JSON));

        // then
        andExpect(resultActions, status().isCreated(), profileAttributes);

    }

    @Test
    void deleteProfileAttributesShouldReturnDeletedProfileAttributes() throws Exception {

        List<TypedAttribute> profileAttributes = profileAttributes(3);

        // given
        when(mockProfileService.deleteProfileAttributes(colleagueUuid, profileAttributes))
                .thenReturn(profileAttributes);

        // when
        ResultActions resultActions = mvc.perform(delete("/colleagues/{colleagueUuid}/attributes", colleagueUuid)
                .contentType(APPLICATION_JSON)
                .content(jsonTester.write(profileAttributes).getJson())
                .accept(APPLICATION_JSON));

        // then
        andExpect(resultActions, status().isOk(), profileAttributes);

    }

    private AggregatedColleague randomProfileResponse() {
        return RANDOM.nextObject(AggregatedColleague.class);
    }

    private List<TypedAttribute> profileAttributes(int size) {
        return IntStream.rangeClosed(1, size)
                .mapToObj(value ->  profileAttribute())
                .collect(Collectors.toList());
    }

    private TypedAttribute profileAttribute() {
        return randomProfileAttribute();
    }

    private TypedAttribute randomProfileAttribute() {
        return RANDOM.nextObject(TypedAttribute.class);
    }

    private void andExpect(ResultActions resultActions,
                           ResultMatcher status,
                           List<TypedAttribute> profileAttributes) throws Exception {

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