package com.tesco.pma.cms.controller;

import com.tesco.pma.cms.model.Content;
import com.tesco.pma.cms.service.ContentService;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.UUID;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/cms")
public class ContentEndpoint {

    private final ContentService contentService;

    @Operation(summary = "Find Content by key in path", tags = {"CMS"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Find contents")
    @GetMapping(path = "/content", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isColleague()")
    public RestResponse<List<Content>> findByRequestQuery(RequestQuery rq) {
        return RestResponse.success(contentService.findByRequestQuery(rq));
    }

    @Operation(summary = "Create a Content", tags = {"CMS"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Create a new Content")
    @PostMapping(path = "/content", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isColleague()")
    public RestResponse<Content> create(@RequestBody Content content) {
        return RestResponse.success(contentService.create(content));
    }

    @Operation(summary = "Update a Content", tags = {"CMS"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Update a Content")
    @PutMapping(path = "/content", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isColleague()")
    public RestResponse<Content> update(@RequestBody Content content) {
        return RestResponse.success(contentService.update(content));
    }

    @Operation(summary = "Delete a Content", tags = {"CMS"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Delete a Content")
    @DeleteMapping(path = "/content/{uuid}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isColleague()")
    public RestResponse<?> delete(@PathVariable UUID uuid) {
        contentService.delete(uuid);
        return RestResponse.success();
    }

}
