package com.tesco.pma.notes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesco.pma.TestConfig;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.notes.model.Note;
import com.tesco.pma.notes.model.NoteStatus;
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
import java.util.UUID;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = NotesEndpoint.class)
@ContextConfiguration(classes = TestConfig.class)
public class NotesEndpointTest extends AbstractEndpointTest {

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
        var note = createNote(UUID.randomUUID(), UUID.randomUUID(), ownerId);

        when(notesService.createNote(note)).thenReturn(note);

        mvc.perform(post("/notes")
                        .with(colleague(ownerId.toString()))
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(note)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath(ownerColleagueUuidExpression).value(is(note.getOwnerColleagueUuid().toString())));

    }

    @Test
    void update() throws Exception {

        var ownerId = UUID.randomUUID();
        var note = createNote(UUID.randomUUID(), UUID.randomUUID(), ownerId);

        when(notesService.updateNote(note)).thenReturn(note);

        mvc.perform(put("/notes/{id}", note.getId())
                        .with(colleague(ownerId.toString()))
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(note)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath(ownerColleagueUuidExpression).value(is(note.getOwnerColleagueUuid().toString())));

    }

    @Test
    void updateNotFound() throws Exception {

        var ownerId = UUID.randomUUID();
        var note = createNote(UUID.randomUUID(), UUID.randomUUID(), ownerId);

        when(notesService.updateNote(note)).thenThrow(new NotFoundException(HttpStatusCodes.NOT_FOUND, "Not found"));

        mvc.perform(put("/notes/{id}", note.getId())
                        .with(colleague(ownerId.toString()))
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(note)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(APPLICATION_JSON));

    }

    @Test
    void findByColleagueUUID() throws Exception {

        when(notesService.findNoteByOwner(colleagueUuid))
                .thenReturn(new ArrayList<>());

        mvc.perform(get("/notes?ownerId={colleagueUuid}", colleagueUuid)
                        .with(colleague())
                        .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));

    }

    private Note createNote(UUID id, UUID folderId, UUID ownerId){
        var note = new Note();
        note.setId(id);
        note.setFolderUuid(folderId);
        note.setOwnerColleagueUuid(ownerId);
        note.setTitle("Title");
        note.setContent("Content");
        note.setStatus(NoteStatus.CREATED);
        note.setUpdateTime(null);
        return note;
    }


}
