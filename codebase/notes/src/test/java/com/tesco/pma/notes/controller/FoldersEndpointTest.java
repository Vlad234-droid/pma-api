package com.tesco.pma.notes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.notes.model.Folder;
import com.tesco.pma.notes.service.NotesServiceImpl;
import com.tesco.pma.rest.AbstractEndpointTest;
import com.tesco.pma.rest.HttpStatusCodes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.ArrayList;
import java.util.UUID;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FoldersEndpoint.class)
public class FoldersEndpointTest extends AbstractEndpointTest {

    private final UUID colleagueUuid = UUID.randomUUID();

    private final ObjectMapper objectMapper = new ObjectMapper();

    String ownerColleagueUuidExpression = "$.data.ownerColleagueUuid";

    @Autowired
    protected MockMvc mvc;

    @MockBean
    private NotesServiceImpl notesService;

    @Test
    void create() throws Exception {

        var ownerId = UUID.randomUUID();
        var folder = createFolder(UUID.randomUUID(), ownerId);

        when(notesService.createFolder(folder)).thenReturn(folder);

        mvc.perform(post("/notes/folders")
                        .with(lineManager(ownerId.toString()))
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(folder)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath(ownerColleagueUuidExpression).value(is(folder.getOwnerColleagueUuid().toString())));

    }

    @Test
    void update() throws Exception {

        var ownerId = UUID.randomUUID();
        var folder = createFolder(UUID.randomUUID(), ownerId);

        when(notesService.updateFolder(folder)).thenReturn(folder);

        mvc.perform(put("/notes/folders/{id}", folder.getId())
                        .with(lineManager(ownerId.toString()))
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(folder)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath(ownerColleagueUuidExpression).value(is(folder.getOwnerColleagueUuid().toString())));

    }

    @Test
    void updateNotFound() throws Exception {

        var ownerId = UUID.randomUUID();
        var folder = createFolder(UUID.randomUUID(), ownerId);

        when(notesService.updateFolder(folder)).thenThrow(new NotFoundException(HttpStatusCodes.NOT_FOUND, "Not found"));

        mvc.perform(put("/notes/folders/{id}", folder.getId())
                        .with(lineManager(ownerId.toString()))
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(folder)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON));

    }

    @Test
    void findByColleagueUUID() throws Exception {

        when(notesService.findFolderByOwner(colleagueUuid))
                .thenReturn(new ArrayList<>());

        mvc.perform(get("/notes/folders?ownerId={colleagueUuid}", colleagueUuid).with(colleague())
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));

    }

    private Folder createFolder(UUID id, UUID ownerId){
        var folder = new Folder();
        folder.setId(id);
        folder.setOwnerColleagueUuid(ownerId);
        folder.setTitle("Title");
        return folder;
    }


}
