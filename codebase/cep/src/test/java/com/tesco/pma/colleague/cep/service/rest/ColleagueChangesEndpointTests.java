package com.tesco.pma.colleague.cep.service.rest;

import com.tesco.pma.colleague.cep.domain.ColleagueChangeEventPayload;
import com.tesco.pma.colleague.cep.service.ColleagueChangesService;
import com.tesco.pma.rest.AbstractEndpointTest;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.internal.stubbing.answers.AnswersWithDelay;
import org.mockito.internal.stubbing.answers.DoesNothing;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = ColleagueChangesEndpoint.class, properties = {
        "tesco.application.security.enabled=true",
})
public class ColleagueChangesEndpointTests extends AbstractEndpointTest {

    private static final String POST_EVENT_PATH = "/colleagues/cep/events";
    private static final String IMMEDIATE_REQUEST_CEP_SUCCESS_JSON = "immediate_request_cep_success.json";
    private static final int MAX_NUMBER_PARALLEL_REQUESTS = 2;
    private static final String TEST_CEP_SUBJECT = "test-cep-subject";

    private static final EasyRandom RANDOM = new EasyRandom();

    private final ColleagueChangeEventPayload colleagueChangeEventPayload =
            RANDOM.nextObject(ColleagueChangeEventPayload.class);

    @MockBean
    private ColleagueChangesService mockColleagueChangesService;

    @BeforeEach
    void waitForFinishAllTasks() throws InterruptedException {
        TimeUnit.SECONDS.sleep(MAX_NUMBER_PARALLEL_REQUESTS);
    }

    @Test
    void processColleagueChangeEventShouldReturnAcceptedHttpStatus() throws Exception {

        // given

        // when
        ResultActions resultActions = mvc.perform(post("/colleagues/cep/events")
                .with(jwtWithSubject(TEST_CEP_SUBJECT))
                .contentType(APPLICATION_JSON)
                .content(json.from(IMMEDIATE_REQUEST_CEP_SUCCESS_JSON).getJson())
                .accept(APPLICATION_JSON));

        // then
        resultActions
                .andExpect(status().isAccepted());

    }

    @Test
    @Disabled // TODO
    void processColleagueChangeEventShouldThrowsExceptionIfToManyRequests() throws Exception {

        // given
        doAnswer(new AnswersWithDelay(500, DoesNothing.doesNothing()))
                .when(mockColleagueChangesService).processColleagueChangeEvent(colleagueChangeEventPayload);

        // when
        for (var i = 0; i < MAX_NUMBER_PARALLEL_REQUESTS; i++) {
            callEventRequest(status().isAccepted());
        }

        callEventRequest(status().isTooManyRequests());

        // then
        verify(mockColleagueChangesService, timeout(1000).times(MAX_NUMBER_PARALLEL_REQUESTS))
                .processColleagueChangeEvent(colleagueChangeEventPayload);
    }

    @Test
    void processColleagueChangeEventsWithInvalidPayload() throws Exception {

        mvc.perform(post(POST_EVENT_PATH)
                        .with(jwtWithSubject(TEST_CEP_SUBJECT))
                        .content("{\"payload\":{}}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isAccepted());

        mvc.perform(post(POST_EVENT_PATH)
                        .with(jwtWithSubject(TEST_CEP_SUBJECT))
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isAccepted());

        verify(mockColleagueChangesService, timeout(500).times(0))
                .processColleagueChangeEvent(colleagueChangeEventPayload);
    }

    @Test
    void processColleagueChangeEventsUnauthorized() throws Exception {
        mvc.perform(post(POST_EVENT_PATH).with(anonymous())
                        .content(json.from(IMMEDIATE_REQUEST_CEP_SUCCESS_JSON).getJson())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(mockColleagueChangesService);
    }

    @Test
    void processColleagueChangeEventLocalShouldReturnAcceptedHttpStatus() throws Exception {

        // given

        // when
        ResultActions resultActions = mvc.perform(post("/colleagues/cep/events/local")
                .contentType(APPLICATION_JSON)
                .content(json.from(IMMEDIATE_REQUEST_CEP_SUCCESS_JSON).getJson())
                .accept(APPLICATION_JSON));

        // then
        resultActions
                .andExpect(status().isCreated());

    }

    private void callEventRequest(ResultMatcher resultMatcher) throws Exception {
        mvc.perform(post(POST_EVENT_PATH)
                        .with(jwtWithSubject(TEST_CEP_SUBJECT))
                        .content(json.from(IMMEDIATE_REQUEST_CEP_SUCCESS_JSON).getJson())
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(resultMatcher);
    }

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtWithSubject(String s) {
        return jwt().jwt(builder -> builder.subject(s));
    }

}