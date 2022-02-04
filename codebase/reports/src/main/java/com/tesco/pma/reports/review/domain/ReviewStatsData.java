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

    Integer objectivesSubmittedPercentage;
    Integer objectivesApprovedPercentage;

    Integer myrSubmittedPercentage;
    Integer myrApprovedPercentage;
    Integer eyrSubmittedPercentage;
    Integer eyrApprovedPercentage;

    Integer myrRatingBreakdownBelowExpectedPercentage;
    Long myrRatingBreakdownBelowExpectedCount;
    Integer myrRatingBreakdownSatisfactoryPercentage;
    Long myrRatingBreakdownSatisfactoryCount;
    Integer myrRatingBreakdownGreatPercentage;
    Long myrRatingBreakdownGreatCount;
    Integer myrRatingBreakdownOutstandingPercentage;
    Long myrRatingBreakdownOutstandingCount;


    Integer eyrRatingBreakdownBelowExpectedPercentage;
    Long eyrRatingBreakdownBelowExpectedCount;
    Integer eyrRatingBreakdownSatisfactoryPercentage;
    Long eyrRatingBreakdownSatisfactoryCount;
    Integer eyrRatingBreakdownGreatPercentage;
    Long eyrRatingBreakdownGreatCount;
    Integer eyrRatingBreakdownOutstandingPercentage;
    Long eyrRatingBreakdownOutstandingCount;


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