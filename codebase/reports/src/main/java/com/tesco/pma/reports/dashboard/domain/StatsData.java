package com.tesco.pma.reports.dashboard.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

/**
 * Report statistics data
 */
@EqualsAndHashCode
@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuppressWarnings("PMD.TooManyFields")
public class StatsData {

    int colleaguesCount;

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

    long newToBusinessCount;

    int anniversaryReviewPerQuarter1Percentage;
    long anniversaryReviewPerQuarter1Count;
    int anniversaryReviewPerQuarter2Percentage;
    long anniversaryReviewPerQuarter2Count;
    int anniversaryReviewPerQuarter3Percentage;
    long anniversaryReviewPerQuarter3Count;
    int anniversaryReviewPerQuarter4Percentage;
    long anniversaryReviewPerQuarter4Count;

    @SuppressWarnings("PMD.NcssCount")
    public List<Object> toList() {
        var statistics = new ArrayList<>();

        statistics.add(colleaguesCount);
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

        statistics.add(newToBusinessCount);

        statistics.add(anniversaryReviewPerQuarter1Percentage);
        statistics.add(anniversaryReviewPerQuarter1Count);
        statistics.add(anniversaryReviewPerQuarter2Percentage);
        statistics.add(anniversaryReviewPerQuarter2Count);
        statistics.add(anniversaryReviewPerQuarter3Percentage);
        statistics.add(anniversaryReviewPerQuarter3Count);
        statistics.add(anniversaryReviewPerQuarter4Percentage);
        statistics.add(anniversaryReviewPerQuarter4Count);

        return statistics;
    }
}