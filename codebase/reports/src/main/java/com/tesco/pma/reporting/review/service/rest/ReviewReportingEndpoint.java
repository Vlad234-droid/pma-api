package com.tesco.pma.reporting.review.service.rest;

import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.pagination.Condition;
import com.tesco.pma.pagination.RequestQuery;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;

import static com.tesco.pma.cycle.api.PMTimelinePointStatus.APPROVED;
import static com.tesco.pma.rest.RestResponse.success;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RestController
@Validated
public class ReviewReportingEndpoint {

    private static final PMTimelinePointStatus DEFAULT_STATUS = APPROVED;
    private static final Integer DEFAULT_YEAR = 2021;

    private final ReviewReportingService reviewReportingService;

    /**
     * Get call using a Path param and return an Objectives linked with reviews data as JSON.
     *
     * @param requestQuery -  filter contains start time, end time of colleague cycle and status of timeline point
     * @return a RestResponse parameterized with Objectives report data
     */
    @Operation(summary = "Get a Linked Objectives Report by its tlPointUuid and status", tags = {"report"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the Report data")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Report data not found", content = @Content)
    @GetMapping(path = "/pm-linked-objective-report",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isPeopleTeam() or isTalentAdmin() or isAdmin()")
    public RestResponse<Report> getLinkedObjectivesReport(RequestQuery requestQuery) {
        var year = getProperty(requestQuery, "year");
        var statuses = getProperty(requestQuery, "statuses");

        return success(reviewReportingService.getLinkedObjectivesData(
                (year != null) ? Integer.parseInt(year.toString()) : DEFAULT_YEAR,
                getTimelinePointStatuses(statuses)));
    }

    private List<PMTimelinePointStatus> getTimelinePointStatuses(Object statuses) {
        var timelinePointStatuses = new LinkedList<PMTimelinePointStatus>();

        if (statuses instanceof List) {
            var inputStatuses = (List<Object>) statuses;
            if (inputStatuses.isEmpty()) {
                timelinePointStatuses.add(DEFAULT_STATUS);
            }

            inputStatuses.forEach(s -> {
                var timelinePointStatus = PMTimelinePointStatus.valueOf(s.toString());
                timelinePointStatuses.add(timelinePointStatus);
            });
        }
        return timelinePointStatuses;
    }

    private Object getProperty(RequestQuery requestQuery, String propertyName) {
        var dateTime = requestQuery.getFilters().stream()
                .filter(cond -> propertyName.equalsIgnoreCase(cond.getProperty()))
                .findFirst();
        return dateTime.map(Condition::getValue).orElse(null);
    }
}