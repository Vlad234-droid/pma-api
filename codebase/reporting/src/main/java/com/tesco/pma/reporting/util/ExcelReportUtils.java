package com.tesco.pma.reporting.util;

import com.tesco.pma.pagination.Condition;
import com.tesco.pma.reporting.metadata.ColumnMetadata;
import lombok.experimental.UtilityClass;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static java.time.Instant.now;

/**
 * Excel utilities to build Excel reports
 */
@UtilityClass
public class ExcelReportUtils {

    /**
     * Build Excel Report
     *
     * @param sheetName       - name of sheet
     * @param reportData      - data of report
     * @param columnMetadata  - column metadata, report name, etc
     * @return resource with Report
     * @throws IOException if the resource cannot be written
     */
    public static Resource buildResource(String sheetName,
                                         List<List<Object>> reportData, List<ColumnMetadata> columnMetadata) throws IOException {
        try (var outputStream = new ByteArrayOutputStream(); var workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet(sheetName);

            buildHeader(columnMetadata, sheet);
            buildData(reportData, sheet);

            workbook.write(outputStream);

            return new ByteArrayResource(outputStream.toByteArray());
        }
    }

    /**
     * Build Statistics Excel Report
     *
     * @param statistics      - statistics to include in report
     * @param sheetName       - name of sheet
     * @param filters         - UI filters
     * @param reportData      - data of report
     * @param columnMetadata  - column metadata, report name, etc
     * @return resource with Statistics Report
     * @throws IOException if the resource cannot be written
     */
    public static Resource buildResourceWithStatistics(List<String> statistics, String sheetName,
                                                       String filters,
                                                       List<List<Object>> reportData,
                                                       List<ColumnMetadata> columnMetadata) throws IOException {
        try (var outputStream = new ByteArrayOutputStream(); var workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet(sheetName);

            buildStatisticsData(statistics, filters, reportData, columnMetadata, sheet);

            workbook.write(outputStream);

            return new ByteArrayResource(outputStream.toByteArray());
        }
    }

    private void buildStatisticsData(List<String> statistics,
                                     String filters,
                                     List<List<Object>> reportData, List<ColumnMetadata> columnMetadata,
                                     XSSFSheet sheet) {
        var rowCount = 0;
        var rowWithDate = sheet.createRow(rowCount++);
        createCell("Date:", 0, rowWithDate);
        createCell(now().toString(), 1, rowWithDate);

        var rowWithFilters = sheet.createRow(rowCount++);
        createCell("Filters applied:", 0, rowWithFilters);
        createCell(filters, 1, rowWithFilters);

        for (int i = 0; i < columnMetadata.size(); i++) {
            if (statistics.contains(columnMetadata.get(i).getId())) {
                var row = sheet.createRow(rowCount++);
                createCell(columnMetadata.get(i).getName(), 0, row);
                createCell(reportData.get(0).get(i), 1, row);
            }
        }
    }

    /**
     * Format condition of filter
     * @param condition  - condition of filter
     * @return formatted condition
     */
    public String formatCondition(Condition condition) {
        return String.format("%s %s %s", condition.getProperty(), condition.getOperand().name(), condition.getValue());
    }

    private void createCell(Object value, int columnIndex, XSSFRow row) {
        var cell = row.createCell(columnIndex);
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Long) {
            cell.setCellValue((Long) value);
        } else if (value instanceof Enum<?>) {
            cell.setCellValue(value.toString());
        }
    }

    private void buildData(List<List<Object>> reportData, XSSFSheet sheet) {
        var rowCount = 0;
        for (List<Object> data : reportData) {
            var row = sheet.createRow(++rowCount);
            var column = 0;

            for (var field : data) {
                createCell(field, column++, row);
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