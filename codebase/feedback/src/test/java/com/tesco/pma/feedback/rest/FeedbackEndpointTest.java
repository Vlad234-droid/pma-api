package com.tesco.pma.feedback.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FeedbackEndpoint.class, properties = {
        "tesco.application.security.enabled=false",
})
class FeedbackEndpointTest extends AbstractEndpointTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    protected MockMvc mvc;

    @MockBean
    private FeedbackService service;

    @Test
    void createFeedback() throws Exception {
        //given
        Feedback feedback = TestDataUtil.buildFeedback();
        when(service.create(feedback)).thenReturn(feedback);

        //when
        mvc.perform(post("/feedbacks")
                .contentType(APPLICATION_JSON)
                .content(OBJECT_MAPPER.writeValueAsString(feedback)))
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
                .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false));
    }

}