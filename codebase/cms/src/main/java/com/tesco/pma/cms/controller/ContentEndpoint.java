package com.tesco.pma.cms.controller;

import com.tesco.pma.cms.model.Content;
import com.tesco.pma.cms.service.ContentService;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;


import java.util.List;
import java.util.UUID;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/cms")
public class ContentEndpoint {

    private static final String KNOWLEDGE_LIBRARY = "knowledge-library";

    private final ContentService contentService;

    @Operation(summary = "Find Content by key", tags = {"CMS"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Find contents")
    @GetMapping(path = "/content", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isColleague()")
    public RestResponse<List<Content>> findByKey(@RequestParam("key") String key) {
        return RestResponse.success(contentService.findByKey(key));
    }

    @Operation(summary = "Find Content by key in path", tags = {"CMS"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Find contents")
    @GetMapping(path = "/" + KNOWLEDGE_LIBRARY + "/{countryCode}/{iam}/{content}", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isColleague()")
    public RestResponse<List<Content>> findByCountryIamContent(@PathVariable String countryCode,
                                                               @PathVariable String iam,
                                                               @PathVariable String content) {
        return RestResponse.success(contentService.findByKey(createKey(KNOWLEDGE_LIBRARY, countryCode, iam, content)));
    }

    @Operation(summary = "Create a Content", tags = {"CMS"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Create a new Content")
    @PostMapping(path = "/content", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isColleague()")
    public RestResponse<Content> create(@RequestBody Content note) {
        return RestResponse.success(contentService.create(note));
    }

    @Operation(summary = "Delete a Content", tags = {"CMS"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Delete a Content")
    @DeleteMapping(path = "/content/{uuid}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isColleague()")
    public RestResponse<?> delete(@PathVariable UUID uuid) {
        contentService.delete(uuid);
        return RestResponse.success();
    }

    private String createKey(String... elements) {
        var sb = new StringBuilder();
        var divisor = "";

        for (String el : elements) {
            sb.append(divisor);
            divisor = "/";
            sb.append(el);
        }

        return sb.toString();
    }

}
