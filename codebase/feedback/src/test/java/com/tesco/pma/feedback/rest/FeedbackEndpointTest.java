package com.tesco.pma.feedback.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesco.pma.TestConfig;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.feedback.api.Feedback;
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

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FeedbackEndpoint.class)
@ContextConfiguration(classes = TestConfig.class)
class FeedbackEndpointTest extends AbstractEndpointTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    protected MockMvc mvc;

    @MockBean
    private FeedbackService service;

    @Test
    void createFeedbacks() throws Exception {
        //given
        Feedback feedback = TestDataUtil.buildFeedback();
        when(service.create(feedback)).thenReturn(feedback);

        //when
        mvc.perform(post("/feedbacks")
                        .with(colleague(TestDataUtil.COLLEAGUE_UUID.toString()))
                        .contentType(APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(Collections.singletonList(feedback))))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON));

        //then
        verify(service, times(1)).create(feedback);
    }

    @Test
    void updateFeedback() throws Exception {
        //given
        Feedback feedback = TestDataUtil.buildFeedback();
        feedback.setUuid(TestDataUtil.FEEDBACK_UUID_LAST);
        when(service.update(feedback)).thenReturn(feedback);

        //when
        mvc.perform(put("/feedbacks/{uuid}", TestDataUtil.FEEDBACK_UUID_LAST)
                        .with(colleague(TestDataUtil.COLLEAGUE_UUID.toString()))
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
        Feedback feedback = TestDataUtil.buildFeedback();
        feedback.setUuid(TestDataUtil.FEEDBACK_UUID_UNREAD);

        //when
        mvc.perform(put("/feedbacks/{uuid}/read", TestDataUtil.FEEDBACK_UUID_UNREAD)
                        .with(colleague(TestDataUtil.COLLEAGUE_UUID.toString()))
                        .contentType(APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(feedback)))
                .andExpect(status().isNoContent());

        //then
        verify(service).markAsRead(TestDataUtil.FEEDBACK_UUID_UNREAD);
    }

    @Test
    void getAllFeedbacks() throws Exception {
        // given
        Feedback feedback1 = TestDataUtil.buildFeedback();
        feedback1.setUuid(TestDataUtil.FEEDBACK_UUID_LAST);
        Feedback feedback2 = TestDataUtil.buildFeedback();
        feedback2.setUuid(TestDataUtil.FEEDBACK_UUID_UNREAD);
        List<Feedback> feedbacks = List.of(feedback1, feedback2);
        when(service.findAll(any(RequestQuery.class))).thenReturn(feedbacks);

        //when & then
        mvc.perform(get("/feedbacks")
                        .with(colleague(TestDataUtil.COLLEAGUE_UUID.toString()))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data[%s].uuid", 0).isString());
    }

    @Test
    void getFeedback() throws Exception {
        // given
        Feedback feedback = TestDataUtil.buildFeedback();
        feedback.setUuid(TestDataUtil.FEEDBACK_UUID_LAST);
        when(service.findOne(TestDataUtil.FEEDBACK_UUID_LAST)).thenReturn(feedback);

        //when & then
        mvc.perform(get("/feedbacks/{uuid}", TestDataUtil.FEEDBACK_UUID_LAST)
                        .with(colleague())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data.uuid").isString());
    }

    @Test
    void getFeedbackNotFound() throws Exception {
        // given
        Feedback feedback = TestDataUtil.buildFeedback();
        feedback.setUuid(TestDataUtil.FEEDBACK_UUID_LAST);
        when(service.findOne(TestDataUtil.FEEDBACK_UUID_LAST)).thenThrow(NotFoundException.class);

        //when & then
        mvc.perform(get("/feedbacks/{uuid}", TestDataUtil.FEEDBACK_UUID_LAST)
                        .with(allRoles(TestDataUtil.COLLEAGUE_UUID.toString()))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getGivenFeedbacksCount() throws Exception {
        // given
        when(service.findGivenFeedbackCount(TestDataUtil.COLLEAGUE_UUID)).thenReturn(5);

        //when & then
        mvc.perform(get("/feedbacks/given")
                        .with(colleague(TestDataUtil.COLLEAGUE_UUID.toString()))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data").value(5));
    }

    @Test
    void cannotGetGivenFeedbacksCountIfUnauthorized() throws Exception {
        // given
        when(service.findGivenFeedbackCount(TestDataUtil.COLLEAGUE_UUID)).thenReturn(5);

        //when & then
        mvc.perform(get("/feedbacks/given")
                        .with(anonymous())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getRequestedFeedbacksCount() throws Exception {
        // given
        when(service.findRequestedFeedbackCount(TestDataUtil.COLLEAGUE_UUID)).thenReturn(5);

        //when & then
        mvc.perform(get("/feedbacks/requested")
                        .with(colleague(TestDataUtil.COLLEAGUE_UUID.toString()))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data").value(5));
    }

    @Test
    void cannotGetRequestedFeedbacksCountIfUnauthorized() throws Exception {
        // given
        when(service.findRequestedFeedbackCount(TestDataUtil.COLLEAGUE_UUID)).thenReturn(5);

        //when & then
        mvc.perform(get("/feedbacks/requested")
                        .with(anonymous())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

}