package com.tesco.pma.fs.rest;

import com.tesco.pma.configuration.audit.AuditorAware;
import com.tesco.pma.exception.DataUploadException;
import com.tesco.pma.exception.InvalidPayloadException;
import com.tesco.pma.file.api.File;
import com.tesco.pma.file.api.FilesUploadMetadata;
import com.tesco.pma.fs.service.FileService;
import com.tesco.pma.logging.TraceUtils;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    static final boolean INCLUDE_FILE_CONTENT_DEFAULT = false;

    private final FileService fileService;
    private final AuditorAware<UUID> auditorAware;

    @Operation(
            summary = "Get Files information with the latest version by file name and path",
            description = "Get Files information with the latest version by file name and path",
            tags = "file",
            responses = {
                    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the file data by its path and name"),
                    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "File data not found", content = @Content),
            })
    @GetMapping(path = "/last", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isProcessManager() or isAdmin()")
    public RestResponse<File> get(@RequestParam("path") String path,
                                  @RequestParam("fileName") String fileName) {
        return success(fileService.get(path, fileName, INCLUDE_FILE_CONTENT_DEFAULT, null));
    }

    @Operation(
            summary = "Get File information with the latest version by its uuid",
            description = "Get File information with the latest version by its uuid",
            tags = "file",
            responses = {
                    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the file data by its uuid"),
                    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "File data not found", content = @Content),
            })
    @GetMapping("{fileUuid}")
    @PreAuthorize("isProcessManager() or isAdmin()")
    public RestResponse<File> get(@PathVariable UUID fileUuid) {
        return success(fileService.get(fileUuid, INCLUDE_FILE_CONTENT_DEFAULT, null));
    }

    @Operation(
            summary = "Get Files information with the latest version applying search, filter and sorting",
            description = "Get Files information with the latest version applying search, filter and sorting",
            tags = "file",
            responses = {
                    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found filtered files data"),
                    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Files data not found", content = @Content),
            })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isProcessManager() or isAdmin()")
    public RestResponse<List<File>> get(RequestQuery requestQuery) {
        return success(fileService.get(requestQuery, INCLUDE_FILE_CONTENT_DEFAULT, null, true));
    }

    @Operation(
            summary = "Get all information about File with All Versions by its name and path",
            description = "Get all information about File with All Versions by its name and path",
            tags = "file",
            responses = {
                    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the file data of all versions by path and name"),
                    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "File data not found", content = @Content),
            })
    @GetMapping(path = "/versions", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isProcessManager() or isAdmin()")
    public RestResponse<List<File>> getAllVersions(@RequestParam("path") String path,
                                                   @RequestParam("fileName") String fileName) {
        return success(fileService.getAllVersions(path, fileName, INCLUDE_FILE_CONTENT_DEFAULT, null));
    }

    /**
     * GET call to download file.
     *
     * @return a RestResponse with downloaded file
     * @throws IOException if the resource cannot be resolved
     */
    @Operation(summary = "Download File",
            description = "Download File",
            tags = "file")
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "File downloaded")
    @ApiResponse(responseCode = HttpStatusCodes.BAD_REQUEST, description = "Invalid id supplied", content = @Content)
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "File not found",
            content = @Content(mediaType = APPLICATION_OCTET_STREAM_VALUE))
    @ApiResponse(responseCode = HttpStatusCodes.UNAUTHORIZED, description = "Unauthorized", content = @Content)
    @ApiResponse(responseCode = HttpStatusCodes.FORBIDDEN, description = "Forbidden", content = @Content)
    @ApiResponse(responseCode = HttpStatusCodes.INTERNAL_SERVER_ERROR, description = "Internal Server Error", content = @Content)
    @GetMapping("/{fileUuid}/download")
    @PreAuthorize("isProcessManager() or isAdmin()")
    public ResponseEntity<Resource> download(@PathVariable UUID fileUuid) {
        var file = fileService.get(fileUuid, true, null);
        var content = file.getFileContent();

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(content.length)
                .body(new ByteArrayResource(content));
    }

    @Operation(
            summary = "Upload Files",
            description = "Upload Files",
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
    @PreAuthorize("isProcessManager() or isAdmin()")
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

    /**
     * DELETE call to delete file by its uuid.
     *
     * @param fileUuid file identifier
     * @return a Void RestResponse
     */
    @Operation(summary = "Delete existing File by its uuid", description = "Delete existing file", tags = {"file"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "File deleted")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "File not found", content = @Content)
    @DeleteMapping(path = "/{fileUuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isProcessManager() or isAdmin()")
    public RestResponse<Void> delete(@PathVariable("fileUuid") UUID fileUuid) {
        fileService.delete(fileUuid, null);
        return RestResponse.success();
    }

    /**
     * POST call to delete files by its path, name and versions.
     *
     * @param path      file path
     * @param fileName  file name
     * @param versions  file versions
     * @return a Void RestResponse
     */
    @Operation(summary = "Delete existing Files by its path, name and versions", description = "Delete existing files", tags = {"file"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Files deleted")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Files not found", content = @Content)
    @DeleteMapping(path = "/versions", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isProcessManager() or isAdmin()")
    public RestResponse<Void> delete(@RequestParam("path") String path,
                                     @RequestParam("fileName") String fileName,
                                     @RequestParam(name = "versions", required = false) List<@NotNull Integer> versions) {
        fileService.deleteVersions(path, fileName, versions, null);
        return RestResponse.success();
    }
}
