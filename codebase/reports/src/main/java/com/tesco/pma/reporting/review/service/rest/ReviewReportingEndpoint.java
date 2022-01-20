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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
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
    public ResponseEntity<Resource> getLinkedObjectivesReport(RequestQuery requestQuery) {
        var year = getProperty(requestQuery, "year");
        var statuses = getProperty(requestQuery, "statuses");

        var report = reviewReportingService.getLinkedObjectivesData(
                (year != null) ? Integer.parseInt(year.toString()) : DEFAULT_YEAR,
                getTimelinePointStatuses(statuses));

        var reportData = report.getData();
        var reportMetadata = report.getMetadata();

        var fileName = "ObjectivesReport.xlsx";
        try (var outputStream = new ByteArrayOutputStream(); var workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Report");

            Row header = sheet.createRow(0);
            buildHeader(reportMetadata, header);
            buildData(reportData, sheet);

            workbook.write(outputStream);
            HttpHeaders httpHeader = new HttpHeaders();
            httpHeader.setContentType(new MediaType("application", "force-download"));
            httpHeader.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

            return new ResponseEntity<>(new ByteArrayResource(outputStream.toByteArray()), httpHeader, HttpStatus.CREATED);
        } catch (Exception e) {
            log.warn("Resource was not closed correctly: " + fileName, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void buildData(List<List<Object>> reportData, XSSFSheet sheet) {
        int rowCount = 0;
        for (List<Object> data : reportData) {
            Row row = sheet.createRow(++rowCount);
            int column = 0;

            for (Object field : data) {
                Cell cell = row.createCell(column++);
                if (field instanceof String) {
                    cell.setCellValue((String) field);
                } else if (field instanceof Integer) {
                    cell.setCellValue((Integer) field);
                }
            }
        }
    }

    private void buildHeader(List<ColumnMetadata> reportMetadata, Row header) {
        for (int column = 0; column < reportMetadata.size(); column++) {
            Cell headerCell = header.createCell(column);
            headerCell.setCellValue(reportMetadata.get(column).getName());
        }
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