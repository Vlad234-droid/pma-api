package com.tesco.pma.reporting.review.domain;

import com.tesco.pma.reporting.Reportable;
import com.tesco.pma.reporting.metadata.ColumnMetadata;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.EMPLOYEE_NO;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.EMPLOYEE_UUID;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.FIRST_NAME;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.HOW_ACHIEVED;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.HOW_OVER_ACHIEVED;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.JOB_TITLE;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.LAST_NAME;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.LINE_MANAGER;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.OBJECTIVE_NUMBER;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.OBJECTIVE_TITLE;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.STRATEGIC_PRIORITY;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.WORKING_LEVEL;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ObjectiveReport implements Reportable {
    String employeeNo;
    String employeeUUID;
    String firstName;
    String lastName;
    String workingLevel;
    String jobTitle;
    String lineManager;
    Integer objectiveNumber;
    String strategicPriority;
    String objectiveTitle;
    String howAchieved;
    String howOverAchieved;

    @Override
    public List<List<Object>> getReportData() {
        return List.of(List.of(
                employeeNo,
                employeeUUID,
                firstName,
                lastName,
                workingLevel,
                jobTitle,
                lineManager,
                objectiveNumber,
                strategicPriority,
                objectiveTitle,
                howAchieved,
                howOverAchieved));
    }

    @Override
    public List<ColumnMetadata> getReportMetadata() {
        return List.of(
                EMPLOYEE_NO.getColumnMetadata(),
                EMPLOYEE_UUID.getColumnMetadata(),
                FIRST_NAME.getColumnMetadata(),
                LAST_NAME.getColumnMetadata(),
                WORKING_LEVEL.getColumnMetadata(),
                JOB_TITLE.getColumnMetadata(),
                LINE_MANAGER.getColumnMetadata(),
                OBJECTIVE_NUMBER.getColumnMetadata(),
                STRATEGIC_PRIORITY.getColumnMetadata(),
                OBJECTIVE_TITLE.getColumnMetadata(),
                HOW_ACHIEVED.getColumnMetadata(),
                HOW_OVER_ACHIEVED.getColumnMetadata());
    }
}
