package com.tesco.pma.fs.rest;

import com.tesco.pma.configuration.audit.AuditorAware;
import com.tesco.pma.exception.DataUploadException;
import com.tesco.pma.exception.InvalidPayloadException;
import com.tesco.pma.fs.domain.ProcessTemplate;
import com.tesco.pma.fs.domain.UploadMetadata;
import com.tesco.pma.fs.service.TemplateService;
import com.tesco.pma.logging.TraceUtils;
import com.tesco.pma.rest.HttpStatusCodes;

import com.tesco.pma.rest.RestResponse;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

import static com.tesco.pma.fs.exception.ErrorCodes.ERROR_FILE_UPLOAD_FAILED;
import static com.tesco.pma.fs.exception.ErrorCodes.INVALID_PAYLOAD;
import static com.tesco.pma.logging.TraceId.TRACE_ID_HEADER;
import static com.tesco.pma.rest.HttpStatusCodes.CREATED;

import static com.tesco.pma.rest.RestResponse.success;
import static java.util.UUID.fromString;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

/**
 * Template service endpoint.
 */
@RestController
@RequestMapping(path = "/templates")
@Validated
@RequiredArgsConstructor
public class TemplateEndpoint {

    private final TemplateService templateService;
    private final AuditorAware<String> auditorAware;

    @Operation(
            summary = "Read Template information by its uuid",
            description = "Read Template information by its uuid",
            tags = "template",
            responses = {
                    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the template data by its uuid"),
                    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Template data not found", content = @Content),
            })
    @GetMapping("{templateUuid}")
    public RestResponse<ProcessTemplate> readTemplate(@PathVariable String templateUuid) {
        return success(templateService.readTemplateByUuid(fromString(templateUuid)));
    }

    /**
     * GET call to download Template file.
     *
     * @return a RestResponse with downloaded Template file
     * @throws IOException if the resource cannot be resolved
     */
    @Operation(summary = "Download Template file",
            description = "Download Template file",
            tags = "template")
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Template downloaded")
    @ApiResponse(responseCode = HttpStatusCodes.BAD_REQUEST, description = "Invalid id supplied", content = @Content)
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Template not found",
            content = @Content(mediaType = APPLICATION_OCTET_STREAM_VALUE))
    @ApiResponse(responseCode = HttpStatusCodes.UNAUTHORIZED, description = "Unauthorized", content = @Content)
    @ApiResponse(responseCode = HttpStatusCodes.FORBIDDEN, description = "Forbidden", content = @Content)
    @ApiResponse(responseCode = HttpStatusCodes.INTERNAL_SERVER_ERROR, description = "Internal Server Error", content = @Content)
    @GetMapping("/last")
    public ResponseEntity<Resource> downloadTemplate() throws IOException {
        var template = templateService.downloadTemplate();
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + template.getFilename() + "\"")
                .contentLength(template.contentLength())
                .contentType(MediaType.APPLICATION_OCTET_STREAM).body(template);
    }

    @Operation(
            summary = "Upload template file",
            description = "Upload template file",
            tags = "template",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            encoding = {
                                    @Encoding(name = "uploadMetadata", contentType = MediaType.APPLICATION_JSON_VALUE),
                                    @Encoding(name = "file", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
                            })
            ),
            responses = {
                    @ApiResponse(responseCode = CREATED, description = "Uploaded Template file")
            }
    )
    @PostMapping(path = "/",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse<ProcessTemplate> uploadTemplate(
            @RequestPart("uploadMetadata")
            @Valid @Parameter(schema = @Schema(type = "string", format = "binary")) UploadMetadata uploadMetadata,
            @RequestPart("file") MultipartFile file,
            HttpServletResponse response) {

        var traceId = TraceUtils.toParent();
        response.setHeader(TRACE_ID_HEADER, traceId.getValue());

        var fileName = file.getOriginalFilename();
        if (file.isEmpty()) {
            throw new DataUploadException(ERROR_FILE_UPLOAD_FAILED.name(), "Failed to store empty Template file.", fileName);
        }

        if (StringUtils.isBlank(fileName)) {
            throw new InvalidPayloadException(INVALID_PAYLOAD.getCode(), "File name cannot be empty", "fileName");
        }

        try (var inputStream = file.getInputStream()) {
            return success(templateService.uploadTemplate(inputStream, uploadMetadata, file, auditorAware.getCurrentAuditor()));
        } catch (IOException e) {
            throw new DataUploadException(ERROR_FILE_UPLOAD_FAILED.name(), "Failed to store Template file.", fileName, e);
        }
    }

}
