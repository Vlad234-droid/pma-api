package com.tesco.pma.reports.review.service.rest;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.DownloadException;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.reporting.Report;
import com.tesco.pma.reports.exception.ErrorCodes;
import com.tesco.pma.reports.review.domain.provider.ObjectiveLinkedReviewReportProvider;
import com.tesco.pma.reports.review.service.ReviewReportingService;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

import static com.tesco.pma.reports.util.ExcelReportUtils.buildResource;
import static com.tesco.pma.rest.RestResponse.success;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/reports/")
@Validated
public class ReviewReportingEndpoint {

    static final MediaType APPLICATION_FORCE_DOWNLOAD_VALUE = new MediaType("application", "force-download");

    private final ReviewReportingService reviewReportingService;
    private final NamedMessageSourceAccessor messages;

    /**
     * Get call using a Path param and return an Objectives linked with reviews data as Resource.
     *
     * @param requestQuery -  filter contains start time, end time of colleague cycle and status of timeline point
     * @return a RestResponse parameterized with Objectives report resource
     */
    @Operation(summary = "Get a Linked Objectives Report by year of cycle and statuses of review", tags = {"report"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the Report data")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Report data not found", content = @Content)
    @GetMapping(path = ObjectiveLinkedReviewReportProvider.REPORT_NAME + "/formats/excel", produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isPeopleTeam() or isTalentAdmin() or isAdmin()")
    public ResponseEntity<Resource> getLinkedObjectivesReportFile(RequestQuery requestQuery) {
        var report = reviewReportingService.getLinkedObjectivesReport(requestQuery);

        var reportData = report.getData();
        var reportMetadata = report.getMetadata().getColumnMetadata();

        Resource resource;
        try {
            resource = buildResource(report.getMetadata().getSheetName(), reportData, reportMetadata);
        } catch (IOException e) {
            var message = messages.getMessage(ErrorCodes.INTERNAL_DOWNLOAD_ERROR,
                    Map.of("reportName", report.getMetadata().getName(), "requestQuery", requestQuery));
            throw new DownloadException(ErrorCodes.INTERNAL_DOWNLOAD_ERROR.getCode(), message, report.getMetadata().getName(), e);
        }

        var httpHeader = new HttpHeaders();
        httpHeader.setContentType(APPLICATION_FORCE_DOWNLOAD_VALUE);
        httpHeader.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + report.getMetadata().getFileName());

        return new ResponseEntity<>(resource, httpHeader, CREATED);
    }

    /**
     * Get call using a Path param and return an Objectives linked with reviews report data as JSON.
     *
     * @param requestQuery -  filter contains start time, end time of colleague cycle and status of timeline point
     * @return a RestResponse parameterized with Objectives report data
     */
    @Operation(summary = "Get a Linked Objectives Report Data by year of cycle and statuses of review", tags = {"report"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the Report data")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Report data not found", content = @Content)
    @GetMapping(path = ObjectiveLinkedReviewReportProvider.REPORT_NAME, produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isPeopleTeam() or isTalentAdmin() or isAdmin()")
    public RestResponse<Report> getLinkedObjectivesReportData(RequestQuery requestQuery) {
        return success(reviewReportingService.getLinkedObjectivesReport(requestQuery));
    }
}