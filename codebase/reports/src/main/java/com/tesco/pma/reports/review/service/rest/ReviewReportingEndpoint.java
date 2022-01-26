package com.tesco.pma.reports.review.service.rest;

import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.reporting.Report;
import com.tesco.pma.reports.review.service.ReviewReportingService;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.tesco.pma.reports.util.ReportUtils.LINKED_OBJECTIVES_REPORT_FILE_NAME;
import static com.tesco.pma.reports.util.ReportUtils.LINKED_OBJECTIVES_REPORT_SHEET_NAME;
import static com.tesco.pma.reports.util.ReportUtils.buildResource;
import static com.tesco.pma.rest.RestResponse.success;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RestController
@Validated
public class ReviewReportingEndpoint {

    static final MediaType APPLICATION_FORCE_DOWNLOAD_VALUE = new MediaType("application", "force-download");

    private final ReviewReportingService reviewReportingService;

    /**
     * Get call using a Path param and return an Objectives linked with reviews data as Resource.
     *
     * @param requestQuery -  filter contains start time, end time of colleague cycle and status of timeline point
     * @return a RestResponse parameterized with Objectives report resource
     */
    @Operation(summary = "Get a Linked Objectives Report by its tlPointUuid and status", tags = {"report"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the Report data")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Report data not found", content = @Content)
    @GetMapping(path = "/linked-objective-report",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isPeopleTeam() or isTalentAdmin() or isAdmin()")
    public ResponseEntity<Resource> getLinkedObjectivesReportFile(RequestQuery requestQuery) {
        var report = reviewReportingService.getLinkedObjectivesReport(requestQuery);

        var reportData = report.getData();
        var reportMetadata = report.getMetadata();

        var resource = buildResource(LINKED_OBJECTIVES_REPORT_FILE_NAME, LINKED_OBJECTIVES_REPORT_SHEET_NAME,
                reportData, reportMetadata);
        if (resource == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        var httpHeader = new HttpHeaders();
        httpHeader.setContentType(APPLICATION_FORCE_DOWNLOAD_VALUE);
        httpHeader.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + LINKED_OBJECTIVES_REPORT_FILE_NAME);

        return new ResponseEntity<>(resource, httpHeader, CREATED);
    }

    /**
     * Get call using a Path param and return an Objectives linked with reviews report data as JSON.
     *
     * @param requestQuery -  filter contains start time, end time of colleague cycle and status of timeline point
     * @return a RestResponse parameterized with Objectives report data
     */
    @Operation(summary = "Get a Linked Objectives Report Data by its tlPointUuid and status", tags = {"report"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the Report data")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Report data not found", content = @Content)
    @GetMapping(path = "/linked-objective-report-data",
            produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isPeopleTeam() or isTalentAdmin() or isAdmin()")
    public RestResponse<Report> getLinkedObjectivesReportData(RequestQuery requestQuery) {
        return success(reviewReportingService.getLinkedObjectivesReport(requestQuery));
    }
}