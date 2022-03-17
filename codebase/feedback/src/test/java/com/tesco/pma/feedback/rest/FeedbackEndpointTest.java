package com.tesco.pma.feedback.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesco.pma.TestConfig;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.feedback.service.FeedbackService;
import com.tesco.pma.feedback.util.TestDataUtil;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.rest.AbstractEndpointTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static com.tesco.pma.feedback.util.TestDataUtil.COLLEAGUE_UUID;
import static com.tesco.pma.feedback.util.TestDataUtil.FEEDBACKS_COUNT;
import static com.tesco.pma.feedback.util.TestDataUtil.FEEDBACK_UUID_LAST;
import static com.tesco.pma.feedback.util.TestDataUtil.FEEDBACK_UUID_UNREAD;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FeedbackEndpoint.class)
@ContextConfiguration(classes = TestConfig.class)
class FeedbackEndpointTest extends AbstractEndpointTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String FEEDBACKS_UUID_URL = "/feedbacks/{uuid}";

    @Autowired
    protected MockMvc mvc;

    @MockBean
    private FeedbackService service;

    @Test
    void createFeedbacks() throws Exception {
        //given
        var feedback = TestDataUtil.buildFeedback();
        when(service.create(feedback)).thenReturn(feedback);

        //when
        mvc.perform(post("/feedbacks")
                        .with(colleague(COLLEAGUE_UUID.toString()))
                        .contentType(APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(Collections.singletonList(feedback))))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON));

        //then
        verify(service, times(1)).create(feedback);
    }

    @Test
    void unsuccessCreateFeedbacksIfValidationFailed() throws Exception {
        //given
        var feedback = TestDataUtil.buildFeedback();
        when(service.create(feedback)).thenReturn(feedback);
        var invalidFeedback = TestDataUtil.buildFeedback();
        invalidFeedback.setColleagueUuid(randomUUID());
        invalidFeedback.setTargetColleagueUuid(randomUUID());

        //when
        mvc.perform(post("/feedbacks")
                        .with(colleague(COLLEAGUE_UUID.toString()))
                        .contentType(APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(List.of(invalidFeedback, feedback))))
                .andExpect(status().isBadRequest());

        //then
        verifyNoInteractions(service);
    }

    @Test
    void updateFeedback() throws Exception {
        //given
        var feedback = TestDataUtil.buildFeedback();
        feedback.setUuid(FEEDBACK_UUID_LAST);
        when(service.update(feedback)).thenReturn(feedback);

        //when
        mvc.perform(put(FEEDBACKS_UUID_URL, FEEDBACK_UUID_LAST)
                        .with(colleague(COLLEAGUE_UUID.toString()))
                        .contentType(APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(feedback)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));

        //then
        verify(service, times(1)).update(feedback);
    }

    @Test
    void markAsRead() throws Exception {
        //given
        var feedback = TestDataUtil.buildFeedback();
        feedback.setUuid(FEEDBACK_UUID_UNREAD);

        //when
        mvc.perform(put("/feedbacks/{uuid}/read", FEEDBACK_UUID_UNREAD)
                        .with(colleague(COLLEAGUE_UUID.toString()))
                        .contentType(APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(feedback)))
                .andExpect(status().isNoContent());

        //then
        verify(service).markAsRead(FEEDBACK_UUID_UNREAD, COLLEAGUE_UUID);
    }

    @Test
    void getAllFeedbacks() throws Exception { //NOSONAR used MockMvc checks
        // given
        var feedback1 = TestDataUtil.buildFeedback();
        feedback1.setUuid(FEEDBACK_UUID_LAST);
        var feedback2 = TestDataUtil.buildFeedback();
        feedback2.setUuid(FEEDBACK_UUID_UNREAD);
        var feedbacks = List.of(feedback1, feedback2);
        when(service.findAll(any(RequestQuery.class), eq(COLLEAGUE_UUID))).thenReturn(feedbacks);

        //when & then
        mvc.perform(get("/feedbacks")
                        .with(colleague(COLLEAGUE_UUID.toString()))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data[%s].uuid", 0).isString());
    }

    @Test
    void getFeedback() throws Exception { //NOSONAR used MockMvc checks
        // given
        var feedback = TestDataUtil.buildFeedback();
        feedback.setUuid(FEEDBACK_UUID_LAST);
        when(service.findOne(FEEDBACK_UUID_LAST)).thenReturn(feedback);

        //when & then
        mvc.perform(get(FEEDBACKS_UUID_URL, FEEDBACK_UUID_LAST)
                        .with(colleague(COLLEAGUE_UUID.toString()))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data.uuid").isString());
    }

    @Test
    void getFeedbackNotFound() throws Exception { //NOSONAR used MockMvc checks
        // given
        var feedback = TestDataUtil.buildFeedback();
        feedback.setUuid(FEEDBACK_UUID_LAST);
        when(service.findOne(FEEDBACK_UUID_LAST)).thenThrow(NotFoundException.class);

        //when & then
        mvc.perform(get(FEEDBACKS_UUID_URL, FEEDBACK_UUID_LAST)
                        .with(allRoles(COLLEAGUE_UUID.toString()))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void unsuccessGetFeedbackIfPostValidationFailed  () throws Exception { //NOSONAR used MockMvc checks
        // given
        var feedback = TestDataUtil.buildFeedback();
        feedback.setUuid(FEEDBACK_UUID_LAST);
        feedback.setColleagueUuid(randomUUID());
        feedback.setTargetColleagueUuid(randomUUID());
        when(service.findOne(FEEDBACK_UUID_LAST)).thenReturn(feedback);

        //when
        mvc.perform(get(FEEDBACKS_UUID_URL, FEEDBACK_UUID_LAST)
                        .with(colleague(COLLEAGUE_UUID.toString()))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // then
        verify(service, times(1)).findOne(FEEDBACK_UUID_LAST);
    }

    @Test
    void getGivenFeedbacksCount() throws Exception { //NOSONAR used MockMvc checks
        // given
        when(service.getGivenFeedbackCount(COLLEAGUE_UUID)).thenReturn(FEEDBACKS_COUNT);

        //when & then
        mvc.perform(get("/feedbacks/given-count")
                        .with(colleague(COLLEAGUE_UUID.toString()))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data").value(FEEDBACKS_COUNT));
    }

    @Test
    void cannotGetGivenFeedbacksCountIfUnauthorized() throws Exception {
        //when & then
        mvc.perform(get("/feedbacks/given-count")
                        .with(anonymous())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    @Test
    void getRequestedFeedbacksCount() throws Exception { //NOSONAR used MockMvc checks
        // given
        when(service.getRequestedFeedbackCount(COLLEAGUE_UUID)).thenReturn(FEEDBACKS_COUNT);

        //when & then
        mvc.perform(get("/feedbacks/requested-count")
                        .with(colleague(COLLEAGUE_UUID.toString()))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data").value(FEEDBACKS_COUNT));
    }

    @Test
    void cannotGetRequestedFeedbacksCountIfUnauthorized() throws Exception {
        //when & then
        mvc.perform(get("/feedbacks/requested-count")
                        .with(anonymous())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

}