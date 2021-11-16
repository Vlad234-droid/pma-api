package com.tesco.pma.review.service.rest;

import com.tesco.pma.cycle.api.PMReviewStatus;
import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.configuration.CaseInsensitiveEnumEditor;
import com.tesco.pma.configuration.audit.AuditorAware;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.exception.InvalidParameterException;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import com.tesco.pma.review.domain.ColleagueReviews;
import com.tesco.pma.review.domain.GroupObjective;
import com.tesco.pma.review.domain.PMCycleTimelinePoint;
import com.tesco.pma.review.domain.Review;
import com.tesco.pma.review.domain.WorkingGroupObjective;
import com.tesco.pma.review.domain.request.UpdateReviewsStatusRequest;
import com.tesco.pma.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

import java.util.List;
import java.util.UUID;

import static com.tesco.pma.rest.RestResponse.success;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
public class ReviewEndpoint {

    private final ReviewService reviewService;
    private final AuditorAware<String> auditorAware;
    private final PMCycleService pmCycleService;

    private static final String CURRENT_PARAMETER_NAME = "CURRENT";

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
    public RestResponse<Review> createReview(@PathVariable("colleagueUuid") UUID colleagueUuid,
                                             @PathVariable("cycleUuid") String cycleUuid,
                                             @PathVariable("type") PMReviewType type,
                                             @PathVariable("number") Integer number,
                                             @RequestBody Review review) {
        review.setPerformanceCycleUuid(getPMCycleUuid(colleagueUuid, cycleUuid));
        review.setColleagueUuid(colleagueUuid);
        review.setType(type);
        review.setNumber(number);
        return success(reviewService.createReview(review));
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
    public RestResponse<Review> getReview(@PathVariable("colleagueUuid") UUID colleagueUuid,
                                          @PathVariable("cycleUuid") String cycleUuid,
                                          @PathVariable("type") PMReviewType type,
                                          @PathVariable("number") Integer number) {
        return success(reviewService.getReview(getPMCycleUuid(colleagueUuid, cycleUuid), colleagueUuid, type, number));
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
    public RestResponse<List<Review>> getReviews(@PathVariable("colleagueUuid") UUID colleagueUuid,
                                                 @PathVariable("cycleUuid") String cycleUuid,
                                                 @PathVariable("type") PMReviewType type) {
        return success(reviewService.getReviews(getPMCycleUuid(colleagueUuid, cycleUuid), colleagueUuid, type));
    }

    /**
     * Get call using a Path param and return a list of colleagues reviews as JSON.
     *
     * @param managerUuid an identifier of colleague
     * @return a RestResponse parameterized with list of colleagues reviews
     */
    @Operation(summary = "Get a list of colleagues reviews by managerUuid", tags = {"review"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found reviews")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Reviews not found", content = @Content)
    @GetMapping(path = "/managers/{managerUuid}/reviews",
            produces = APPLICATION_JSON_VALUE)
    public RestResponse<List<ColleagueReviews>> getTeamReviews(@PathVariable("managerUuid") UUID managerUuid) {
        return success(reviewService.getTeamReviews(managerUuid));
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
    public RestResponse<Review> updateReview(@PathVariable("colleagueUuid") UUID colleagueUuid,
                                             @PathVariable("cycleUuid") String cycleUuid,
                                             @PathVariable("type") PMReviewType type,
                                             @PathVariable("number") Integer number,
                                             @RequestBody Review review) {
        review.setPerformanceCycleUuid(getPMCycleUuid(colleagueUuid, cycleUuid));
        review.setColleagueUuid(colleagueUuid);
        review.setType(type);
        review.setNumber(number);
        return success(reviewService.updateReview(review));
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
    public RestResponse<PMReviewStatus> updateReviewsStatus(@PathVariable("colleagueUuid") UUID colleagueUuid,
                                                            @PathVariable("cycleUuid") String cycleUuid,
                                                            @PathVariable("type") PMReviewType type,
                                                            @PathVariable("status") PMReviewStatus status,
                                                            @RequestBody UpdateReviewsStatusRequest request) {
        return success(reviewService.updateReviewsStatus(
                getPMCycleUuid(colleagueUuid, cycleUuid),
                colleagueUuid,
                type,
                request.getReviews(),
                status,
                request.getReason(),
                resolveUserName()
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
    public RestResponse<List<PMCycleTimelinePoint>> getTimelineByColleague(@PathVariable UUID colleagueUuid) {
        return RestResponse.success(reviewService.getCycleTimelineByColleague(colleagueUuid));
    }

    /**
     * POST call to create group's objectives.
     *
     * @param businessUnitUuid business unit an identifier
     * @param groupObjectives  group's objectives
     * @return a RestResponse parameterized with group's objectives
     */
    @Operation(summary = "Create new group's objectives", description = "Group's objectives created", tags = {"objective"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Successful operation")
    @PostMapping(path = "/business-units/{businessUnitUuid}/objectives",
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse<List<GroupObjective>> createGroupObjectives(@PathVariable("businessUnitUuid") UUID businessUnitUuid,
                                                                    @RequestBody List<GroupObjective> groupObjectives) {
        return RestResponse.success(reviewService.createGroupObjectives(businessUnitUuid, groupObjectives));
    }

    /**
     * Get call using a Path param and return a list of Group Objectives as JSON.
     *
     * @param businessUnitUuid business unit an identifier
     * @return a RestResponse parameterized with list of Group Objectives
     */
    @Operation(summary = "Get all group's objectives by business unit", tags = {"objective"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found Group Objectives")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Group Objectives not found", content = @Content)
    @GetMapping(path = "/business-units/{businessUnitUuid}/objectives",
            produces = APPLICATION_JSON_VALUE)
    public RestResponse<List<GroupObjective>> getGroupObjectives(@PathVariable("businessUnitUuid") UUID businessUnitUuid) {
        return success(reviewService.getAllGroupObjectives(businessUnitUuid));
    }

    /**
     * Get call using a Path param and return a list of published Group Objectives as JSON.
     *
     * @param businessUnitUuid business unit an identifier
     * @return a RestResponse parameterized with list of published Group Objectives
     */
    @Operation(summary = "Get published group's objectives by business unit", tags = {"objective"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found Group Objectives")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Group Objectives not found", content = @Content)
    @GetMapping(path = "/business-units/{businessUnitUuid}/objectives/published",
            produces = APPLICATION_JSON_VALUE)
    public RestResponse<List<GroupObjective>> getPublishedGroupObjectives(@PathVariable("businessUnitUuid") UUID businessUnitUuid) {
        return success(reviewService.getPublishedGroupObjectives(businessUnitUuid));
    }

    @Operation(summary = "Publish group's objectives", tags = {"objective"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Group's objectives have been published")
    @PostMapping(value = "/business-units/{businessUnitUuid}/objectives/publish", produces = APPLICATION_JSON_VALUE)
    public RestResponse<WorkingGroupObjective> publishBusinessUnitStructure(@PathVariable("businessUnitUuid") UUID businessUnitUuid) {
        return success(reviewService.publishGroupObjectives(businessUnitUuid, resolveUserName()));
    }

    @Operation(summary = "Unpublish group's objectives", tags = {"objective"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Group's objectives have been unpublished")
    @DeleteMapping(value = "/business-units/{businessUnitUuid}/objectives/publish", produces = APPLICATION_JSON_VALUE)
    public RestResponse<?> unpublishBusinessUnitStructure(@PathVariable("businessUnitUuid") UUID businessUnitUuid) {
        reviewService.unpublishGroupObjectives(businessUnitUuid);
        return RestResponse.success();
    }

    private String resolveUserName() {
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
        binder.registerCustomEditor(PMReviewStatus.class, new CaseInsensitiveEnumEditor(PMReviewStatus.class));
    }
}
