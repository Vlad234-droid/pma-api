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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TipEndpoint.class, properties = {
        "tesco.application.security.enabled=false",
})
class TipEndpointTest extends AbstractEndpointTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String TIPS_KEY_URL_TEMPLATE = "/tips/{key}";

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
        tip.setKey(TestDataUtil.TIP_KEY);
        when(service.findOne(TestDataUtil.TIP_KEY)).thenReturn(tip);

        //when & then
        mvc.perform(get(TIPS_KEY_URL_TEMPLATE, TestDataUtil.TIP_KEY))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data.key").isString());
    }

    @Test
    void readNotFound() throws Exception {
        // given
        Tip tip = TestDataUtil.buildTip();
        tip.setKey(TestDataUtil.TIP_KEY);
        when(service.findOne(TestDataUtil.TIP_KEY)).thenThrow(NotFoundException.class);

        //when & then
        mvc.perform(get(TIPS_KEY_URL_TEMPLATE, TestDataUtil.TIP_KEY))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void findAll() throws Exception {
        // given
        Tip tip1 = TestDataUtil.buildTip();
        tip1.setKey(TestDataUtil.TIP_KEY);
        Tip tip2 = TestDataUtil.buildTip();
        tip2.setKey(TestDataUtil.TIP_KEY_UNPUBLISHED);
        List<Tip> tips = List.of(tip1, tip2);
        when(service.findAll(any(RequestQuery.class))).thenReturn(tips);

        //when & then
        mvc.perform(get("/tips")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data[%s].key", 0).isString());
    }

    @Test
    void update() throws Exception {
        //given
        Tip tip = TestDataUtil.buildTip();
        tip.setKey(TestDataUtil.TIP_KEY);
        when(service.update(tip)).thenReturn(tip);

        //when
        mvc.perform(put(TIPS_KEY_URL_TEMPLATE, TestDataUtil.TIP_KEY)
                .contentType(APPLICATION_JSON)
                .content(OBJECT_MAPPER.writeValueAsString(tip)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));

        //then
        verify(service, times(1)).update(tip);
    }

    @Test
    void deleteTip() throws Exception {
        //given
        Tip tip = TestDataUtil.buildTip();
        tip.setKey(TestDataUtil.TIP_KEY);

        //when
        mvc.perform(delete(TIPS_KEY_URL_TEMPLATE, TestDataUtil.TIP_KEY))
                .andExpect(status().isNoContent());

        //then
        verify(service, times(1)).delete(TestDataUtil.TIP_KEY);
    }

    @Test
    void publish() throws Exception {
        //given
        Tip tip = TestDataUtil.buildTip();
        tip.setKey(TestDataUtil.TIP_KEY_UNPUBLISHED);

        //when
        mvc.perform(put("/tips/{key}/publish", TestDataUtil.TIP_KEY_UNPUBLISHED))
                .andExpect(status().isNoContent());

        //then
        verify(service, times(1)).publish(TestDataUtil.TIP_KEY_UNPUBLISHED);
    }
}