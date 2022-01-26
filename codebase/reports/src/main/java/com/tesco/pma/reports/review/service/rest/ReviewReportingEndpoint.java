package com.tesco.pma.reports.review.service.rest;

import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.reporting.Report;
import com.tesco.pma.reporting.metadata.ColumnMetadata;
import com.tesco.pma.reports.review.service.ReviewReportingService;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
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
import java.util.List;

import static com.tesco.pma.rest.RestResponse.success;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RequiredArgsConstructor
@RestController
@Validated
public class ReviewReportingEndpoint {

    static final String FILE_NAME = "ObjectivesReport.xlsx";
    static final MediaType APPLICATION_FORCE_DOWNLOAD_VALUE = new MediaType("application", "force-download");

    private static final String REPORT_SHEETNAME = "Report";

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

        var resource = buildResource(reportData, reportMetadata);
        if (resource == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        var httpHeader = new HttpHeaders();
        httpHeader.setContentType(APPLICATION_FORCE_DOWNLOAD_VALUE);
        httpHeader.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + FILE_NAME);

        return new ResponseEntity<>(resource, httpHeader, HttpStatus.CREATED);
    }

    private Resource buildResource(List<List<Object>> reportData, List<ColumnMetadata> reportMetadata) {
        try (var outputStream = new ByteArrayOutputStream(); var workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet(REPORT_SHEETNAME);

            buildHeader(reportMetadata, sheet);
            buildData(reportData, sheet);

            workbook.write(outputStream);

            return new ByteArrayResource(outputStream.toByteArray());
        } catch (Exception e) {
            log.warn("Resource was not closed correctly: " + FILE_NAME, e);
            return null;
        }
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
                } else if (field instanceof Enum<?>) {
                    cell.setCellValue(field.toString());
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
}