package com.tesco.pma.notes.controller;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.notes.exception.NotesErrorCodes;
import com.tesco.pma.notes.model.Note;
import com.tesco.pma.notes.service.NotesService;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
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
import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.util.SecurityUtils.getColleagueUuid;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/notes")
public class NotesEndpoint {

    private final NotesService notesService;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Operation(summary = "Create a Note", tags = {"Notes"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Create a new Note")
    @PostMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isColleague() and isCurrentUser(#note.ownerColleagueUuid)")
    public RestResponse<Note> createNote(@RequestBody Note note) {
        return RestResponse.success(notesService.createNote(note));
    }

    @Operation(summary = "Update a Note", tags = {"Notes"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Update a Note")
    @PutMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isColleague() and isCurrentUser(#note.ownerColleagueUuid)")
    public RestResponse<Note> update(@PathVariable("id") UUID uuid, @RequestBody Note note) {
        return RestResponse.success(notesService.updateNote(note));
    }

    @Operation(summary = "Find Notes by Owner", tags = {"Notes"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Find Note")
    @GetMapping(produces = APPLICATION_JSON_VALUE, params = "ownerId")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isColleague() and isCurrentUser(#ownerId)")
    public RestResponse<List<Note>> findByOwner(@RequestParam UUID ownerId) {
        return RestResponse.success(notesService.findNoteByOwner(ownerId));
    }

    @Operation(summary = "Find Notes by Folder", tags = {"Notes"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Find Note")
    @GetMapping(produces = APPLICATION_JSON_VALUE, params = "folderId")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isColleague()")
    public RestResponse<List<Note>> findByFolder(@RequestParam UUID folderId,
                                                 @CurrentSecurityContext(expression = "authentication") Authentication authentication) {
        return RestResponse.success(notesService.findNoteByFolder(folderId));
    }

    @Operation(summary = "Delete a Note", tags = {"Notes"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Delete a Note")
    @DeleteMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isColleague()")
    public RestResponse<Void> delete(@PathVariable("id") UUID uuid,
                                     @CurrentSecurityContext(expression = "authentication") Authentication authentication) {
        resolveNoteAccess(uuid, authentication);
        notesService.deleteNote(uuid);
        return RestResponse.success();
    }

    private void resolveNoteAccess(UUID uuid, Authentication authentication) {
        var currentUserUuid = getColleagueUuid(authentication);
        var found = notesService.findNoteByOwner(currentUserUuid).stream()
                .anyMatch(note -> note.getId().equals(uuid));
        if (!found) {
            throw new AccessDeniedException(messageSourceAccessor.getMessage(NotesErrorCodes.NOT_A_NOTE_OWNER,
                    Map.of("uuid", uuid)));
        }
    }

}
