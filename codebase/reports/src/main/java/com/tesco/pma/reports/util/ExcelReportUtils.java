package com.tesco.pma.reports.util;

import com.tesco.pma.reporting.metadata.ColumnMetadata;
import lombok.experimental.UtilityClass;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Excel utilities to build Excel report
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