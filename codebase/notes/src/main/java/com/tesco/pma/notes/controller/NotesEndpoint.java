package com.tesco.pma.notes.controller;

import com.tesco.pma.notes.model.Note;
import com.tesco.pma.notes.service.NotesService;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;
import java.util.UUID;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/notes")
public class NotesEndpoint {

    private final NotesService notesService;

    @Operation(summary = "Create a Note", tags = {"Notes"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Create a new Note")
    @PostMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isCurrentUser(#note.ownerColleagueUuid)")
    public RestResponse<Note> createNote(@RequestBody Note note) {
        return RestResponse.success(notesService.createNote(note));
    }

    @Operation(summary = "Update a Note", tags = {"Notes"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Update a Note")
    @PutMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isCurrentUser(#note.ownerColleagueUuid)")
    public RestResponse<Note> update(@PathVariable("id") UUID uuid, @RequestBody Note note) {
        return RestResponse.success(notesService.updateNote(note));
    }

    @Operation(summary = "Find Notes", tags = {"Notes"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Find Note")
    @GetMapping(produces = APPLICATION_JSON_VALUE, params = "ownerId")
    @ResponseStatus(HttpStatus.OK)
    public RestResponse<List<Note>> findByOwner(@RequestParam UUID ownerId) {
        return RestResponse.success(notesService.findNoteByOwner(ownerId));
    }

    @Operation(summary = "Find Notes by Folder", tags = {"Notes"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Find Note")
    @GetMapping(produces = APPLICATION_JSON_VALUE, params = "folderId")
    @ResponseStatus(HttpStatus.OK)
    public RestResponse<List<Note>> findByFolder(@RequestParam UUID folderId) {
        return RestResponse.success(notesService.findNoteByFolder(folderId));
    }

    @Operation(summary = "Delete a Note", tags = {"Notes"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Delete a Note")
    @DeleteMapping(value = "/{id}",produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public RestResponse<?> delete(@PathVariable("id") UUID uuid) {
        notesService.deleteNote(uuid);
        return RestResponse.success();
    }

}
