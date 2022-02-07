package com.tesco.pma.cep.v2.service.rest;

import com.tesco.pma.TestConfig;
import com.tesco.pma.cep.v2.domain.ColleagueChangeEventPayload;
import com.tesco.pma.cep.v2.service.ColleagueChangesService;
import com.tesco.pma.configuration.cep.CEPProperties;
import com.tesco.pma.rest.AbstractEndpointTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.stubbing.answers.AnswersWithDelay;
import org.mockito.internal.stubbing.answers.DoesNothing;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ColleagueChangesEndpoint.class)
@ContextConfiguration(classes = TestConfig.class)
public class ColleagueChangesEndpointTests extends AbstractEndpointTest {

    private static final String POST_EVENT_PATH = "/colleagues/events";

    private static final String REQUEST_CEP_HEADER_FEED_ID = "FeedId";

    private static final String JIT_REQUEST_CEP_FEED_ID = "colleagues-jit-v1";
    private static final String JIT_REQUEST_CEP_JSON = "jit_request_cep_2.json";

    private static final String IMMEDIATE_REQUEST_CEP_FEED_ID = "colleagues-immediate-v1";
    private static final String IMMEDIATE_REQUEST_CEP_JSON = "immediate_request_cep_2.json";

    private static final int MAX_NUMBER_PARALLEL_REQUESTS = 2;

    private static final String TEST_CEP_SUBJECT = "test-cep-subject";

    @MockBean
    private ColleagueChangesService mockColleagueChangesService;

    @MockBean
    private CEPProperties mockCepProperties;

    @BeforeEach
    void waitForFinishAllTasks() throws InterruptedException {
        TimeUnit.SECONDS.sleep(MAX_NUMBER_PARALLEL_REQUESTS);
        when(mockCepProperties.getSubject()).thenReturn(TEST_CEP_SUBJECT);
    }

    @Test
    void processJustInTimeFlowColleagueChangeEventShouldReturnAcceptedHttpStatus() throws Exception {

        callEventRequest(JIT_REQUEST_CEP_FEED_ID, JIT_REQUEST_CEP_JSON, status().isAccepted());

        verify(mockColleagueChangesService, timeout(500))
                .processColleagueChangeEvent(any(String.class), any(ColleagueChangeEventPayload.class));

    }

    @Test
    void processImmediateFlowColleagueChangeEventShouldReturnAcceptedHttpStatus() throws Exception {

        callEventRequest(IMMEDIATE_REQUEST_CEP_FEED_ID, IMMEDIATE_REQUEST_CEP_JSON, status().isAccepted());

        verify(mockColleagueChangesService, timeout(500))
                .processColleagueChangeEvent(any(String.class), any(ColleagueChangeEventPayload.class));

    }


    @Test
    void processColleagueChangeEventSeveralTimesShouldReturnAcceptedHttpStatus() throws Exception {
        for (var i = 0; i < MAX_NUMBER_PARALLEL_REQUESTS; i++) {
            callEventRequest(JIT_REQUEST_CEP_FEED_ID, JIT_REQUEST_CEP_JSON, status().isAccepted());
        }

        verify(mockColleagueChangesService, timeout(500)
                .times(MAX_NUMBER_PARALLEL_REQUESTS))
                .processColleagueChangeEvent(any(String.class), any(ColleagueChangeEventPayload.class));

    }

    @Test
    void processColleagueChangeEventShouldThrowsExceptionIfToManyRequests() throws Exception {

        // given
        doAnswer(new AnswersWithDelay(500, DoesNothing.doesNothing()))
                .when(mockColleagueChangesService)
                .processColleagueChangeEvent(any(String.class), any(ColleagueChangeEventPayload.class));

        // when
        for (var i = 0; i < MAX_NUMBER_PARALLEL_REQUESTS; i++) {
            callEventRequest(JIT_REQUEST_CEP_FEED_ID, JIT_REQUEST_CEP_JSON, status().isAccepted());
        }

        callEventRequest(JIT_REQUEST_CEP_FEED_ID, JIT_REQUEST_CEP_JSON, status().isTooManyRequests());

        // then
        verify(mockColleagueChangesService, timeout(1000).times(MAX_NUMBER_PARALLEL_REQUESTS))
                .processColleagueChangeEvent(any(String.class), any(ColleagueChangeEventPayload.class));
    }

    @Test
    void processColleagueChangeEventsWithInvalidPayload() throws Exception {

        mvc.perform(post(POST_EVENT_PATH)
                        .with(jwtWithSubject(TEST_CEP_SUBJECT))
                        .header(REQUEST_CEP_HEADER_FEED_ID, JIT_REQUEST_CEP_FEED_ID)
                        .content("{\"colleagueUUID\":\"77778888-9999-0000-1111-222222222222\"}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isAccepted());

        mvc.perform(post(POST_EVENT_PATH)
                        .with(jwtWithSubject(TEST_CEP_SUBJECT))
                        .header(REQUEST_CEP_HEADER_FEED_ID, JIT_REQUEST_CEP_FEED_ID)
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isAccepted());

        verify(mockColleagueChangesService, timeout(500).times(0))
                .processColleagueChangeEvent(any(String.class), any(ColleagueChangeEventPayload.class));
    }

    @Test
    void processColleagueChangeEventsUnauthorized() throws Exception {
        mvc.perform(post(POST_EVENT_PATH).with(anonymous())
                        .content(json.from(JIT_REQUEST_CEP_JSON).getJson())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(mockColleagueChangesService);
    }

    @Test
    void processColleagueChangeEventForbiddenWithSubjectNotMatch() throws Exception {
        mvc.perform(post(POST_EVENT_PATH).with(jwtWithSubject("not-cep-subject"))
                        .header(REQUEST_CEP_HEADER_FEED_ID, JIT_REQUEST_CEP_FEED_ID)
                        .content(json.from(JIT_REQUEST_CEP_JSON).getJson())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());

        verifyNoInteractions(mockColleagueChangesService);
    }

    private void callEventRequest(String feedId, String jsonSource, ResultMatcher resultMatcher) throws Exception {
        mvc.perform(post(POST_EVENT_PATH)
                        .with(jwtWithSubject(TEST_CEP_SUBJECT))
                        .header(REQUEST_CEP_HEADER_FEED_ID, feedId)
                        .content(json.from(jsonSource).getJson())
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(resultMatcher);
    }

}