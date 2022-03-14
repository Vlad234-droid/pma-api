package com.tesco.pma.reporting.review.domain.provider;

import com.tesco.pma.api.ValueType;
import com.tesco.pma.reporting.ReportMetadata;
import com.tesco.pma.reporting.Reportable;
import com.tesco.pma.reporting.metadata.ColumnMetadata;
import com.tesco.pma.reporting.review.domain.ObjectiveLinkedReviewData;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.stream.Collectors;

import static com.tesco.pma.api.ValueType.INTEGER;
import static com.tesco.pma.api.ValueType.STRING;
import static com.tesco.pma.reporting.review.domain.provider.ObjectiveLinkedReviewReportProvider.ColumnMetadataEnum.COLLEAGUE_UUID;
import static com.tesco.pma.reporting.review.domain.provider.ObjectiveLinkedReviewReportProvider.ColumnMetadataEnum.FIRST_NAME;
import static com.tesco.pma.reporting.review.domain.provider.ObjectiveLinkedReviewReportProvider.ColumnMetadataEnum.HOW_ACHIEVED;
import static com.tesco.pma.reporting.review.domain.provider.ObjectiveLinkedReviewReportProvider.ColumnMetadataEnum.HOW_OVER_ACHIEVED;
import static com.tesco.pma.reporting.review.domain.provider.ObjectiveLinkedReviewReportProvider.ColumnMetadataEnum.IAM_ID;
import static com.tesco.pma.reporting.review.domain.provider.ObjectiveLinkedReviewReportProvider.ColumnMetadataEnum.JOB_TITLE;
import static com.tesco.pma.reporting.review.domain.provider.ObjectiveLinkedReviewReportProvider.ColumnMetadataEnum.LAST_NAME;
import static com.tesco.pma.reporting.review.domain.provider.ObjectiveLinkedReviewReportProvider.ColumnMetadataEnum.LINE_MANAGER;
import static com.tesco.pma.reporting.review.domain.provider.ObjectiveLinkedReviewReportProvider.ColumnMetadataEnum.OBJECTIVE_NUMBER;
import static com.tesco.pma.reporting.review.domain.provider.ObjectiveLinkedReviewReportProvider.ColumnMetadataEnum.OBJECTIVE_TITLE;
import static com.tesco.pma.reporting.review.domain.provider.ObjectiveLinkedReviewReportProvider.ColumnMetadataEnum.STATUS;
import static com.tesco.pma.reporting.review.domain.provider.ObjectiveLinkedReviewReportProvider.ColumnMetadataEnum.STRATEGIC_DRIVER;
import static com.tesco.pma.reporting.review.domain.provider.ObjectiveLinkedReviewReportProvider.ColumnMetadataEnum.WORKING_LEVEL;

/**
 * Objectives linked with reviews report with data, metadata
 */
@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ObjectiveLinkedReviewReportProvider implements Reportable {

    public static final String REPORT_NAME = "linked-objective-report";
    public static final String REPORT_DESCRIPTION = "Linked Objectives Review Report";
    public static final String REPORT_FILE_NAME = "LinkedObjectivesReport.xlsx";
    public static final String REPORT_SHEET_NAME = "Report";

    enum ColumnMetadataEnum {
       IAM_ID("iam-id", "Employee No", STRING, "Employee No"),
       COLLEAGUE_UUID("colleague-uuid", "Employee UUID", STRING, "Employee UUID"),
       FIRST_NAME("first-name", "First Name", STRING, "First Name"),
       LAST_NAME("last-name", "Surname", STRING, "Surname"),
       WORKING_LEVEL("working-level", "Working level", STRING, "Working level"),
       JOB_TITLE("job-title", "Job title", STRING, "Job title"),
       LINE_MANAGER("line-manager", "Line Manager", STRING, "Line Manager"),
       OBJECTIVE_NUMBER("objective-number", "Objective number", INTEGER, "Objective number"),
       STATUS("objective-status", "Objective Status", STRING, "Objective Status"),
       STRATEGIC_DRIVER("strategic-driver", "Strategic driver", STRING,
               "Link to Strategic drivers"),
       OBJECTIVE_TITLE("title", "Objective title", STRING, "Objective title"),
       HOW_ACHIEVED("how-achieved", "How achieved", STRING,
               "How do I know I've ACHIEVED this objective?"),
       HOW_OVER_ACHIEVED("how-over-achieved", "How over-achieved", STRING,
               "How do I know I've OVER-ACHIEVED this objective?");

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

    List<ObjectiveLinkedReviewData> objectives;

    @Override
    public List<List<Object>> getReportData() {
        return objectives.stream()
                .map(ObjectiveLinkedReviewData::toList)
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
                IAM_ID.getColumnMetadata(),
                COLLEAGUE_UUID.getColumnMetadata(),
                FIRST_NAME.getColumnMetadata(),
                LAST_NAME.getColumnMetadata(),
                WORKING_LEVEL.getColumnMetadata(),
                JOB_TITLE.getColumnMetadata(),
                LINE_MANAGER.getColumnMetadata(),
                OBJECTIVE_NUMBER.getColumnMetadata(),
                STATUS.getColumnMetadata(),
                STRATEGIC_DRIVER.getColumnMetadata(),
                OBJECTIVE_TITLE.getColumnMetadata(),
                HOW_ACHIEVED.getColumnMetadata(),
                HOW_OVER_ACHIEVED.getColumnMetadata());
    }
}
