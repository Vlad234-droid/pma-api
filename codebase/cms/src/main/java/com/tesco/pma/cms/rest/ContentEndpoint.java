package com.tesco.pma.cms.rest;

import com.tesco.pma.cms.api.ContentEntry;
import com.tesco.pma.cms.service.ContentEntryService;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;


import java.util.List;
import java.util.UUID;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/cms")
public class ContentEndpoint {

    private final ContentEntryService contentEntryService;

    @Operation(summary = "Find Content by key in path", tags = {"CMS"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Find contents")
    @GetMapping(path = "/content-entries", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isColleague()")
    public RestResponse<List<ContentEntry>> findByRequestQuery(RequestQuery rq) {
        return RestResponse.success(contentEntryService.find(rq));
    }

    @Operation(summary = "Create a Content", tags = {"CMS"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Create a new Content")
    @PostMapping(path = "/content-entries", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isColleague()")
    public RestResponse<ContentEntry> create(@RequestBody ContentEntry contentEntry) {
        return RestResponse.success(contentEntryService.create(contentEntry));
    }

    @Operation(summary = "Update a Content", tags = {"CMS"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Update a Content")
    @PutMapping(path = "/content-entries", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isColleague()")
    public RestResponse<ContentEntry> update(@RequestBody ContentEntry contentEntry) {
        return RestResponse.success(contentEntryService.update(contentEntry));
    }

    @Operation(summary = "Delete a Content", tags = {"CMS"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Delete a Content")
    @DeleteMapping(path = "/content-entries/{uuid}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isColleague()")
    public RestResponse<?> delete(@PathVariable UUID uuid) {
        contentEntryService.delete(uuid);
        return RestResponse.success();
    }

}
