package com.tesco.pma.reporting.review.domain;

import com.tesco.pma.api.ValueType;
import com.tesco.pma.reporting.Reportable;
import com.tesco.pma.reporting.metadata.ColumnMetadata;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.stream.Collectors;

import static com.tesco.pma.api.ValueType.INTEGER;
import static com.tesco.pma.api.ValueType.STRING;
import static com.tesco.pma.reporting.review.domain.ObjectiveLinkedReviewReportProvider.ColumnMetadataEnum.COLLEAGUE_UUID;
import static com.tesco.pma.reporting.review.domain.ObjectiveLinkedReviewReportProvider.ColumnMetadataEnum.FIRST_NAME;
import static com.tesco.pma.reporting.review.domain.ObjectiveLinkedReviewReportProvider.ColumnMetadataEnum.HOW_ACHIEVED;
import static com.tesco.pma.reporting.review.domain.ObjectiveLinkedReviewReportProvider.ColumnMetadataEnum.HOW_OVER_ACHIEVED;
import static com.tesco.pma.reporting.review.domain.ObjectiveLinkedReviewReportProvider.ColumnMetadataEnum.IAM_ID;
import static com.tesco.pma.reporting.review.domain.ObjectiveLinkedReviewReportProvider.ColumnMetadataEnum.JOB_TITLE;
import static com.tesco.pma.reporting.review.domain.ObjectiveLinkedReviewReportProvider.ColumnMetadataEnum.LAST_NAME;
import static com.tesco.pma.reporting.review.domain.ObjectiveLinkedReviewReportProvider.ColumnMetadataEnum.LINE_MANAGER;
import static com.tesco.pma.reporting.review.domain.ObjectiveLinkedReviewReportProvider.ColumnMetadataEnum.OBJECTIVE_NUMBER;
import static com.tesco.pma.reporting.review.domain.ObjectiveLinkedReviewReportProvider.ColumnMetadataEnum.OBJECTIVE_TITLE;
import static com.tesco.pma.reporting.review.domain.ObjectiveLinkedReviewReportProvider.ColumnMetadataEnum.STATUS;
import static com.tesco.pma.reporting.review.domain.ObjectiveLinkedReviewReportProvider.ColumnMetadataEnum.STRATEGIC_PRIORITY;
import static com.tesco.pma.reporting.review.domain.ObjectiveLinkedReviewReportProvider.ColumnMetadataEnum.WORKING_LEVEL;

/**
 * Objectives linked with reviews report with data, metadata
 */
@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ObjectiveLinkedReviewReportProvider implements Reportable {

     enum ColumnMetadataEnum {
        IAM_ID("IamId", "Employee No", STRING, "Employee No"),
        COLLEAGUE_UUID("ColleagueUUID", "Employee UUID", STRING, "Employee UUID"),
        FIRST_NAME("FirstName", "First Name", STRING, "First Name"),
        LAST_NAME("LastName", "Surname", STRING, "Surname"),
        WORKING_LEVEL("WorkingLevel", "Working level", STRING, "Working level"),
        JOB_TITLE("JobTitle", "Job title", STRING, "Job title"),
        LINE_MANAGER("LineManager", "Line Manager", STRING, "Line Manager"),
        OBJECTIVE_NUMBER("ObjectiveNumber", "Objective number", INTEGER, "Objective number"),
        STATUS("ObjectiveStatus", "Objective Status", STRING, "Objective Status"),
        STRATEGIC_PRIORITY("Strategic_Priority", "Strategic priority", STRING,
                "Link to Strategic priorities"),
        OBJECTIVE_TITLE("Title", "Objective title", STRING, "Objective title"),
        HOW_ACHIEVED("How_Achieved", "How achieved", STRING,
                "How do I know I've ACHIEVED this objective?"),
        HOW_OVER_ACHIEVED("How_Over_Achieved", "How over-achieved", STRING,
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
    public List<ColumnMetadata> getReportMetadata() {
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
                STRATEGIC_PRIORITY.getColumnMetadata(),
                OBJECTIVE_TITLE.getColumnMetadata(),
                HOW_ACHIEVED.getColumnMetadata(),
                HOW_OVER_ACHIEVED.getColumnMetadata());
    }
}
