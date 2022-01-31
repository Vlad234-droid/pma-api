package com.tesco.pma.reporting;

import com.tesco.pma.api.DictionaryItem;
import com.tesco.pma.reporting.metadata.ColumnMetadata;
import lombok.Data;

import java.util.List;

/**
 * Metadata of Report: column metadata, report name, file name, etc
 */
@Data
public class ReportMetadata implements DictionaryItem<String> {
    List<ColumnMetadata> columnMetadata;
    private String id;
    private String code;
    private String description;
    private String fileName;
    private String sheetName;

    public String getName() {
        return code;
    }
}