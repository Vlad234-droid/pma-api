package com.tesco.pma.notes.controller;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.notes.exception.NotesErrorCodes;
import com.tesco.pma.notes.model.Folder;
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
@RequestMapping(path = "/notes/folders")
public class FoldersEndpoint {

    private final NotesService notesService;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Operation(summary = "Create a Folder", tags = {"Notes"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Create a new Folder")
    @PostMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isColleague() and isCurrentUser(#folder.ownerColleagueUuid)")
    public RestResponse<Folder> createFolder(@RequestBody Folder folder) {
        return RestResponse.success(notesService.createFolder(folder));
    }

    @Operation(summary = "Find a Folder by Owner", tags = {"Notes"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Find a folder")
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isColleague() and isCurrentUser(#ownerId)")
    public RestResponse<List<Folder>> get(@RequestParam UUID ownerId) {
        return RestResponse.success(notesService.findFolderByOwner(ownerId));
    }

    @Operation(summary = "Update a Folder", tags = {"Notes"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Update a Folder")
    @PutMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isColleague() and isCurrentUser(#folder.ownerColleagueUuid)")
    public RestResponse<Folder> update(@PathVariable("id") UUID uuid, @RequestBody Folder folder) {
        return RestResponse.success(notesService.updateFolder(folder));
    }

    @Operation(summary = "Delete a Folder", tags = {"Notes"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Delete a Folder")
    @DeleteMapping(value = "/{id}",produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isColleague()")
    public RestResponse<Void> delete(@PathVariable("id") UUID uuid,
                                     @CurrentSecurityContext(expression = "authentication") Authentication authentication) {
        resolveFolderAccess(uuid, authentication);
        notesService.deleteFolder(uuid);
        return RestResponse.success();
    }

    private void resolveFolderAccess(UUID uuid, Authentication authentication) {
        var currentUserUuid = getColleagueUuid(authentication);
        var found = notesService.findFolderByOwner(currentUserUuid).stream()
                .anyMatch(folder -> folder.getId().equals(uuid));
        if (!found) {
            throw new AccessDeniedException(messageSourceAccessor.getMessage(NotesErrorCodes.NOT_A_FOLDER_OWNER,
                    Map.of("uuid", uuid)));
        }
    }

}
