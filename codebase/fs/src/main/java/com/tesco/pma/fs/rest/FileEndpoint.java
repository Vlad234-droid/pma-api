package com.tesco.pma.fs.rest;

import com.tesco.pma.configuration.audit.AuditorAware;
import com.tesco.pma.exception.DataUploadException;
import com.tesco.pma.exception.InvalidPayloadException;
import com.tesco.pma.fs.domain.File;
import com.tesco.pma.fs.domain.FilesUploadMetadata;
import com.tesco.pma.fs.service.FileService;
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
import org.springframework.core.io.ByteArrayResource;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static com.tesco.pma.fs.exception.ErrorCodes.ERROR_FILE_UPLOAD_FAILED;
import static com.tesco.pma.fs.exception.ErrorCodes.FILES_COUNT_MISMATCH;
import static com.tesco.pma.fs.exception.ErrorCodes.INVALID_PAYLOAD;
import static com.tesco.pma.logging.TraceId.TRACE_ID_HEADER;
import static com.tesco.pma.rest.HttpStatusCodes.CREATED;

import static com.tesco.pma.rest.RestResponse.success;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

/**
 * File service endpoint.
 */
@RestController
@RequestMapping(path = "/files")
@Validated
@RequiredArgsConstructor
public class FileEndpoint {

    public static final String INCLUDE_FILE_CONTENT = "includeFileContent";

    private final FileService fileService;
    private final AuditorAware<String> auditorAware;

    @Operation(
            summary = "Get File information by its uuid",
            description = "Get File information by its uuid",
            tags = "file",
            responses = {
                    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the file data by its uuid"),
                    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "File data not found", content = @Content),
            })
    @GetMapping("{fileUuid}")
    public RestResponse<File> get(@PathVariable UUID fileUuid,
                                  @RequestParam(value = INCLUDE_FILE_CONTENT, defaultValue = "true") boolean includeFileContent) {
        return success(fileService.get(fileUuid, includeFileContent));
    }

    /**
     * GET call to download file.
     *
     * @return a RestResponse with downloaded file
     * @throws IOException if the resource cannot be resolved
     */
    @Operation(summary = "Download file",
            description = "Download file",
            tags = "file")
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "File downloaded")
    @ApiResponse(responseCode = HttpStatusCodes.BAD_REQUEST, description = "Invalid id supplied", content = @Content)
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "File not found",
            content = @Content(mediaType = APPLICATION_OCTET_STREAM_VALUE))
    @ApiResponse(responseCode = HttpStatusCodes.UNAUTHORIZED, description = "Unauthorized", content = @Content)
    @ApiResponse(responseCode = HttpStatusCodes.FORBIDDEN, description = "Forbidden", content = @Content)
    @ApiResponse(responseCode = HttpStatusCodes.INTERNAL_SERVER_ERROR, description = "Internal Server Error", content = @Content)
    @GetMapping("/download/{fileUuid}")
    public ResponseEntity<Resource> download(@PathVariable UUID fileUuid) {
        var file = fileService.get(fileUuid, true);
        byte[] content = file.getFileContent();

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(content.length)
                .body(new ByteArrayResource(content));
    }

    @Operation(
            summary = "Upload files",
            description = "Upload files",
            tags = "file",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            encoding = {
                                    @Encoding(name = "uploadMetadata", contentType = MediaType.APPLICATION_JSON_VALUE),
                                    @Encoding(name = "files", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
                            })
            ),
            responses = {
                    @ApiResponse(responseCode = CREATED, description = "Uploaded file")
            }
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse<List<File>> upload(
            @RequestPart("uploadMetadata")
            @Valid @Parameter(schema = @Schema(type = "string", format = "binary")) FilesUploadMetadata filesUploadMetadata,
            @RequestPart("files") @NotEmpty List<@NotNull MultipartFile> files,
            HttpServletResponse response) {

        var traceId = TraceUtils.toParent();
        response.setHeader(TRACE_ID_HEADER, traceId.getValue());

        var uploadMetadataList = filesUploadMetadata.getUploadMetadataList();
        if (uploadMetadataList.size() != files.size()) {
            throw new DataUploadException(FILES_COUNT_MISMATCH.name(), "Count of data files and metadata files do not match", null);
        }

        var uploadMetadataIterator = uploadMetadataList.iterator();
        var filesIterator = files.iterator();
        var uploadedFiles = new LinkedList<File>();
        while (uploadMetadataIterator.hasNext() && filesIterator.hasNext()) {
            var file = filesIterator.next();

            var fileName = file.getOriginalFilename();
            if (file.isEmpty()) {
                throw new DataUploadException(ERROR_FILE_UPLOAD_FAILED.name(), "Failed to store empty file.", fileName);
            }

            if (StringUtils.isBlank(fileName)) {
                throw new InvalidPayloadException(INVALID_PAYLOAD.getCode(), "File name cannot be empty", "fileName");
            }

            var fileData = new File();
            fileData.setFileName(fileName);
            fileData.setFileLength((int) file.getSize());

            try {
                fileData.setFileContent(file.getBytes());
                var fileUploadMetadata = uploadMetadataIterator.next();
                uploadedFiles.add(fileService.upload(fileData, fileUploadMetadata, auditorAware.getCurrentAuditor()));
            } catch (IOException e) {
                throw new DataUploadException(ERROR_FILE_UPLOAD_FAILED.name(), "Failed to store file.", fileName, e);
            }

        }
        return success(uploadedFiles);
    }

}
