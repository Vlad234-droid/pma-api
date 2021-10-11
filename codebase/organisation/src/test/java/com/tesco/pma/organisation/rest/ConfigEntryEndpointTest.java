package com.tesco.pma.organisation.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesco.pma.organisation.api.ConfigEntry;
import com.tesco.pma.organisation.api.ConfigEntryResponse;
import com.tesco.pma.api.GeneralDictionaryItem;
import com.tesco.pma.organisation.service.ConfigEntryService;
import com.tesco.pma.rest.AbstractEndpointTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ConfigEntryEndpoint.class, properties = {
        "tesco.application.security.enabled=false",
})
class ConfigEntryEndpointTest extends AbstractEndpointTest {

    private static final UUID ENTRY_UUID = UUID.fromString("fe33d24d-1fd2-4e68-8dff-6220609a80df");
    private static final String COMPOSITE_KEY = "BU/Test/#v1";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String GET_STRUCTURE_JSON = "get_structure.json";
    private static final String GET_ARRAY_STRUCTURE_JSON = "get_array_structure.json";

    @Autowired
    protected MockMvc mvc;

    @MockBean
    private ConfigEntryService service;

    @Test
    void getEntryConfigStructure() throws Exception {

        when(service.getStructure(ENTRY_UUID)).thenReturn(getConfigEntryResponse());

        var result = mvc.perform(get("/config-entries/{entryUuid}", ENTRY_UUID)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn()
                .getResponse();

        assertResponseContent(result, GET_STRUCTURE_JSON);
    }

    @Test
    void getPublishedEntryConfigStructureByCompositeKey() throws Exception {

        when(service.getPublishedChildStructureByCompositeKey(COMPOSITE_KEY)).thenReturn(List.of(getConfigEntryResponse()));

        var result = mvc.perform(get("/config-entries/published").param("compositeKey", COMPOSITE_KEY)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn()
                .getResponse();

        assertResponseContent(result, GET_ARRAY_STRUCTURE_JSON);
    }

    @Test
    void getEntryConfigStructureByCompositeKey() throws Exception {

        when(service.getUnpublishedChildStructureByCompositeKey(COMPOSITE_KEY)).thenReturn(List.of(getConfigEntryResponse()));

        var result = mvc.perform(get("/config-entries").param("compositeKey", COMPOSITE_KEY)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn()
                .getResponse();

        assertResponseContent(result, GET_ARRAY_STRUCTURE_JSON);
    }

    @Test
    void getUnpublishedRoots() throws Exception {

        when(service.getUnpublishedRoots()).thenReturn(List.of(getConfigEntryResponse()));

        var result = mvc.perform(get("/config-entries/roots")
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn()
                .getResponse();

        assertResponseContent(result, GET_ARRAY_STRUCTURE_JSON);
    }

    @Test
    void create() throws Exception {

        mvc.perform(post("/config-entries")
                .contentType(APPLICATION_JSON)
                .content(OBJECT_MAPPER.writeValueAsString(getConfigEntry())))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn()
                .getResponse();

        verify(service).createConfigEntry(getConfigEntry());
    }

    @Test
    void updateConfigEntry() throws Exception {

        mvc.perform(put("/config-entries/{entryUuid}", ENTRY_UUID)
                .contentType(APPLICATION_JSON)
                .content(OBJECT_MAPPER.writeValueAsString(getConfigEntry())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn()
                .getResponse();

        verify(service).updateConfigEntry(getConfigEntry());
    }

    @Test
    void deleteConfigEntry() throws Exception {

        mvc.perform(delete("/config-entries/{entryUuid}", ENTRY_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn()
                .getResponse();

        verify(service).deleteConfigEntry(ENTRY_UUID);
    }

    @Test
    void publishEntryConfigStructure() throws Exception {

        mvc.perform(post("/config-entries/{entryUuid}/publish", ENTRY_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn()
                .getResponse();

        verify(service).publishConfigEntry(ENTRY_UUID);
    }

    @Test
    void unpublishEntryConfigStructure() throws Exception {

        mvc.perform(delete("/config-entries/{entryUuid}/publish", ENTRY_UUID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andReturn()
                .getResponse();

        verify(service).unpublishConfigEntry(ENTRY_UUID);
    }

    private ConfigEntry getConfigEntry() {
        var cet = getConfigEntryType();

        var ce = new ConfigEntry();
        ce.setUuid(ENTRY_UUID);
        ce.setName("C22");
        ce.setType(cet);
        ce.setVersion(4);
        return ce;
    }

    private ConfigEntryResponse getConfigEntryResponse() {
        var cet = getConfigEntryType();

        var ce = new ConfigEntryResponse();
        ce.setUuid(ENTRY_UUID);
        ce.setName("C22");
        ce.setType(cet);
        ce.setVersion(4);
        ce.setRoot(true);
        ce.setCompositeKey(COMPOSITE_KEY);
        ce.setChildren(List.of(new ConfigEntryResponse()));
        return ce;
    }

    private GeneralDictionaryItem getConfigEntryType() {
        var cet = new GeneralDictionaryItem();
        cet.setId(1);
        cet.setCode("code");
        cet.setDescription("desc");
        return cet;
    }
}