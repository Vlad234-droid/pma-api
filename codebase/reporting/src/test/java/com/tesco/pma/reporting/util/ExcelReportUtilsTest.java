package com.tesco.pma.reporting.util;

import com.tesco.pma.api.ValueType;
import com.tesco.pma.pagination.Condition;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.reporting.Report;
import com.tesco.pma.reporting.ReportMetadata;
import com.tesco.pma.reporting.dashboard.domain.StatsData;
import com.tesco.pma.reporting.metadata.ColumnMetadata;
import com.tesco.pma.reporting.review.domain.ObjectiveLinkedReviewData;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.tesco.pma.api.ValueType.INTEGER;
import static com.tesco.pma.api.ValueType.STRING;
import static com.tesco.pma.pagination.Condition.Operand.IN;
import static com.tesco.pma.reporting.review.domain.provider.ObjectiveLinkedReviewReportProvider.REPORT_SHEET_NAME;
import static com.tesco.pma.reporting.util.ExcelReportUtils.TOPICS_PARAM_NAME;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExcelReportUtilsTest {

    private static final String COLLEAGUE_UUID = "10000000-0000-0000-0000-000000000000";
    private static final String LINE_MANAGER_UUID = "10000000-0000-0000-0000-000000000002";

    private enum ColumnMetadataEnum {
        IAM_ID("iam-id", "Employee No", STRING, "Employee No"),
        COLLEAGUE_UUID("colleague-uuid", "Employee UUID", STRING, "Employee UUID"),
        FIRST_NAME("first-name", "First Name", STRING, "First Name"),
        LAST_NAME("last-name", "Surname", STRING, "Surname"),
        WORKING_LEVEL("working-level", "Working level", STRING, "Working level"),
        JOB_TITLE("job-title", "Job title", STRING, "Job title"),
        LINE_MANAGER("line-manager", "Line Manager", STRING, "Line Manager"),
        OBJECTIVE_NUMBER("objective-number", "Objective number", INTEGER, "Objective number");

        private final ColumnMetadata columnMetadata;

        ColumnMetadataEnum(String id,
                           String name,
                           ValueType type,
                           String description) {
            columnMetadata = new ColumnMetadata();
            columnMetadata.setId(id);
            columnMetadata.setName(name);
            columnMetadata.setType(type);
            columnMetadata.setDescription(description);
        }

        public ColumnMetadata getColumnMetadata() {
            return columnMetadata;
        }
    }

    private enum StatsColumnMetadataEnum {
        COLLEAGUES_COUNT("colleagues-count",
                "Colleagues count", INTEGER,
                "Total number of individuals"),
        OBJECTIVES_SUBMITTED_PERCENTAGE("objectives-submitted-percentage",
                "Objectives submitted percentage", INTEGER,
                "Number of individuals with submitted objectives/all individuals with objectives to submit"),
        OBJECTIVES_APPROVED_PERCENTAGE("objectives-approved-percentage",
                "Objectives approved percentage", INTEGER,
                "Number of individuals with approved objectives/all individuals with objectives to submit"),
        NEW_TO_BUSINESS_COUNT("new-to-business-count",
                "New to business count", INTEGER,
                "Number of individuals who have joined the business in the last 90 days");

        private final ColumnMetadata columnMetadata;

        StatsColumnMetadataEnum(String id,
                                String name,
                                ValueType type,
                                String description) {
            columnMetadata = new ColumnMetadata();
            columnMetadata.setId(id);
            columnMetadata.setName(name);
            columnMetadata.setType(type);
            columnMetadata.setDescription(description);
        }

        public ColumnMetadata getColumnMetadata() {
            return columnMetadata;
        }
    }

    @Test
    void buildResource() throws IOException {
        var reportData = getReportData();
        var reportMetadata = getLinkedObjectivesReportMetadata(REPORT_SHEET_NAME);
        var report = new Report();
        report.setData(reportData);
        report.setMetadata(reportMetadata);

        var resource = ExcelReportUtils.buildResource(report, new RequestQuery());

        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
        assertTrue(resource instanceof ByteArrayResource);
        try (var workbook = new XSSFWorkbook(resource.getInputStream())) {
            var sheet = workbook.getSheet(REPORT_SHEET_NAME);
            assertNotNull(sheet);
            assertEquals(reportData.size(), sheet.getLastRowNum());
            checkHeader(sheet, reportMetadata.getColumnMetadata());
            checkData(1, sheet, reportData.get(0), reportMetadata.getColumnMetadata());
        }
    }

    @Test
    void buildResourceWithTopics() throws IOException {
        var filtersOnUI = List.of(Condition.build("year", 2021), Condition.build("status", "Approved"));
        var requestQuery = new RequestQuery();
        var filters = new LinkedList<>(filtersOnUI);
        var topics = List.of("colleagues-count", "new-to-business-count");
        filters.add(new Condition(TOPICS_PARAM_NAME, IN, topics));
        requestQuery.setFilters(filters);
        var reportData = getStatsReportData();
        var reportMetadata = getStatsReportMetadata(REPORT_SHEET_NAME);
        var report = new Report();
        report.setData(reportData);
        report.setMetadata(reportMetadata);

        var resource = ExcelReportUtils.buildResourceWithStatisticTopics(report, requestQuery);

        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
        assertTrue(resource instanceof ByteArrayResource);
        try (var workbook = new XSSFWorkbook(resource.getInputStream())) {
            var sheet = workbook.getSheet(REPORT_SHEET_NAME);
            assertNotNull(sheet);
            assertEquals(topics.size() + 1, sheet.getLastRowNum());
            var filtersOnUIParam = filtersOnUI.stream()
                    .map(ExcelReportUtils::formatCondition)
                    .collect(Collectors.toList()).toString();
            checkStatisticsData(sheet, topics, filtersOnUIParam, reportData, reportMetadata.getColumnMetadata());
        }
    }

    @Test
    void formatCondition() {
        var condition = new Condition(TOPICS_PARAM_NAME, IN, asList("colleagues-count", "new-to-business-count"));

        var formatted = ExcelReportUtils.formatCondition(condition);

        assertEquals(TOPICS_PARAM_NAME + " IN [colleagues-count, new-to-business-count]", formatted);
    }

    private void checkStatisticsData(XSSFSheet sheet, List<String> statistics, String filters,
                                     List<List<Object>> reportData, List<ColumnMetadata> columnMetadata) {
        var rowNum = 0;
        var rowWithDate = sheet.getRow(rowNum++);
        assertEquals("Date:", rowWithDate.getCell(0).getStringCellValue());
        assertTrue(rowWithDate.getCell(1).getStringCellValue().startsWith(LocalDate.now().toString()));

        var rowWithFilters = sheet.getRow(rowNum++);
        assertEquals("Filters applied:", rowWithFilters.getCell(0).getStringCellValue());
        assertTrue(rowWithFilters.getCell(1).getStringCellValue().startsWith(filters));

        for (int i = 0; i < columnMetadata.size(); i++) {
            if (statistics.contains(columnMetadata.get(i).getId())) {
                var rowWithStats = sheet.getRow(rowNum++);
                assertEquals(columnMetadata.get(i).getName(), rowWithStats.getCell(0).getStringCellValue());
                assertEquals(((Number) reportData.get(0).get(i)).intValue(), (int) rowWithStats.getCell(1).getNumericCellValue());
            }
        }
    }

    private void checkHeader(XSSFSheet sheet, List<ColumnMetadata> columnMetadata) {
        var headerRow = sheet.getRow(0);

        var rowCellsIterator = headerRow.cellIterator();
        var cellIndex = 0;
        while (rowCellsIterator.hasNext()) {
            assertEquals(columnMetadata.get(cellIndex++).getName(), rowCellsIterator.next().getStringCellValue());
        }
    }

    private void checkData(int rowNum, XSSFSheet sheet, List<Object> data, List<ColumnMetadata> columnMetadata) {
        var row = sheet.getRow(rowNum);

        var rowCellsIterator = row.cellIterator();
        var cellIndex = 0;
        while (rowCellsIterator.hasNext()) {
            Object cellValue = null;
            if (STRING == columnMetadata.get(cellIndex).getType()) {
                cellValue = rowCellsIterator.next().getStringCellValue();
            } else if (INTEGER == columnMetadata.get(cellIndex).getType()) {
                cellValue = (int) rowCellsIterator.next().getNumericCellValue();
            }
            assertEquals(data.get(cellIndex++), cellValue);
        }
    }

    private List<List<Object>> getReportData() {
        var reportData = List.of(getObjectiveLinkedReviewData());
        return reportData.stream()
                .map(this::toList)
                .collect(Collectors.toList());
    }

    private List<Object> toList(ObjectiveLinkedReviewData data) {
        var strings = new ArrayList<>();
        strings.add(data.getIamId());
        strings.add(data.getColleagueUUID());
        strings.add(data.getFirstName());
        strings.add(data.getLastName());
        strings.add(data.getWorkLevel());
        strings.add(data.getJobTitle());
        strings.add(data.getLineManager());
        strings.add(data.getObjectiveNumber());

        return strings;
    }

    private List<Object> toList(StatsData data) {
        var strings = new ArrayList<>();
        strings.add(data.getColleaguesCount());
        strings.add(data.getObjectivesSubmittedPercentage());
        strings.add(data.getObjectivesApprovedPercentage());
        strings.add(data.getNewToBusinessCount());

        return strings;
    }

    private ObjectiveLinkedReviewData getObjectiveLinkedReviewData() {
        var reportData = new ObjectiveLinkedReviewData();
        reportData.setIamId("UKE12375189");
        reportData.setColleagueUUID(COLLEAGUE_UUID);
        reportData.setFirstName("Name");
        reportData.setLastName("Surname");
        reportData.setWorkLevel("WL5");
        reportData.setJobTitle("JobTitle");
        reportData.setLineManager(LINE_MANAGER_UUID);
        reportData.setObjectiveNumber(1);

        return reportData;
    }

    private ReportMetadata getLinkedObjectivesReportMetadata(String sheetName) {
        var reportMetadata = new ReportMetadata();
        reportMetadata.setSheetName(sheetName);
        reportMetadata.setColumnMetadata(List.of(
                ColumnMetadataEnum.IAM_ID.getColumnMetadata(),
                ColumnMetadataEnum.COLLEAGUE_UUID.getColumnMetadata(),
                ColumnMetadataEnum.FIRST_NAME.getColumnMetadata(),
                ColumnMetadataEnum.LAST_NAME.getColumnMetadata(),
                ColumnMetadataEnum.WORKING_LEVEL.getColumnMetadata(),
                ColumnMetadataEnum.JOB_TITLE.getColumnMetadata(),
                ColumnMetadataEnum.LINE_MANAGER.getColumnMetadata(),
                ColumnMetadataEnum.OBJECTIVE_NUMBER.getColumnMetadata()));

        return reportMetadata;
    }

    private ReportMetadata getStatsReportMetadata(String sheetName) {
        var reportMetadata = new ReportMetadata();
        reportMetadata.setSheetName(sheetName);
        reportMetadata.setColumnMetadata(List.of(
                StatsColumnMetadataEnum.COLLEAGUES_COUNT.getColumnMetadata(),
                StatsColumnMetadataEnum.OBJECTIVES_SUBMITTED_PERCENTAGE.getColumnMetadata(),
                StatsColumnMetadataEnum.OBJECTIVES_APPROVED_PERCENTAGE.getColumnMetadata(),
                StatsColumnMetadataEnum.NEW_TO_BUSINESS_COUNT.getColumnMetadata()));

        return reportMetadata;
    }

    private List<List<Object>> getStatsReportData() {
        var reportData = List.of(getStatsData());
        return reportData.stream()
                .map(this::toList)
                .collect(Collectors.toList());
    }

    private StatsData getStatsData() {
        var statsData = new StatsData();
        statsData.setColleaguesCount(5);
        statsData.setObjectivesSubmittedPercentage(55);
        statsData.setObjectivesApprovedPercentage(45);
        statsData.setNewToBusinessCount(3);

        return statsData;
    }
}