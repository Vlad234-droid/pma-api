package com.tesco.pma.reporting.util;

import com.tesco.pma.api.ValueType;
import com.tesco.pma.pagination.Condition;
import com.tesco.pma.reporting.metadata.ColumnMetadata;
import com.tesco.pma.reporting.review.domain.ObjectiveLinkedReviewData;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.tesco.pma.api.ValueType.INTEGER;
import static com.tesco.pma.api.ValueType.STRING;
import static com.tesco.pma.pagination.Condition.Operand.IN;
import static com.tesco.pma.reporting.review.domain.provider.ObjectiveLinkedReviewReportProvider.REPORT_SHEET_NAME;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ExcelReportUtilsTest {

    private static final String COLLEAGUE_UUID = "10000000-0000-0000-0000-000000000000";
    private static final String LINE_MANAGER_UUID = "10000000-0000-0000-0000-000000000002";

    enum ColumnMetadataEnum {
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

    @Test
    void buildResource() throws IOException {
        var columnMetadata = getColumnMetadata();
        var reportData = getReportData();

        var resource = ExcelReportUtils.buildResource(REPORT_SHEET_NAME, reportData, columnMetadata);

        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
        assertTrue(resource instanceof ByteArrayResource);
        var workbook = new XSSFWorkbook(resource.getInputStream());
        var sheet = workbook.getSheet(REPORT_SHEET_NAME);
        assertNotNull(sheet);
        assertEquals(reportData.size(), sheet.getLastRowNum());
        checkHeader(sheet, columnMetadata);
        checkData(1, sheet, reportData.get(0), columnMetadata);
    }

    @Test
    void buildResourceWithStatistics() {
    }

    @Test
    void formatCondition() {
        var condition = new Condition("stats", IN, asList("colleagues-count", "new-to-business-count"));

        var formatted = ExcelReportUtils.formatCondition(condition);

        assertEquals("stats IN [colleagues-count, new-to-business-count]", formatted);
    }

    private void checkStatisticsData() {

    }

    private void checkHeader(XSSFSheet sheet, List<ColumnMetadata> columnMetadata) {
        var headerRow = sheet.getRow(0);

        var rowCellsIterator = headerRow.cellIterator();
        var cellIndex = 0;
        while(rowCellsIterator.hasNext()) {
            assertEquals(columnMetadata.get(cellIndex++).getName(), rowCellsIterator.next().getStringCellValue());
        }
    }

    private void checkData(int rowNum, XSSFSheet sheet, List<Object> data, List<ColumnMetadata> columnMetadata) {
        var row = sheet.getRow(rowNum);

        var rowCellsIterator = row.cellIterator();
        var cellIndex = 0;
        while(rowCellsIterator.hasNext()) {
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
        var reportData = List.of(buildObjectiveLinkedReviewData(1));
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

    private ObjectiveLinkedReviewData buildObjectiveLinkedReviewData(Integer objectiveNumber) {
        var reportData = new ObjectiveLinkedReviewData();
        reportData.setIamId("UKE12375189");
        reportData.setColleagueUUID(COLLEAGUE_UUID);
        reportData.setFirstName("Name");
        reportData.setLastName("Surname");
        reportData.setWorkLevel("WL5");
        reportData.setJobTitle("JobTitle");
        reportData.setLineManager(LINE_MANAGER_UUID);
        reportData.setObjectiveNumber(objectiveNumber);

        return reportData;
    }

    private List<ColumnMetadata> getColumnMetadata() {
        return List.of(
                ColumnMetadataEnum.IAM_ID.getColumnMetadata(),
                ColumnMetadataEnum.COLLEAGUE_UUID.getColumnMetadata(),
                ColumnMetadataEnum.FIRST_NAME.getColumnMetadata(),
                ColumnMetadataEnum.LAST_NAME.getColumnMetadata(),
                ColumnMetadataEnum.WORKING_LEVEL.getColumnMetadata(),
                ColumnMetadataEnum.JOB_TITLE.getColumnMetadata(),
                ColumnMetadataEnum.LINE_MANAGER.getColumnMetadata(),
                ColumnMetadataEnum.OBJECTIVE_NUMBER.getColumnMetadata());
    }
}