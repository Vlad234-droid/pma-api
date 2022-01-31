package com.tesco.pma.review.service.rest;

import com.tesco.pma.configuration.CaseInsensitiveEnumEditor;
import com.tesco.pma.configuration.audit.AuditorAware;
import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.exception.InvalidParameterException;
import com.tesco.pma.file.api.File;
import com.tesco.pma.file.api.FileType;
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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.tesco.pma.pagination.Condition.Operand.EQUALS;
import static com.tesco.pma.pagination.Condition.Operand.IN;
import static com.tesco.pma.rest.RestResponse.success;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
public class ReviewEndpoint {

    private final ReviewService reviewService;
    private final AuditorAware<UUID> auditorAware;
    private final PMCycleService pmCycleService;

    private static final String CURRENT_PARAMETER_NAME = "CURRENT";
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
        return success(reviewService.createReview(review, getPMCycleUuid(colleagueUuid, cycleUuid), colleagueUuid));
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
                getPMCycleUuid(colleagueUuid, cycleUuid),
                colleagueUuid,
                type,
                reviews));
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
        return success(reviewService.getReview(getPMCycleUuid(colleagueUuid, cycleUuid), colleagueUuid, type, number));
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
        return success(reviewService.getReviews(getPMCycleUuid(colleagueUuid, cycleUuid), colleagueUuid, type));
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
        return success(reviewService.getReviewsByColleague(getPMCycleUuid(colleagueUuid, cycleUuid), colleagueUuid));
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
    @PreAuthorize("isColleague()")
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
        return success(reviewService.updateReview(review, getPMCycleUuid(colleagueUuid, cycleUuid), colleagueUuid));
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
                getPMCycleUuid(colleagueUuid, cycleUuid),
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
        reviewService.deleteReview(
                getPMCycleUuid(colleagueUuid, cycleUuid),
                colleagueUuid,
                type,
                number);
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
                                @CurrentSecurityContext(expression = "authentication") Authentication authentication) {

        var currentUserUuid = UUID.fromString(authentication.getName());
        var path = String.format(REVIEWS_FILES_PATH, colleagueUuid);
        var types = Stream.of(FileType.FileTypeEnum.PDF, FileType.FileTypeEnum.DOC, FileType.FileTypeEnum.PPT)
                .map(FileType.FileTypeEnum::getId)
                .collect(toList());

        var requestQuery = new RequestQuery();
        requestQuery.setFilters(Arrays.asList(
                new Condition("path", EQUALS, path),
                new Condition("type", IN, types)));
        return RestResponse.success(reviewService.getReviewsFilesByColleague(colleagueUuid, currentUserUuid, requestQuery));
    }

    private UUID resolveUserUuid() {
        return auditorAware.getCurrentAuditor();
    }

    private UUID getPMCycleUuid(UUID colleagueUuid, String cycleUuid) {
        if (cycleUuid.equalsIgnoreCase(CURRENT_PARAMETER_NAME)) {
            return pmCycleService.getCurrentByColleague(colleagueUuid).getUuid();
        } else {
            try {
                return UUID.fromString(cycleUuid);
            } catch (IllegalArgumentException e) {
                throw new InvalidParameterException(HttpStatusCodes.BAD_REQUEST, e.getMessage(), "cycleUuid"); // NOPMD
            }
        }
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(PMReviewType.class, new CaseInsensitiveEnumEditor(PMReviewType.class));
        binder.registerCustomEditor(PMTimelinePointStatus.class, new CaseInsensitiveEnumEditor(PMTimelinePointStatus.class));
    }
}
