package com.tesco.pma.reports.review.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

/**
 * Review report statistics data
 */
@EqualsAndHashCode
@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuppressWarnings("PMD.TooManyFields")
public class ReviewStatsData {

    int objectivesSubmittedPercentage;
    int objectivesApprovedPercentage;

    int myrSubmittedPercentage;
    int myrApprovedPercentage;
    int eyrSubmittedPercentage;
    int eyrApprovedPercentage;

    int myrRatingBreakdownBelowExpectedPercentage;
    long myrRatingBreakdownBelowExpectedCount;
    int myrRatingBreakdownSatisfactoryPercentage;
    long myrRatingBreakdownSatisfactoryCount;
    int myrRatingBreakdownGreatPercentage;
    long myrRatingBreakdownGreatCount;
    int myrRatingBreakdownOutstandingPercentage;
    long myrRatingBreakdownOutstandingCount;


    int eyrRatingBreakdownBelowExpectedPercentage;
    long eyrRatingBreakdownBelowExpectedCount;
    int eyrRatingBreakdownSatisfactoryPercentage;
    long eyrRatingBreakdownSatisfactoryCount;
    int eyrRatingBreakdownGreatPercentage;
    long eyrRatingBreakdownGreatCount;
    int eyrRatingBreakdownOutstandingPercentage;
    long eyrRatingBreakdownOutstandingCount;


    public List<Object> toList() {
        var statistics = new ArrayList<>();

        statistics.add(objectivesSubmittedPercentage);
        statistics.add(objectivesApprovedPercentage);

        statistics.add(myrSubmittedPercentage);
        statistics.add(myrApprovedPercentage);
        statistics.add(eyrSubmittedPercentage);
        statistics.add(eyrApprovedPercentage);

        statistics.add(myrRatingBreakdownBelowExpectedPercentage);
        statistics.add(myrRatingBreakdownBelowExpectedCount);
        statistics.add(myrRatingBreakdownSatisfactoryPercentage);
        statistics.add(myrRatingBreakdownSatisfactoryCount);
        statistics.add(myrRatingBreakdownGreatPercentage);
        statistics.add(myrRatingBreakdownGreatCount);
        statistics.add(myrRatingBreakdownOutstandingPercentage);
        statistics.add(myrRatingBreakdownOutstandingCount);

        statistics.add(eyrRatingBreakdownBelowExpectedPercentage);
        statistics.add(eyrRatingBreakdownBelowExpectedCount);
        statistics.add(eyrRatingBreakdownSatisfactoryPercentage);
        statistics.add(eyrRatingBreakdownSatisfactoryCount);
        statistics.add(eyrRatingBreakdownGreatPercentage);
        statistics.add(eyrRatingBreakdownGreatCount);
        statistics.add(eyrRatingBreakdownOutstandingPercentage);
        statistics.add(eyrRatingBreakdownOutstandingCount);

        return statistics;
    }
}