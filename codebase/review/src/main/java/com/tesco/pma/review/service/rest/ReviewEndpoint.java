package com.tesco.pma.review.service.rest;

import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.configuration.CaseInsensitiveEnumEditor;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.configuration.audit.AuditorAware;
import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.exception.DataUploadException;
import com.tesco.pma.exception.InvalidPayloadException;
import com.tesco.pma.file.api.File;
import com.tesco.pma.file.api.FileType.FileTypeEnum;
import com.tesco.pma.file.api.FilesUploadMetadata;
import com.tesco.pma.fs.exception.ErrorCodes;
import com.tesco.pma.fs.service.FileService;
import com.tesco.pma.logging.TraceUtils;
import com.tesco.pma.pagination.Condition;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import com.tesco.pma.review.domain.AuditOrgObjectiveReport;
import com.tesco.pma.review.domain.ColleagueView;
import com.tesco.pma.review.domain.OrgObjective;
import com.tesco.pma.review.domain.Review;
import com.tesco.pma.review.domain.TimelinePoint;
import com.tesco.pma.review.domain.request.UpdateReviewsStatusRequest;
import com.tesco.pma.review.service.ReviewService;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.file.api.FileType.FileTypeEnum.DOC;
import static com.tesco.pma.file.api.FileType.FileTypeEnum.PDF;
import static com.tesco.pma.file.api.FileType.FileTypeEnum.PPT;
import static com.tesco.pma.logging.TraceId.TRACE_ID_HEADER;
import static com.tesco.pma.pagination.Condition.Operand.EQUALS;
import static com.tesco.pma.pagination.Condition.Operand.IN;
import static com.tesco.pma.rest.HttpStatusCodes.CREATED;
import static com.tesco.pma.rest.RestResponse.success;
import static com.tesco.pma.review.exception.ErrorCodes.INSUFFICIENT_FILE_ACCESS;
import static com.tesco.pma.security.UserRoleNames.ADMIN;
import static com.tesco.pma.util.SecurityUtils.getColleagueUuid;
import static com.tesco.pma.util.SecurityUtils.hasAuthority;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

@RestController
@RequiredArgsConstructor
@SuppressWarnings("PMD.ExcessiveImports")
public class ReviewEndpoint {

    private static final List<FileTypeEnum> REVIEW_FILE_TYPES = List.of(PDF, DOC, PPT);
    private final ReviewService reviewService;
    private final AuditorAware<UUID> auditorAware;
    private final FileService fileService;
    private final ProfileService profileService;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    private static final String REVIEWS_FILES_PATH = "/home/%s/reviews";

    /**
     * POST call to create a Review.
     *
     * @param cycleUuid     an identifier of performance cycle
     * @param colleagueUuid an identifier of colleague
     * @param number        a sequence number of review
     * @param type          a review type
     * @param review        a Review
     * @return a RestResponse parameterized with Review
     */
    @Operation(summary = "Create a review", description = "Review created", tags = {"review"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Successful operation")
    @PostMapping(path = "/colleagues/{colleagueUuid}/pm-cycles/{cycleUuid}/review-types/{type}/numbers/{number}",
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isColleague()")
    public RestResponse<Review> createReview(@PathVariable("colleagueUuid") UUID colleagueUuid,
                                             @PathVariable("cycleUuid") String cycleUuid,
                                             @PathVariable("type") PMReviewType type,
                                             @PathVariable("number") Integer number,
                                             @RequestBody Review review) {
        review.setType(type);
        review.setNumber(number);
        return success(reviewService.createReview(review, colleagueUuid));
    }

    /**
     * POST call to update a list of reviews.
     *
     * @param cycleUuid     an identifier of performance cycle
     * @param colleagueUuid an identifier of colleague
     * @param type          a review type
     * @param reviews       a list of Review
     * @return a RestResponse parameterized with Reviews
     */
    @Operation(summary = "Update list of reviews", description = "Update list of reviews", tags = {"review"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Reviews updated")
    @PutMapping(path = "/colleagues/{colleagueUuid}/pm-cycles/{cycleUuid}/review-types/{type}",
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @PreAuthorize("isColleague()")
    public RestResponse<List<Review>> updateReviews(@PathVariable("colleagueUuid") UUID colleagueUuid,
                                                    @PathVariable("cycleUuid") String cycleUuid,
                                                    @PathVariable("type") PMReviewType type,
                                                    @RequestBody List<Review> reviews) {
        return success(reviewService.updateReviews(
                colleagueUuid,
                type,
                reviews,
                resolveUserUuid()));
    }

    /**
     * Get call using a Path param and return a review as JSON.
     *
     * @param cycleUuid     an identifier of performance cycle
     * @param colleagueUuid an identifier of colleague
     * @param type          a review type
     * @param number        a sequence number of review
     * @return a RestResponse parameterized with review
     */
    @Operation(summary = "Get a review by its cycleUuid, colleagueUuid, review type and number", tags = {"review"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the Review")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Review not found", content = @Content)
    @GetMapping(path = "/colleagues/{colleagueUuid}/pm-cycles/{cycleUuid}/review-types/{type}/numbers/{number}",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isColleague()")
    public RestResponse<Review> getReview(@PathVariable("colleagueUuid") UUID colleagueUuid,
                                          @PathVariable("cycleUuid") String cycleUuid,
                                          @PathVariable("type") PMReviewType type,
                                          @PathVariable("number") Integer number) {
        return success(reviewService.getReview(colleagueUuid, type, number));
    }

    /**
     * Get call using a Path param and return a review as JSON.
     *
     * @param uuid an identifier of review
     * @return a RestResponse parameterized with review
     */
    @Operation(summary = "Get a review by its identifier", tags = {"review"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the Review")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Review not found", content = @Content)
    @GetMapping(path = "/reviews/{uuid}",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isColleague()")
    public RestResponse<Review> getReviewByUuid(@PathVariable("uuid") UUID uuid) {
        return success(reviewService.getReview(uuid));
    }

    /**
     * Get call using a Path param and return a list of reviews as JSON.
     *
     * @param cycleUuid     an identifier of performance cycle
     * @param colleagueUuid an identifier of colleague
     * @param type          a review type
     * @return a RestResponse parameterized with list of reviews
     */
    @Operation(summary = "Get a list of reviews by its cycleUuid, colleagueUuid, review type", tags = {"review"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found reviews")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Reviews not found", content = @Content)
    @GetMapping(path = "/colleagues/{colleagueUuid}/pm-cycles/{cycleUuid}/review-types/{type}/reviews",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isColleague()")
    public RestResponse<List<Review>> getReviews(@PathVariable("colleagueUuid") UUID colleagueUuid,
                                                 @PathVariable("cycleUuid") String cycleUuid,
                                                 @PathVariable("type") PMReviewType type) {
        return success(reviewService.getReviews(colleagueUuid, type));
    }

    /**
     * Get call using a Path param and return a list of reviews as JSON.
     *
     * @param cycleUuid     an identifier of performance cycle
     * @param colleagueUuid an identifier of colleague
     * @return a RestResponse parameterized with list of reviews
     */
    @Operation(summary = "Get a list of reviews by its cycleUuid, colleagueUuid", tags = {"review"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found reviews")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Reviews not found", content = @Content)
    @GetMapping(path = "/colleagues/{colleagueUuid}/pm-cycles/{cycleUuid}/reviews",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isColleague()")
    public RestResponse<List<Review>> getReviewsByColleague(@PathVariable("colleagueUuid") UUID colleagueUuid,
                                                            @PathVariable("cycleUuid") String cycleUuid) {
        return success(reviewService.getReviewsByColleague(colleagueUuid));
    }

    /**
     * Get call using a Path param and return a list of colleague's view
     * with active reviews, timeline points etc. as JSON.
     *
     * @param managerUuid an identifier of colleague
     * @return a RestResponse parameterized with list of colleague's view with active reviews, timeline points etc.
     */
    @Operation(summary = "Get a list of colleagues reviews by managerUuid", tags = {"review"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found reviews")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Reviews not found", content = @Content)
    @GetMapping(path = "/managers/{managerUuid}/reviews",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isColleague()")
    public RestResponse<List<ColleagueView>> getTeamView(@PathVariable("managerUuid") UUID managerUuid) {
        return success(reviewService.getTeamView(managerUuid, 1));
    }

    /**
     * Get call using a Path param and return a list of colleague's view
     * with active reviews, timeline points etc. as JSON.
     *
     * @param managerUuid an identifier of colleague
     * @return a RestResponse parameterized with list of colleague's view with active reviews, timeline points etc.
     */
    @Operation(summary = "Get a list of full team reviews by managerUuid", tags = {"review"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found reviews")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Reviews not found", content = @Content)
    @GetMapping(path = "/managers/{managerUuid}/full-team-reviews",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isExecutive()")
    public RestResponse<List<ColleagueView>> getFullTeamView(@PathVariable("managerUuid") UUID managerUuid) {
        return success(reviewService.getTeamView(managerUuid, 2));
    }

    /**
     * PUT call to update a review.
     *
     * @param cycleUuid     an identifier of performance cycle
     * @param colleagueUuid an identifier of colleague
     * @param type          a review type
     * @param number        a sequence number of review
     * @param review        a Review
     * @return a RestResponse parameterized with Review
     */
    @Operation(summary = "Update existing review", description = "Update existing review", tags = {"review"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Review updated")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Review not found", content = @Content)
    @PutMapping(path = "/colleagues/{colleagueUuid}/pm-cycles/{cycleUuid}/review-types/{type}/numbers/{number}",
            consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isColleague()")
    public RestResponse<Review> updateReview(@PathVariable("colleagueUuid") UUID colleagueUuid,
                                             @PathVariable("cycleUuid") String cycleUuid,
                                             @PathVariable("type") PMReviewType type,
                                             @PathVariable("number") Integer number,
                                             @RequestBody Review review) {
        review.setType(type);
        review.setNumber(number);

        return success(reviewService.updateReview(review, colleagueUuid, resolveUserUuid()));
    }

    /**
     * PUT call to update reviews status.
     *
     * @param cycleUuid     an identifier of performance cycle
     * @param colleagueUuid an identifier of colleague
     * @param type          a review type
     * @param status        a ObjectiveStatus
     * @param request       UpdateReviewsStatusRequest
     * @return a RestResponse parameterized with ReviewStatus
     */
    @Operation(summary = "Update status of existing reviews",
            description = "Update status of existing reviews", tags = {"review"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Reviews status updated")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Review not found", content = @Content)
    @PutMapping(
            path = "/colleagues/{colleagueUuid}/pm-cycles/{cycleUuid}/review-types/{type}/statuses/{status}",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isColleague()")
    public RestResponse<PMTimelinePointStatus> updateReviewsStatus(@PathVariable("colleagueUuid") UUID colleagueUuid,
                                                                   @PathVariable("cycleUuid") String cycleUuid,
                                                                   @PathVariable("type") PMReviewType type,
                                                                   @PathVariable("status") PMTimelinePointStatus status,
                                                                   @RequestBody UpdateReviewsStatusRequest request) {
        return success(reviewService.updateReviewsStatus(
                colleagueUuid,
                type,
                request.getReviews(),
                status,
                request.getReason(),
                resolveUserUuid()
        ));
    }

    /**
     * DELETE call to delete a Review.
     *
     * @param cycleUuid     an identifier of performance cycle
     * @param colleagueUuid an identifier of colleague
     * @param type          a review type
     * @param number        a sequence number of review
     * @return a RestResponse with success field of boolean value
     */
    @Operation(summary = "Delete existing review", description = "Delete existing review", tags = {"review"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Review deleted")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Review not found", content = @Content)
    @DeleteMapping(path = "/colleagues/{colleagueUuid}/pm-cycles/{cycleUuid}/review-types/{type}/numbers/{number}",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isColleague()")
    public RestResponse<Void> deleteReview(@PathVariable("colleagueUuid") UUID colleagueUuid,
                                           @PathVariable("cycleUuid") String cycleUuid,
                                           @PathVariable("type") PMReviewType type,
                                           @PathVariable("number") Integer number) {
        reviewService.deleteReview(colleagueUuid, type, number);
        return success();
    }

    @Operation(summary = "Get cycle timeline for colleague", tags = {"review"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the cycle timeline")
    @GetMapping(value = "/colleagues/{colleagueUuid}/timeline", produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isColleague()")
    public RestResponse<List<TimelinePoint>> getTimelineByColleague(@PathVariable UUID colleagueUuid) {
        return RestResponse.success(reviewService.getCycleTimelineByColleague(colleagueUuid));
    }

    /**
     * POST call to create organisation objectives.
     *
     * @param orgObjectives organisation objectives
     * @return a RestResponse parameterized with list of organisation objectives
     */
    @Operation(summary = "Create new organisation objectives",
            description = "Organisation objectives created",
            tags = {"org-objective"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Successful operation")
    @PostMapping(path = "/org-objectives",
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isTalentAdmin()")
    public RestResponse<List<OrgObjective>> createOrgObjectives(@RequestBody List<OrgObjective> orgObjectives) {
        return RestResponse.success(reviewService.createOrgObjectives(orgObjectives, resolveUserUuid()));
    }

    /**
     * Get call to return a list of organisation objectives as JSON.
     *
     * @return a RestResponse parameterized with list of organisation objectives
     */
    @Operation(summary = "Get all organisation objectives", tags = {"org-objective"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found organisation objectives")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Organisation objectives not found", content = @Content)
    @GetMapping(path = "/org-objectives",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("hasColleagueWorkLevel('WL4', 'WL5') or isTalentAdmin()")
    public RestResponse<List<OrgObjective>> getOrgObjectives() {
        return success(reviewService.getAllOrgObjectives());
    }

    /**
     * Get call to return a list of published organisation objectives as JSON.
     *
     * @return a RestResponse parameterized with list of published organisation objectives
     */
    @Operation(summary = "Get published organisation objectives", tags = {"org-objective"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found organisation objectives")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Organisation objectives not found", content = @Content)
    @GetMapping(path = "/org-objectives/published",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("hasColleagueWorkLevel('WL4', 'WL5') or isTalentAdmin()")
    public RestResponse<List<OrgObjective>> getPublishedOrgObjectives() {
        return success(reviewService.getPublishedOrgObjectives());
    }

    @Operation(summary = "Create and publish organisation objectives", tags = {"org-objective"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Organisation objectives have been published")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Organisation objectives not found", content = @Content)
    @PostMapping(value = "/org-objectives/publish", produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isTalentAdmin()")
    public RestResponse<List<OrgObjective>> createAndPublishOrgObjectives(@RequestBody List<OrgObjective> orgObjectives) {
        return success(reviewService.createAndPublishOrgObjectives(orgObjectives, resolveUserUuid()));
    }

    @Operation(summary = "Publish organisation objectives", tags = {"org-objective"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Organisation objectives have been published")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Organisation objectives not found", content = @Content)
    @PutMapping(value = "/org-objectives/publish", produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isTalentAdmin()")
    public RestResponse<List<OrgObjective>> publishOrgObjectives() {
        return success(reviewService.publishOrgObjectives(resolveUserUuid()));
    }

    @Operation(summary = "Get audit log of organisation objective actions", tags = {"org-objective"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found audit log data")
    @GetMapping(value = "/audit-logs", produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isTalentAdmin()")
    public RestResponse<List<AuditOrgObjectiveReport>> getAuditLogReport(@NotNull RequestQuery requestQuery) {
        return success(reviewService.getAuditOrgObjectiveReport(requestQuery));
    }

    /**
     * Get call using a Path param and return a list of reviews files.
     *
     * @param colleagueUuid an identifier of colleague
     * @return a RestResponse parameterized with list of reviews files
     */
    @Operation(summary = "Get a list of reviews by colleagueUuid", tags = {"review"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found reviews")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Reviews not found", content = @Content)
    @GetMapping(path = "/colleagues/{colleagueUuid}/reviews/files", produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isColleague()")
    public RestResponse<List<File>> getReviewsFilesByColleague(@PathVariable UUID colleagueUuid,
                                                               @CurrentSecurityContext(expression = "authentication")
                                                                       Authentication authentication) {
        return RestResponse.success(fileService.get(getRequestQueryForReviewFiles(colleagueUuid), false,
                resolveFileAccess(authentication, colleagueUuid), true));
    }

    /**
     * Delete call using a Path params and delete a review file.
     *
     * @param fileUuid file identifier
     * @return a Void RestResponse
     */
    @Operation(summary = "Delete Review File by its uuid", tags = {"review"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Review File deleted")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Review File not found", content = @Content)
    @DeleteMapping(path = "/reviews/files/{fileUuid}", produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isColleague()")
    public RestResponse<Void> delete(@PathVariable UUID fileUuid,
                                     @CurrentSecurityContext(expression = "authentication") Authentication authentication) {
        fileService.delete(fileUuid, resolveColleagueUuid(authentication));
        return RestResponse.success();
    }

    /**
     * Post call to upload Review Files
     *
     * @param filesUploadMetadata files metadata
     * @param files               files data
     * @return uploaded files
     */
    @Operation(
            summary = "Upload Review Files",
            description = "Upload Review Files",
            tags = "review",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            encoding = {
                                    @Encoding(name = "uploadMetadata", contentType = MediaType.APPLICATION_JSON_VALUE),
                                    @Encoding(name = "files", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
                            })
            ),
            responses = {
                    @ApiResponse(responseCode = CREATED, description = "Uploaded review file")
            }
    )
    @PostMapping(path = "/reviews/files",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isColleague()")
    public RestResponse<List<File>> upload(
            @RequestPart("uploadMetadata")
            @Valid @Parameter(schema = @Schema(type = "string", format = "binary")) FilesUploadMetadata filesUploadMetadata,
            @RequestPart("files") @NotEmpty List<@NotNull MultipartFile> files,
            @CurrentSecurityContext(expression = "authentication") Authentication authentication,
            HttpServletResponse response) {

        var traceId = TraceUtils.toParent();
        response.setHeader(TRACE_ID_HEADER, traceId.getValue());

        var uploadMetadataList = filesUploadMetadata.getUploadMetadataList();
        if (uploadMetadataList.size() != files.size()) {
            throw new DataUploadException(ErrorCodes.FILES_COUNT_MISMATCH.name(),
                    "Count of data review files and metadata files do not match", null);
        }

        var uploadMetadataIterator = uploadMetadataList.iterator();
        var filesIterator = files.iterator();
        var uploadedFiles = new LinkedList<File>();
        while (uploadMetadataIterator.hasNext() && filesIterator.hasNext()) {
            var file = filesIterator.next();

            var fileName = file.getOriginalFilename();
            if (file.isEmpty()) {
                throw new DataUploadException(ErrorCodes.ERROR_FILE_UPLOAD_FAILED.name(),
                        "Failed to store empty review file.", fileName);
            }

            if (StringUtils.isBlank(fileName)) {
                throw new InvalidPayloadException(ErrorCodes.INVALID_PAYLOAD.getCode(),
                        "Review file name cannot be empty", "fileName");
            }

            var fileData = new File();
            fileData.setFileName(fileName);
            fileData.setFileLength((int) file.getSize());

            try {
                fileData.setFileContent(file.getBytes());
                var fileUploadMetadata = uploadMetadataIterator.next();
                var creatorId = getColleagueUuid(authentication);
                fileUploadMetadata.setPath(String.format(REVIEWS_FILES_PATH, creatorId));
                if (REVIEW_FILE_TYPES.stream().noneMatch(t -> t.getCode().equals(fileUploadMetadata.getType().getCode()))) {
                    throw new InvalidPayloadException(ErrorCodes.INVALID_PAYLOAD.getCode(),
                            "Review file type must be one of " + REVIEW_FILE_TYPES, "type");
                }
                uploadedFiles.add(fileService.upload(fileData, fileUploadMetadata, creatorId));
            } catch (IOException e) {
                throw new DataUploadException(ErrorCodes.ERROR_FILE_UPLOAD_FAILED.name(),
                        "Failed to store review file.", fileName, e);
            }

        }
        return success(uploadedFiles);
    }

    /**
     * GET call to download review file.
     *
     * @return a RestResponse with downloaded review file
     */
    @Operation(summary = "Download Review File", description = "Download Review File", tags = {"review"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Review File downloaded")
    @ApiResponse(responseCode = HttpStatusCodes.BAD_REQUEST, description = "Invalid id supplied", content = @Content)
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Review File not found",
            content = @Content(mediaType = APPLICATION_OCTET_STREAM_VALUE))
    @ApiResponse(responseCode = HttpStatusCodes.UNAUTHORIZED, description = "Unauthorized", content = @Content)
    @ApiResponse(responseCode = HttpStatusCodes.FORBIDDEN, description = "Forbidden", content = @Content)
    @ApiResponse(responseCode = HttpStatusCodes.INTERNAL_SERVER_ERROR, description = "Internal Server Error", content = @Content)

    @GetMapping("/colleagues/{colleagueUuid}/reviews/files/{fileUuid}/download")
    @PreAuthorize("isColleague()")
    public ResponseEntity<Resource> download(@PathVariable UUID colleagueUuid,
                                             @PathVariable UUID fileUuid,
                                             @CurrentSecurityContext(expression = "authentication") Authentication authentication) {
        var file = fileService.get(fileUuid, true, resolveFileAccess(authentication, colleagueUuid));
        var content = file.getFileContent();

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(content.length)
                .body(new ByteArrayResource(content));
    }

    private RequestQuery getRequestQueryForReviewFiles(UUID colleagueUuid) {
        var path = String.format(REVIEWS_FILES_PATH, colleagueUuid);
        var types = REVIEW_FILE_TYPES.stream()
                .map(FileTypeEnum::getId)
                .collect(toList());

        var requestQuery = new RequestQuery();
        requestQuery.setFilters(Arrays.asList(
                new Condition("path", EQUALS, path),
                new Condition("type", IN, types)));
        return requestQuery;
    }

    private UUID resolveColleagueUuid(Authentication authentication) {
        return hasAuthority(authentication, ADMIN) ? null : getColleagueUuid(authentication);
    }

    private UUID resolveFileAccess(Authentication authentication, UUID colleagueUuid) {
        var currentUserUuid = getColleagueUuid(authentication);

        if (!colleagueUuid.equals(currentUserUuid)) {
            var profile = profileService.getColleague(colleagueUuid);
            if (!currentUserUuid.equals(profile.getManagerUuid())) {
                throw new AccessDeniedException(messageSourceAccessor.getMessage(INSUFFICIENT_FILE_ACCESS,
                        Map.of("managerUuid", currentUserUuid, "colleagueUuid", colleagueUuid)));
            }
        }

        return colleagueUuid;
    }

    private UUID resolveUserUuid() {
        return auditorAware.getCurrentAuditor();
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(PMReviewType.class, new CaseInsensitiveEnumEditor(PMReviewType.class));
        binder.registerCustomEditor(PMTimelinePointStatus.class, new CaseInsensitiveEnumEditor(PMTimelinePointStatus.class));
    }
}
