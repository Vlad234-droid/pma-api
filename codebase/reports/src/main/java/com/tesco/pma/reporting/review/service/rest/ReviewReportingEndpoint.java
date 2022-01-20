package com.tesco.pma.reporting.review.service.rest;

import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.pagination.Condition;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.reporting.metadata.ColumnMetadata;
import com.tesco.pma.reporting.review.service.ReviewReportingService;
import com.tesco.pma.rest.HttpStatusCodes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.List;

import static com.tesco.pma.cycle.api.PMTimelinePointStatus.APPROVED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@RestController
@Validated
public class ReviewReportingEndpoint {

    static final String FILE_NAME = "ObjectivesReport.xlsx";
    static final MediaType APPLICATION_FORCE_DOWNLOAD_VALUE = new MediaType("application", "force-download");

    private static final PMTimelinePointStatus DEFAULT_STATUS = APPROVED;
    private static final Integer DEFAULT_YEAR = 2021;
    private static final String REPORT_SHEETNAME = "Report";

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
    public ResponseEntity<Resource> getLinkedObjectivesReport(RequestQuery requestQuery) {
        var yearParam = getProperty(requestQuery, "year");
        var statusesParam = getProperty(requestQuery, "statuses");

        var report = reviewReportingService.getLinkedObjectivesData(getYear(yearParam), getStatuses(statusesParam));

        var reportData = report.getData();
        var reportMetadata = report.getMetadata();

        try (var outputStream = new ByteArrayOutputStream(); var workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet(REPORT_SHEETNAME);

            buildHeader(reportMetadata, sheet);
            buildData(reportData, sheet);

            workbook.write(outputStream);
            var httpHeader = new HttpHeaders();
            httpHeader.setContentType(APPLICATION_FORCE_DOWNLOAD_VALUE);
            httpHeader.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + FILE_NAME);

            return new ResponseEntity<>(new ByteArrayResource(outputStream.toByteArray()), httpHeader, HttpStatus.CREATED);
        } catch (Exception e) {
            log.warn("Resource was not closed correctly: " + FILE_NAME, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void buildData(List<List<Object>> reportData, XSSFSheet sheet) {
        var rowCount = 0;
        for (List<Object> data : reportData) {
            var row = sheet.createRow(++rowCount);
            var column = 0;

            for (var field : data) {
                var cell = row.createCell(column++);
                if (field instanceof String) {
                    cell.setCellValue((String) field);
                } else if (field instanceof Integer) {
                    cell.setCellValue((Integer) field);
                }
            }
        }
    }

    private void buildHeader(List<ColumnMetadata> reportMetadata, XSSFSheet sheet) {
        var header = sheet.createRow(0);
        for (var column = 0; column < reportMetadata.size(); column++) {
            var headerCell = header.createCell(column);
            headerCell.setCellValue(reportMetadata.get(column).getName());
        }
    }

    private int getYear(Object yearParam) {
        return (yearParam != null) ? Integer.parseInt(yearParam.toString()) : DEFAULT_YEAR;
    }

    private List<PMTimelinePointStatus> getStatuses(Object statusesParam) {
        var timelinePointStatuses = new LinkedList<PMTimelinePointStatus>();

        if (statusesParam instanceof List) {
            var inputStatuses = (List<Object>) statusesParam;
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