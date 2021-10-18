package com.tesco.pma.objective.service.rest;

import com.tesco.pma.configuration.audit.AuditorAware;
import com.tesco.pma.objective.domain.GroupObjective;
import com.tesco.pma.objective.domain.Review;
import com.tesco.pma.objective.domain.ReviewProperties;
import com.tesco.pma.objective.domain.ReviewStatus;
import com.tesco.pma.objective.domain.ReviewType;
import com.tesco.pma.objective.domain.WorkingGroupObjective;
import com.tesco.pma.objective.domain.request.ReviewBodyRequest;
import com.tesco.pma.objective.service.ReviewService;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import com.tesco.pma.validation.ValidationGroup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import java.util.List;
import java.util.UUID;

import static com.tesco.pma.rest.RestResponse.success;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@Validated
@RequiredArgsConstructor
public class ReviewEndpoint {

    private final ReviewService reviewService;
    private final AuditorAware<String> auditorAware;

    /**
     * POST call to create a Review.
     *
     * @param performanceCycleUuid an identifier of performance cycle
     * @param colleagueUuid        an identifier of colleague
     * @param number               a sequence number ща review
     * @param type                 a review type
     * @param reviewBodyRequest    a ReviewBodyRequest
     * @return a RestResponse parameterized with Review
     */
    @Operation(summary = "Create a review", description = "Review created", tags = {"review"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "Successful operation")
    @PostMapping(path = "/performance-cycles/{performanceCycleUuid}/colleagues/{colleagueUuid}/types/{type}/numbers/{number}",
            produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
    @Validated({ValidationGroup.WithoutId.class, Default.class})
    @ResponseStatus(HttpStatus.CREATED)
    public RestResponse<Review> createReview(@PathVariable("performanceCycleUuid") UUID performanceCycleUuid,
                                             @PathVariable("colleagueUuid") UUID colleagueUuid,
                                             @PathVariable("type") ReviewType type,
                                             @PathVariable("number") Integer number,
                                             @RequestBody @Valid ReviewBodyRequest reviewBodyRequest) {
        var review = new Review();
        review.setPerformanceCycleUuid(performanceCycleUuid);
        review.setColleagueUuid(colleagueUuid);
        review.setType(type);
        review.setNumber(number);
        review.setGroupObjectiveUuid(reviewBodyRequest.getLinkedReviewUuid());
        review.setProperties(new ReviewProperties(reviewBodyRequest.getReviewProperties()));
        return success(reviewService.createReview(review));
    }

    /**
     * Get call using a Path param and return a review as JSON.
     *
     * @param uuid an identifier
     * @return a RestResponse parameterized with Review
     */
    @Operation(summary = "Get a review by its uuid", tags = {"review"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the Review")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Review not found", content = @Content)
    @GetMapping(path = "/reviews/{uuid}", produces = APPLICATION_JSON_VALUE)
    public RestResponse<Review> getReviewByUuid(@PathVariable("uuid") UUID uuid) {
        return success(reviewService.getReviewByUuid(uuid));
    }

    /**
     * Get call using a Path param and return a review as JSON.
     *
     * @param performanceCycleUuid an identifier of performance cycle
     * @param colleagueUuid        an identifier of colleague
     * @param type                 a review type
     * @param number               a sequence number of review
     * @return a RestResponse parameterized with review
     */
    @Operation(summary = "Get a review by its performanceCycleUuid, colleagueUuid, review type and number", tags = {"review"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the Review")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Review not found", content = @Content)
    @GetMapping(path = "/performance-cycles/{performanceCycleUuid}/colleagues/{colleagueUuid}/types/{type}/numbers/{number}",
            produces = APPLICATION_JSON_VALUE)
    public RestResponse<Review> getReview(@PathVariable("performanceCycleUuid") UUID performanceCycleUuid,
                                          @PathVariable("colleagueUuid") UUID colleagueUuid,
                                          @PathVariable("type") ReviewType type,
                                          @PathVariable("number") Integer number) {
        return success(reviewService.getReview(performanceCycleUuid, colleagueUuid, type, number));
    }

    /**
     * Get call using a Path param and return a list of reviews as JSON.
     *
     * @param performanceCycleUuid an identifier of performance cycle
     * @param colleagueUuid        an identifier of colleague
     * @param type                 a review type
     * @return a RestResponse parameterized with list of reviews
     */
    @Operation(summary = "Get a list of reviews by its performanceCycleUuid, colleagueUuid, review type", tags = {"review"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found reviews")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Reviews not found", content = @Content)
    @GetMapping(path = "/performance-cycles/{performanceCycleUuid}/colleagues/{colleagueUuid}/types/{type}/reviews",
            produces = APPLICATION_JSON_VALUE)
    public RestResponse<List<Review>> getReviews(@PathVariable("performanceCycleUuid") UUID performanceCycleUuid,
                                                 @PathVariable("colleagueUuid") UUID colleagueUuid,
                                                 @PathVariable("type") ReviewType type) {
        return success(reviewService.getReviews(performanceCycleUuid, colleagueUuid, type));
    }

    /**
     * PUT call to update a review.
     *
     * @param performanceCycleUuid an identifier of performance cycle
     * @param colleagueUuid        an identifier of colleague
     * @param type                 a review type
     * @param number               a sequence number of review
     * @param reviewBodyRequest    a ReviewBodyRequest
     * @return a RestResponse parameterized with Review
     */
    @Operation(summary = "Update existing review", description = "Update existing review", tags = {"review"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Review updated")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Review not found", content = @Content)
    @PutMapping(path = "/performance-cycles/{performanceCycleUuid}/colleagues/{colleagueUuid}/numbers/{number}/types/{type}",
            consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Validated({ValidationGroup.WithoutId.class, Default.class})
    public RestResponse<Review> updateReview(@PathVariable("performanceCycleUuid") UUID performanceCycleUuid,
                                             @PathVariable("colleagueUuid") UUID colleagueUuid,
                                             @PathVariable("type") ReviewType type,
                                             @PathVariable("number") Integer number,
                                             @RequestBody @Valid ReviewBodyRequest reviewBodyRequest) {
        var review = new Review();
        review.setPerformanceCycleUuid(performanceCycleUuid);
        review.setColleagueUuid(colleagueUuid);
        review.setType(type);
        review.setNumber(number);
        review.setGroupObjectiveUuid(reviewBodyRequest.getLinkedReviewUuid());
        review.setProperties(new ReviewProperties(reviewBodyRequest.getReviewProperties()));
        return success(reviewService.updateReview(review));
    }

    /**
     * PUT call to update a review status.
     *
     * @param performanceCycleUuid an identifier of performance cycle
     * @param colleagueUuid        an identifier of colleague
     * @param type                 a review type
     * @param number               a sequence number of review
     * @param status               a ObjectiveStatus
     * @param reason               a reason of changing status
     * @return a RestResponse parameterized with ReviewStatus
     */
    @Operation(summary = "Update status of existing review",
            description = "Update status of existing review", tags = {"review"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Review status updated")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Review not found", content = @Content)
    @PutMapping(
            path = "/performance-cycles/{perfCycleUuid}/colleagues/{colleagueUuid}/numbers/{number}/types/{type}/statuses/{status}",
            produces = APPLICATION_JSON_VALUE)
    @Validated({ValidationGroup.WithoutId.class, Default.class})
    public RestResponse<ReviewStatus> updateReviewStatus(@PathVariable("perfCycleUuid") UUID performanceCycleUuid,
                                                         @PathVariable("colleagueUuid") UUID colleagueUuid,
                                                         @PathVariable("type") ReviewType type,
                                                         @PathVariable("number") Integer number,
                                                         @PathVariable("status") ReviewStatus status,
                                                         @RequestBody String reason) {
        return success(reviewService.updateReviewStatus(
                performanceCycleUuid,
                colleagueUuid,
                type,
                number,
                status,
                reason,
                resolveUserName()
        ));
    }

    /**
     * DELETE call to delete a Review.
     *
     * @param uuid an identifier
     * @return a RestResponse with success field of boolean value
     */
    @Operation(summary = "Delete existing review", description = "Delete existing review", tags = {"review"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Review deleted")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Review not found", content = @Content)
    @DeleteMapping(path = "/reviews/{uuid}", produces = APPLICATION_JSON_VALUE)
    public RestResponse<Void> deleteReview(@PathVariable("uuid") @NotNull UUID uuid) {
        reviewService.deleteReview(uuid);
        return success();
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
                                                                    @RequestBody @Valid List<GroupObjective> groupObjectives) {
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
}
