package com.tesco.pma.reporting.metadata;

import com.tesco.pma.api.ValueType;
import lombok.Getter;

import static com.tesco.pma.api.ValueType.INTEGER;
import static com.tesco.pma.api.ValueType.STRING;

@Getter
public enum ColumnMetadataEnum {
    IAM_ID("IamId", "Employee No", STRING, "Employee No"),
    COLLEAGUE_UUID("ColleagueUUID", "Employee UUID", STRING, "Employee UUID"),
    FIRST_NAME("FirstName", "First Name", STRING, "First Name"),
    LAST_NAME("LastName", "Surname", STRING, "Surname"),
    WORKING_LEVEL("WorkingLevel", "Working level", STRING, "Working level"),
    JOB_TITLE("JobTitle", "Job title", STRING, "Job title"),
    LINE_MANAGER("LineManager", "Line Manager", STRING, "Line Manager"),
    OBJECTIVE_NUMBER("ObjectiveNumber", "Objective number", INTEGER, "Objective number"),
    STATUS("ObjectiveStatus", "Objective Status", STRING, "Objective Status"),
    STRATEGIC_PRIORITY("StrategicPriority", "Strategic priority", STRING,
            "Link to Strategic priorities"),
    OBJECTIVE_TITLE("ObjectiveTitle", "Objective title", STRING, "Objective title"),
    HOW_ACHIEVED("HowAchieved", "How achieved", STRING,
            "How do I know I've ACHIEVED this objective?"),
    HOW_OVER_ACHIEVED("HowOverAchieved", "How over-achieved", STRING,
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
}
