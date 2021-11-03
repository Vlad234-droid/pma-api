package com.tesco.pma.organisation.rest;


import com.tesco.pma.organisation.service.ConfigEntryService;
import com.tesco.pma.organisation.service.SearchColleaguesService;
import com.tesco.pma.rest.AbstractEndpointTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ColleaguesEndpoint.class, properties = {
        "tesco.application.security.enabled=false",
})
public class ColleaguesEndpointTest extends AbstractEndpointTest {


    @Autowired
    protected MockMvc mvc;

    @MockBean
    private SearchColleaguesService searchColleaguesService;

    @Test
    void getSuggestionsFullNameTest() throws Exception {

        when(searchColleaguesService.getSuggestions(anyString(), Mockito.any())).thenReturn(new ArrayList<>());

        mvc.perform(get("/colleagues/suggestions?fullName={fullNameVal}", "John")
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));
    }

    @Test
    void getSuggestionsAmongSubordinatesFullNameTest() throws Exception {

        var managerId = UUID.randomUUID();
        var fullName = "John Dow";

        when(searchColleaguesService.getSuggestions(anyString(), Mockito.eq(managerId))).thenReturn(new ArrayList<>());


        mvc.perform(get("/colleagues/suggestions?fullName={fullNameVal}&managerId={uuid}", fullName, managerId.toString())
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));

        Mockito.verify(searchColleaguesService, Mockito.times(1))
                .getSuggestions(Mockito.eq(fullName), Mockito.eq(managerId));
    }

}
