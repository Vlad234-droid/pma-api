package com.tesco.pma.notes.controller;

import com.tesco.pma.notes.model.Folder;
import com.tesco.pma.notes.model.Note;
import com.tesco.pma.notes.service.NoteService;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/note")
public class NotesController {

    private final NoteService noteService;

    @Operation(summary = "Create a Note", tags = {"Notes"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Create a new Note")
    @PostMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse<?> createNote(@RequestBody Note note){
        noteService.createNote(note);
        return RestResponse.success();
    }

    @Operation(summary = "Update a Note", tags = {"Notes"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Update a Note")
    @PutMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public RestResponse<?> update(@RequestBody Note note){
        noteService.updateNote(note);
        return RestResponse.success();
    }

    @Operation(summary = "Find a Note", tags = {"Notes"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Find Note")
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public RestResponse<List<Note>> get(@RequestParam UUID ownerId){
        return RestResponse.success(noteService.findNoteByOwnerColleagueUuid(ownerId));
    }

    @Operation(summary = "Delete a Note", tags = {"Notes"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Delete a Note")
    @DeleteMapping(value = "/{id}",produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public RestResponse<?> delete(@PathVariable("id") UUID uuid){
        noteService.deleteNote(uuid);
        return RestResponse.success();
    }

}
