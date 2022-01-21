package com.tesco.pma.reporting.review.domain;

import com.tesco.pma.reporting.Reportable;
import com.tesco.pma.reporting.metadata.ColumnMetadata;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.stream.Collectors;

import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.IAM_ID;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.COLLEAGUE_UUID;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.FIRST_NAME;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.HOW_ACHIEVED;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.HOW_OVER_ACHIEVED;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.JOB_TITLE;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.LAST_NAME;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.LINE_MANAGER;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.OBJECTIVE_NUMBER;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.OBJECTIVE_TITLE;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.STATUS;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.STRATEGIC_PRIORITY;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.WORKING_LEVEL;

/**
 * Objectives linked with reviews report with data, metadata
 */
@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ObjectiveLinkedReviewReportProvider implements Reportable {

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
