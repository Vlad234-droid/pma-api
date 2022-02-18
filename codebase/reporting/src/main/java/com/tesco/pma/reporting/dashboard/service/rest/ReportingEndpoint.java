package com.tesco.pma.reporting.dashboard.service.rest;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.DownloadException;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.reporting.Report;
import com.tesco.pma.reporting.exception.ErrorCodes;
import com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider;
import com.tesco.pma.reporting.dashboard.domain.ColleagueReportTargeting;
import com.tesco.pma.reporting.review.service.ReviewReportingService;
import com.tesco.pma.reporting.review.domain.provider.ObjectiveLinkedReviewReportProvider;
import com.tesco.pma.reporting.dashboard.service.ReportingService;
import com.tesco.pma.reporting.util.ExcelReportUtils;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.tesco.pma.reporting.util.ExcelReportUtils.buildResource;
import static com.tesco.pma.reporting.util.ExcelReportUtils.buildResourceWithStatistics;
import static com.tesco.pma.rest.RestResponse.success;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/reports/")
@Validated
public class ReportingEndpoint {

    static final String STATS_PARAM_NAME = "stats";
    static final MediaType APPLICATION_FORCE_DOWNLOAD_VALUE = new MediaType("application", "force-download");
    static final String REPORT_NAME_PARAM_NAME = "reportName";
    static final String REQUEST_QUERY_PARAM_NAME = "requestQuery";

    private final ReportingService reportingService;
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
            throw downloadException(report.getMetadata().getName(), requestQuery, e);
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

    /**
     * Get call using a Path params and return targeting colleagues data as JSON.
     *
     * @param requestQuery -  filters
     * @return a RestResponse parameterized with targeting colleagues data
     */
    @Operation(summary = "Get Colleagues marked with Tags", tags = {"report"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found Targeting Colleagues data")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Targeting Colleagues data not found", content = @Content)
    @GetMapping(path = "targeting-colleagues", produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isPeopleTeam() or isTalentAdmin() or isAdmin()")
    public RestResponse<List<ColleagueReportTargeting>> getReviewReportColleagues(RequestQuery requestQuery) {
        return success(reportingService.getReportColleagues(requestQuery));
    }

    /**
     * Get call using a Path param and return review statistics report data as JSON.
     *
     * @param requestQuery -  filters
     * @return a RestResponse parameterized with review statistics report data
     */
    @Operation(summary = "Get Statistics Report Data with filters", tags = {"report"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the Statistics Report data")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Statistics Report data not found", content = @Content)
    @GetMapping(path = StatsReportProvider.REPORT_NAME, produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isPeopleTeam() or isTalentAdmin() or isAdmin()")
    public RestResponse<Report> getStatsReport(RequestQuery requestQuery) {
        return success(reportingService.getStatsReport(requestQuery));
    }

    /**
     * Get call using a Path param and return statistics data as Resource.
     *
     * @param requestQuery -  filters
     * @return a RestResponse parameterized with statistics report resource
     */
    @Operation(summary = "Get Statistics Report as Resource", tags = {"report"})
    @ApiResponse(responseCode = HttpStatusCodes.OK, description = "Found the Statistics Report data")
    @ApiResponse(responseCode = HttpStatusCodes.NOT_FOUND, description = "Statistics Report data not found", content = @Content)
    @GetMapping(path = StatsReportProvider.REPORT_NAME + "/formats/excel", produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("isPeopleTeam() or isTalentAdmin() or isAdmin()")
    public ResponseEntity<Resource> getStatisticsReportFile(RequestQuery requestQuery) {
        var report = reportingService.getStatsReport(requestQuery);
        var filters = requestQuery.getFilters();
        var stats = (List<String>) filters.stream()
                .filter(c -> STATS_PARAM_NAME.equalsIgnoreCase(c.getProperty()))
                .findFirst()
                .get().getValue();

        var reportData = report.getData();
        var reportMetadata = report.getMetadata().getColumnMetadata();

        var filtersOnUI = filters.stream()
                .filter(condition -> !STATS_PARAM_NAME.equalsIgnoreCase(condition.getProperty()))
                .map(ExcelReportUtils::formatCondition)
                .collect(Collectors.toList()).toString();

        Resource resource;
        try {
            resource = buildResourceWithStatistics(stats, report.getMetadata().getSheetName(), filtersOnUI, reportData, reportMetadata);
        } catch (IOException e) {
            throw downloadException(report.getMetadata().getName(), requestQuery, e);
        }

        var httpHeader = new HttpHeaders();
        httpHeader.setContentType(APPLICATION_FORCE_DOWNLOAD_VALUE);
        httpHeader.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + report.getMetadata().getFileName());

        return new ResponseEntity<>(resource, httpHeader, CREATED);
    }

    private DownloadException downloadException(String reportName, RequestQuery requestQuery, Throwable cause) {
        var message = messages.getMessage(ErrorCodes.INTERNAL_DOWNLOAD_ERROR,
                Map.of(REPORT_NAME_PARAM_NAME, reportName, REQUEST_QUERY_PARAM_NAME, requestQuery));
        return new DownloadException(ErrorCodes.INTERNAL_DOWNLOAD_ERROR.getCode(), message, reportName, cause);
    }
}