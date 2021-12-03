package com.tesco.pma.tip.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.rest.AbstractEndpointTest;
import com.tesco.pma.tip.api.Tip;
import com.tesco.pma.tip.service.TipService;
import com.tesco.pma.tip.util.TestDataUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TipEndpoint.class, properties = {
        "tesco.application.security.enabled=false",
})
class TipEndpointTest extends AbstractEndpointTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String TIPS_UUID_URL_TEMPLATE = "/tips/{uuid}";

    @Autowired
    protected MockMvc mvc;

    @MockBean
    private TipService service;

    @Test
    void create() throws Exception {
        //given
        Tip tip = TestDataUtil.buildTip();
        when(service.create(tip)).thenReturn(tip);

        //when
        mvc.perform(post("/tips")
                .contentType(APPLICATION_JSON)
                .content(OBJECT_MAPPER.writeValueAsString(tip)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON));

        //then
        verify(service, times(1)).create(tip);
    }

    @Test
    void read() throws Exception {
        // given
        Tip tip = TestDataUtil.buildTip();
        tip.setUuid(TestDataUtil.TIP_UUID);
        when(service.findOne(TestDataUtil.TIP_UUID)).thenReturn(tip);

        //when & then
        mvc.perform(get(TIPS_UUID_URL_TEMPLATE, TestDataUtil.TIP_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data.uuid").isString());
    }

    @Test
    void readNotFound() throws Exception {
        // given
        Tip tip = TestDataUtil.buildTip();
        tip.setUuid(TestDataUtil.TIP_UUID);
        when(service.findOne(TestDataUtil.TIP_UUID)).thenThrow(NotFoundException.class);

        //when & then
        mvc.perform(get(TIPS_UUID_URL_TEMPLATE, TestDataUtil.TIP_UUID))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void findAll() throws Exception {
        // given
        Tip tip1 = TestDataUtil.buildTip();
        tip1.setUuid(TestDataUtil.TIP_UUID);
        Tip tip2 = TestDataUtil.buildTip();
        tip2.setUuid(TestDataUtil.TIP_UNPUBLISHED_UUID);
        List<Tip> tips = List.of(tip1, tip2);
        when(service.findAll(any(RequestQuery.class))).thenReturn(tips);

        //when & then
        mvc.perform(get("/tips")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data[%s].uuid", 0).isString());
    }

    @Test
    void deleteTip() throws Exception {
        //given
        Tip tip = TestDataUtil.buildTip();
        tip.setUuid(TestDataUtil.TIP_UUID);

        //when
        mvc.perform(delete(TIPS_UUID_URL_TEMPLATE, TestDataUtil.TIP_UUID))
                .andExpect(status().isNoContent());

        //then
        verify(service, times(1)).delete(TestDataUtil.TIP_UUID, false);
    }

    @Test
    void publish() throws Exception {
        //given
        Tip tip = TestDataUtil.buildTip();
        tip.setUuid(TestDataUtil.TIP_UNPUBLISHED_UUID);
        when(service.publish(TestDataUtil.TIP_UNPUBLISHED_UUID)).thenReturn(tip);

        //when
        mvc.perform(patch("/tips/{uuid}/publish", TestDataUtil.TIP_UNPUBLISHED_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));

        //then
        verify(service, times(1)).publish(TestDataUtil.TIP_UNPUBLISHED_UUID);
    }
}