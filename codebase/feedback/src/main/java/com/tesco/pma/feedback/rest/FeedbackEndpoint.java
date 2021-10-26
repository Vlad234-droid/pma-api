package com.tesco.pma.feedback.rest;

import com.tesco.pma.exception.InvalidParameterException;
import com.tesco.pma.exception.InvalidPayloadException;
import com.tesco.pma.feedback.api.Feedback;
import com.tesco.pma.feedback.service.FeedbackService;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for managing {@link Feedback}.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class FeedbackEndpoint {

    private final FeedbackService feedbackService;

    /**
     * {@code POST  /feedbacks} : Create a new feedback.
     *
     * @param feedback the feedback to create.
     * @return the {@link RestResponse} with status {@code 201 (Created)} and with body the new feedback, or with status {@code 400 (Bad Request)} if the feedback has already an UUID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/feedbacks")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new feedback with items", tags = {"feedback"})
    @ApiResponse(responseCode = HttpStatusCodes.BAD_REQUEST, description = "Feedback has already an UUID")
    public RestResponse<Feedback> createFeedback(@Valid @RequestBody Feedback feedback) throws URISyntaxException {
        log.debug("REST request to save Feedback : {}", feedback);
        if (feedback.getUuid() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A new feedback cannot already have an UUID");
        }
        return RestResponse.success(feedbackService.create(feedback));
    }

    /**
     * {@code PUT  /feedbacks/:uuid} : Updates an existing feedback.
     *
     * @param uuid the uuid of the feedback to save.
     * @param feedback the feedback to update.
     * @return the {@link RestResponse} with status {@code 200 (OK)} and with body the updated feedback,
     * or with status {@code 400 (Bad Request)} if the feedback is not valid,
     * or with status {@code 500 (Internal Server Error)} if the feedback couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/feedbacks/{uuid}")
    @Operation(summary = "Updates an existing feedback", tags = {"feedback"})
    @ApiResponse(responseCode = HttpStatusCodes.BAD_REQUEST, description = "Invalid UUID")
    public RestResponse<Feedback> updateFeedback(
            @PathVariable(value = "uuid", required = false) final UUID uuid,
            @Valid @RequestBody Feedback feedback
    ) throws URISyntaxException {
        log.debug("REST request to update Feedback : {}, {}", uuid, feedback);
        if (feedback.getUuid() == null) {
            throw new InvalidPayloadException(HttpStatusCodes.BAD_REQUEST, "UUID must not be null", "feedback.uuid");
        }
        if (!Objects.equals(uuid, feedback.getUuid())) {
            throw new InvalidParameterException(HttpStatusCodes.BAD_REQUEST, "Path uuid does not match body uuid", "feedback.uuid");
        }

        Feedback result = feedbackService.update(feedback);
        return RestResponse.success(result);
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
    public RestResponse<Void> markAsRead(
            @PathVariable(value = "uuid", required = true) final UUID uuid
    ) throws URISyntaxException {
        log.debug("REST request to mark Feedback as read : {}", uuid);
        feedbackService.markAsRead(uuid);
        return RestResponse.success();
    }

    /**
     * {@code GET  /feedbacks} : get all the feedbacks.
     *
     * @return the {@link RestResponse} with status {@code 200 (OK)} and the list of feedbacks in body.
     * @param requestQuery filter, sort, offset
     */
    @GetMapping("/feedbacks")
    @Operation(summary = "Get all feedbacks with all items", tags = {"feedback"})
    public RestResponse<List<Feedback>> getAllFeedbacks(RequestQuery requestQuery) {
        log.debug("REST request to get a feedbacks of Feedbacks");
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
    public RestResponse<Feedback> getFeedback(@PathVariable UUID uuid) {
        log.debug("REST request to get Feedback : {}", uuid);
        Optional<Feedback> feedback = feedbackService.findOne(uuid);
        return feedback.map(RestResponse::success).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

}
