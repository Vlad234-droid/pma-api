package com.tesco.pma.notes.controller;

import com.tesco.pma.notes.model.Folder;
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
@RequestMapping(path = "/notes/folders")
public class FoldersEndpoint {

    private final NoteService noteService;

    @Operation(summary = "Create a Folder", tags = {"Notes"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Create a new Folder")
    @PostMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse<?> createFolder(@RequestBody Folder folder){
        return RestResponse.success(noteService.createFolder(folder));
    }

    @Operation(summary = "Find a folder", tags = {"Notes"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Find a folder")
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public RestResponse<List<Folder>> get(@RequestParam UUID ownerId){
        return RestResponse.success(noteService.findFolderByOwner(ownerId));
    }

    @Operation(summary = "Update a Folder", tags = {"Notes"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Update a Folder")
    @PutMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public RestResponse<?> update(@RequestBody Folder folder){
        return RestResponse.success(noteService.updateFolder(folder));
    }

    @Operation(summary = "Delete a Note", tags = {"Notes"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Delete a Folder")
    @DeleteMapping(value = "/{id}",produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public RestResponse<?> delete(@PathVariable("id") UUID uuid){
        noteService.deleteFolder(uuid);
        return RestResponse.success();
    }

}
