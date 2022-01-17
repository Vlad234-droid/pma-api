package com.tesco.pma.reporting.review.service.rest;

import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.reporting.Report;
import com.tesco.pma.reporting.review.service.ReviewReportingService;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.tesco.pma.rest.RestResponse.success;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RestController
@Validated
public class ReviewReportingEndpoint {

    private final ReviewReportingService reviewReportingService;

    /**
     * Get call using a Path param and return an Objectives linked with reviews data as JSON.
     *
     * @param tlPointUuid     an identifier of timeline point
     * @param status          a status of timeline point
     * @return a RestResponse parameterized with Objectives report data
     */
    @Operation(summary = "Get a Linked Objectives Report by its tlPointUuid and status", tags = {"report"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the Report data")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Report data not found", content = @Content)
    @GetMapping(path = "/review-reports/pm-linked-objective-report/timeline-points/{tlPointUuid}/statuses/{status}",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isTalentAdmin() or isAdmin()")
    public RestResponse<Report> getLinkedObjectivesReport(@PathVariable("tlPointUuid") UUID tlPointUuid,
                                                          @PathVariable("status") PMTimelinePointStatus status) {
        return success(reviewReportingService.getLinkedObjectivesData(tlPointUuid, status));
    }
}