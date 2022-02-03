package com.tesco.pma.reports.review.domain.provider;

import com.tesco.pma.api.ValueType;
import com.tesco.pma.reporting.ReportMetadata;
import com.tesco.pma.reporting.Reportable;
import com.tesco.pma.reporting.metadata.ColumnMetadata;
import com.tesco.pma.reports.review.domain.ReviewStatsData;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.stream.Collectors;

import static com.tesco.pma.api.ValueType.INTEGER;
import static com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider.ColumnMetadataEnum.OBJECTIVES_SUBMITTED_PERCENTAGE;
import static com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider.ColumnMetadataEnum.OBJECTIVES_APPROVED_PERCENTAGE;

/**
 * Review Statistics Report with data, metadata
 */
@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewStatsReportProvider implements Reportable {

    public static final String REPORT_NAME = "review-statistics-report";
    public static final String REPORT_DESCRIPTION = "Review Statistics Report";
    public static final String REPORT_FILE_NAME = "ReviewStatisticsReport.xlsx";
    public static final String REPORT_SHEET_NAME = "Report";

    enum ColumnMetadataEnum {
        OBJECTIVES_SUBMITTED_PERCENTAGE("ObjectivesSubmittedPercentage",
                "ObjectivesSubmitted", INTEGER,
                "Number of individuals with submitted objectives/all individuals with objectives"),
        OBJECTIVES_APPROVED_PERCENTAGE("ObjectivesApprovedPercentage",
                "ObjectivesApproved", INTEGER,
                "Number of individuals with approved objectives/all individuals with objectives");

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

    List<ReviewStatsData> data;

    @Override
    public List<List<Object>> getReportData() {
        return data.stream()
                .map(c -> c.toList())
                .collect(Collectors.toList());
    }

    @Override
    public ReportMetadata getReportMetadata() {
        var reportMetadata = new ReportMetadata();
        reportMetadata.setId(REPORT_NAME);
        reportMetadata.setCode(REPORT_NAME);
        reportMetadata.setDescription(REPORT_DESCRIPTION);
        reportMetadata.setFileName(REPORT_FILE_NAME);
        reportMetadata.setSheetName(REPORT_SHEET_NAME);
        reportMetadata.setColumnMetadata(getColumnMetadata());
        return reportMetadata;
    }

    private List<ColumnMetadata> getColumnMetadata() {
        return List.of(
                OBJECTIVES_SUBMITTED_PERCENTAGE.getColumnMetadata(),
                OBJECTIVES_APPROVED_PERCENTAGE.getColumnMetadata()
        );
    }
}