package com.tesco.pma.config.controller;

import com.tesco.pma.config.domain.ImportError;
import com.tesco.pma.config.domain.ImportReport;
import com.tesco.pma.config.domain.ImportRequest;
import com.tesco.pma.config.exception.ImportException;
import com.tesco.pma.config.service.ImportColleagueService;
import com.tesco.pma.exception.InvalidPayloadException;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.tesco.pma.rest.HttpStatusCodes.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "/colleagues", produces = APPLICATION_JSON_VALUE)
@Validated
@RequiredArgsConstructor
public class ImportColleagueEndpoint {

    private final ImportColleagueService importService;

    @Operation(
            summary = "Start import colleagues process",
            tags = "profile",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            encoding =
                            @Encoding(name = "file", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE))
            ),
            responses = @ApiResponse(responseCode = CREATED, description = "Colleagues was imported")
    )
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAdmin()")
    public RestResponse<ImportReport> importColleagues(@RequestPart("file") MultipartFile file) {

        if (file.isEmpty()) {
            throw new InvalidPayloadException("INVALID_PAYLOAD", "File cannot be empty", "content");
        }

        try (var inputStream = file.getInputStream()) {
            return RestResponse.success(importService.importColleagues(inputStream, file.getOriginalFilename()));
        } catch (IOException e) {
            throw new ImportException("Failed to import colleagues", e);
        }
    }

    @Operation(summary = "Get import request by uuid", tags = "profile")
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Request found")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Request not found")
    @GetMapping(path = "/requests/{requestUuid}")
    @PreAuthorize("isAdmin()")
    public RestResponse<ImportRequest> getImportRequest(@PathVariable UUID requestUuid) {
        return RestResponse.success(importService.getRequest(requestUuid));
    }

    @Operation(summary = "Get import request errors by request uuid", tags = "profile")
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Request errors found")
    @GetMapping(path = "/requests/{requestUuid}/errors")
    @PreAuthorize("isAdmin()")
    public RestResponse<List<ImportError>> getImportRequestErrors(@PathVariable UUID requestUuid) {
        return RestResponse.success(importService.getRequestErrors(requestUuid));
    }


}
