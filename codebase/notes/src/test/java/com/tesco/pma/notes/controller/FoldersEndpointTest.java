package com.tesco.pma.notes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesco.pma.TestConfig;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.notes.model.Folder;
import com.tesco.pma.notes.service.NotesServiceImpl;
import com.tesco.pma.rest.AbstractEndpointTest;
import com.tesco.pma.rest.HttpStatusCodes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.any;
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

@WebMvcTest(controllers = FoldersEndpoint.class)
@ContextConfiguration(classes = TestConfig.class)
public class FoldersEndpointTest extends AbstractEndpointTest {

    private final UUID colleagueUuid = UUID.randomUUID();

    private final ObjectMapper objectMapper = new ObjectMapper();

    String ownerColleagueUuidExpression = "$.data.ownerColleagueUuid";

    private static final String URL_TEMPLATE = "/notes/folders/{id}";

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
                        .with(colleague(ownerId.toString()))
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

        mvc.perform(put(URL_TEMPLATE, folder.getId())
                        .with(colleague(ownerId.toString()))
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

        mvc.perform(put(URL_TEMPLATE, folder.getId())
                        .with(colleague(ownerId.toString()))
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

        mvc.perform(get("/notes/folders?ownerId={colleagueUuid}", colleagueUuid).with(colleague(colleagueUuid.toString()))
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));

    }

    @Test
    void deleteSuccessful() throws Exception {

        var ownerId = UUID.randomUUID();
        var folder = createFolder(UUID.randomUUID(), ownerId);

        when(notesService.findFolderByOwner(ownerId)).thenReturn(List.of(folder));

        mvc.perform(delete(URL_TEMPLATE, folder.getId())
                        .with(colleague(ownerId.toString()))
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));

        verify(notesService, times(1))
                .findFolderByOwner(ownerId);

        verify(notesService, times(1))
                .deleteFolder(any(UUID.class));

    }

    @Test
    void deleteUnsuccessfulWithAccessDeniedException() throws Exception {

        var ownerId = UUID.randomUUID();
        var folder = createFolder(UUID.randomUUID(), ownerId);

        when(notesService.findFolderByOwner(ownerId)).thenReturn(List.of(folder));

        mvc.perform(delete(URL_TEMPLATE, UUID.randomUUID())
                        .with(colleague(ownerId.toString()))
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(APPLICATION_JSON));

        verify(notesService, times(1))
                .findFolderByOwner(ownerId);

    }


    private Folder createFolder(UUID id, UUID ownerId){
        var folder = new Folder();
        folder.setId(id);
        folder.setOwnerColleagueUuid(ownerId);
        folder.setTitle("Title");
        return folder;
    }


}
