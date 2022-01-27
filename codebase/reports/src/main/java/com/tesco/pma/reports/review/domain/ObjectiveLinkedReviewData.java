package com.tesco.pma.reports.review.domain;

import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

/**
 * Objectives linked with reviews data
 */
@EqualsAndHashCode
@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ObjectiveLinkedReviewData {

    String iamId;
    String colleagueUUID;
    String firstName;
    String lastName;
    String workLevel;
    String jobTitle;
    String lineManager;
    Integer objectiveNumber;
    PMTimelinePointStatus status;
    String strategicPriority;
    String objectiveTitle;
    String howAchieved;
    String howOverAchieved;

    public List<Object> toList() {
        var strings = new ArrayList<>();

        strings.add(iamId);
        strings.add(colleagueUUID);
        strings.add(firstName);
        strings.add(lastName);
        strings.add(workLevel);
        strings.add(jobTitle);
        strings.add(lineManager);
        strings.add(objectiveNumber);
        strings.add(status);
        strings.add(strategicPriority);
        strings.add(objectiveTitle);
        strings.add(howAchieved);
        strings.add(howOverAchieved);

        return strings;
    }
}