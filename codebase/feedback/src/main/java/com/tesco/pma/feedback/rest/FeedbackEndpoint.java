package com.tesco.pma.feedback.rest;

import com.tesco.pma.exception.InvalidParameterException;
import com.tesco.pma.exception.InvalidPayloadException;
import com.tesco.pma.feedback.api.Feedback;
import com.tesco.pma.feedback.service.FeedbackService;
import com.tesco.pma.feedback.validator.FeedbackValidator;
import com.tesco.pma.pagination.Condition;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import com.tesco.pma.validation.ValidationGroup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.groups.Default;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.tesco.pma.pagination.Condition.Operand.EQUALS;
import static com.tesco.pma.util.SecurityUtils.getColleagueUuid;

/**
 * REST controller for managing {@link Feedback}.
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
public class FeedbackEndpoint {

    private final FeedbackService feedbackService;

    /**
     * {@code POST  /feedbacks} : Create a new feedbacks.
     *
     * @param feedbacks the list of feedback to create.
     * @return the {@link RestResponse} with status {@code 201 (Created)} and with body the new feedbacks,
     * or with status {@code 400 (Bad Request)} if the feedback has already an UUID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/feedbacks")
    @ResponseStatus(HttpStatus.CREATED)
    @Validated({ValidationGroup.OnCreate.class, Default.class})
    @Operation(summary = "Create a new list of feedbacks with items", tags = {"feedback"})
    @ApiResponse(responseCode = HttpStatusCodes.CREATED, description = "List of feedbacks created")
    @PreAuthorize("isColleague()")
    public RestResponse<List<Feedback>> createFeedbacks(@RequestBody List<@Valid Feedback> feedbacks,
                                                        @CurrentSecurityContext(expression = "authentication")
                                                                Authentication authentication) throws URISyntaxException {
        log.debug("REST request to save Feedbacks : {}", feedbacks);
        var colleagueUuid = getColleagueUuid(authentication);
        var result = feedbacks.stream()
                .filter(feedback -> FeedbackValidator.validateFeedbackColleague(feedback, colleagueUuid))
                .map(feedbackService::create)
                .collect(Collectors.toList());
        return RestResponse.success(result);
    }

    /**
     * {@code PUT  /feedbacks/:uuid} : Updates an existing feedback.
     *
     * @param uuid     the uuid of the feedback to save.
     * @param feedback the feedback to update.
     * @return the {@link RestResponse} with status {@code 200 (OK)} and with body the updated feedback,
     * or with status {@code 400 (Bad Request)} if the feedback is not valid,
     * or with status {@code 500 (Internal Server Error)} if the feedback couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/feedbacks/{uuid}")
    @Validated({ValidationGroup.OnUpdate.class, Default.class})
    @Operation(summary = "Updates an existing feedback", tags = {"feedback"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Feedback updated")
    @ApiResponse(responseCode = HttpStatusCodes.BAD_REQUEST, description = "Invalid UUID")
    @PreAuthorize("isColleague()")
    public RestResponse<Feedback> updateFeedback(
            @PathVariable(value = "uuid", required = false) final UUID uuid,
            @Valid @RequestBody Feedback feedback,
            @CurrentSecurityContext(expression = "authentication") Authentication authentication
    ) throws URISyntaxException {
        log.debug("REST request to update Feedback : {}, {}", uuid, feedback);
        if (feedback.getUuid() == null) {
            throw new InvalidPayloadException(HttpStatusCodes.BAD_REQUEST, "UUID must not be null", "feedback.uuid");
        }
        if (!Objects.equals(uuid, feedback.getUuid())) {
            throw new InvalidParameterException(HttpStatusCodes.BAD_REQUEST, "Path uuid does not match body uuid", "feedback.uuid");
        }

        FeedbackValidator.validateFeedbackColleague(feedback, getColleagueUuid(authentication));

        return RestResponse.success(feedbackService.update(feedback));
    }

    /**
     * {@code PATCH  /feedbacks/:uuid/read} : Mark an existing feedback as read
     *
     * @param uuid the uuid of the feedback to save.
     * @return the {@link RestResponse} with status {@code 204 (No content)},
     * or with status {@code 404 (Not Found)} if the feedback is not found,
     * or with status {@code 500 (Internal Server Error)} if the feedback couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping(value = "/feedbacks/{uuid}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Mark an existing feedback as read", tags = {"feedback"})
    @ApiResponse(responseCode = HttpStatusCodes.NO_CONTENT, description = "Mark as read successfully")
    @PreAuthorize("isColleague()")
    public RestResponse<Void> markAsRead(
            @PathVariable(value = "uuid") final UUID uuid,
            @CurrentSecurityContext(expression = "authentication") Authentication authentication
    ) throws URISyntaxException {
        log.debug("REST request to mark Feedback as read : {}", uuid);
        feedbackService.markAsRead(uuid, getColleagueUuid(authentication));
        return RestResponse.success();
    }

    /**
     * {@code GET  /feedbacks} : get all the feedbacks.
     *
     * @param requestQuery filter, sort, offset
     * @return the {@link RestResponse} with status {@code 200 (OK)} and the list of feedbacks in body.
     */
    @GetMapping("/feedbacks")
    @Operation(summary = "Get all feedbacks with all items", tags = {"feedback"})
    @PreAuthorize("isColleague()")
    public RestResponse<List<Feedback>> getAllFeedbacks(@Parameter(example = "{\n"
            + "    \"_sort\": \"target-colleague-first-name:DESC,updated-time:ASC\",\n"
            + "    \"read\": \"true\",\n"
            + "    \"colleague-uuid\": \"10000000-0000-0000-0000-000000000001\",\n"
            + "    \"target-colleague-uuid_ne\": \"10000000-0000-0000-0000-000000000002\",\n"
            + "    \"target-id\": \"f48c8772-4661-4373-8151-ea89796bb3e6\",\n"
            + "    \"status_in\": [\"1\",\"2\"],\n"
            + "    \"target-type_nin\": [\"1\",\"2\"],\n"
            + "    \"colleague-first-name_contains\": \"a\",\n"
            + "    \"colleague-last-name_ncontains\": \"Doe\",\n"
            + "    \"target-colleague-middle-name_contains\": \"jef\",\n"
            + "    \"created-time_lt\": \"2021-11-26T14:18:42.615Z\",\n"
            + "    \"created-time_lte\": \"2021-11-26T14:18:42.615Z\",\n"
            + "    \"updated-time_gt\": \"2021-11-25T14:36:33.587Z\",\n"
            + "    \"updated-time_gte\": \"2021-11-25T14:36:33.587Z\",\n"
            + "    \"target-id_null\": \"true\",\n"
            + "    \"_start\": \"1\",\n"
            + "    \"_limit\": \"7\",\n"
            + "    \"_search\": \"first or middle or last target colleague name\"\n"
            + "  }") RequestQuery requestQuery,
                     @CurrentSecurityContext(expression = "authentication") Authentication authentication) {
        log.debug("REST request to get a feedbacks of Feedbacks");
        var filters = requestQuery.getFilters();
        filters.add(new Condition("authenticated-colleague", EQUALS, getColleagueUuid(authentication)));
        requestQuery.setFilters(filters);
        return RestResponse.success(feedbackService.findAll(requestQuery));
    }

    /**
     * {@code GET  /feedbacks/:uuid} : get the "uuid" feedback.
     *
     * @param uuid the uuid of the feedback to retrieve.
     * @return the {@link RestResponse} with status {@code 200 (OK)} and with body the feedbackDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/feedbacks/{uuid}")
    @Operation(summary = "Get feedback by UUID with all items", tags = {"feedback"})
    @PreAuthorize("isColleague()")
    public RestResponse<Feedback> getFeedback(@PathVariable UUID uuid,
                                              @CurrentSecurityContext(expression = "authentication") Authentication authentication) {
        log.debug("REST request to get Feedback : {}", uuid);
        var feedback = feedbackService.findOne(uuid);
        var colleagueUuid = getColleagueUuid(authentication);
        FeedbackValidator.validateFeedbackColleague(feedback, colleagueUuid);
        return RestResponse.success(feedback);
    }

    /**
     * {@code GET  /feedbacks/given-count} : get given feedbacks count.
     *
     * @return the {@link RestResponse} with the given feedbacks count {@code 200 (OK)}.
     */
    @GetMapping("/feedbacks/given-count")
    @Operation(summary = "Get given feedbacks count for colleague", tags = {"feedback"})
    @PreAuthorize("isColleague()")
    public RestResponse<Integer> getGivenFeedbackCount(@CurrentSecurityContext(expression = "authentication")
                                                                   Authentication authentication) {
        var colleagueUuid = getColleagueUuid(authentication);
        log.debug("REST request to get given Feedbacks count for colleague : {}", colleagueUuid);
        return RestResponse.success(feedbackService.getGivenFeedbackCount(colleagueUuid));
    }

    /**
     * {@code GET  /feedbacks/requested-count} : get requested feedbacks count.
     *
     * @return the {@link RestResponse} with the requested feedbacks count {@code 200 (OK)}.
     */
    @GetMapping("/feedbacks/requested-count")
    @Operation(summary = "Get requested feedbacks count for colleague", tags = {"feedback"})
    @PreAuthorize("isColleague()")
    public RestResponse<Integer> getRequestedFeedbackCount(@CurrentSecurityContext(expression = "authentication")
                                                                       Authentication authentication) {
        var colleagueUuid = getColleagueUuid(authentication);
        log.debug("REST request to get requested Feedbacks count for colleague : {}", colleagueUuid);
        return RestResponse.success(feedbackService.getRequestedFeedbackCount(colleagueUuid));
    }

}
