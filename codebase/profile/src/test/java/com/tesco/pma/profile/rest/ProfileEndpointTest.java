package com.tesco.pma.profile.rest;

import com.tesco.pma.api.User;
import com.tesco.pma.profile.rest.model.Profile;
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

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProfileEndpoint.class)
class ProfileEndpointTest extends AbstractEndpointTest {

    private static final EasyRandom RANDOM = new EasyRandom();

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
    void getProfileByColleagueUuid() throws Exception {
        final var user = randomUser();
        final var colleagueUuid = user.getColleagueUuid();

        when(mockProfileService.findProfileByColleagueUuid(colleagueUuid))
                .thenReturn(Optional.of(randomProfile()));

        mvc.perform(get("/profiles/{colleagueUuid}", colleagueUuid)
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));

    }

    private UUID randomUuid() {
        return UUID.randomUUID();
    }

    private User randomUser() {
        return RANDOM.nextObject(User.class);
    }

    private Profile randomProfile() {
        return RANDOM.nextObject(Profile.class);
    }

}