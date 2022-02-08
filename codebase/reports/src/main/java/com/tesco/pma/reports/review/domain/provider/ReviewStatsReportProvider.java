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
import static com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider.ColumnMetadataEnum.MYR_SUBMITTED_PERCENTAGE;
import static com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider.ColumnMetadataEnum.MYR_APPROVED_PERCENTAGE;
import static com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider.ColumnMetadataEnum.EYR_SUBMITTED_PERCENTAGE;
import static com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider.ColumnMetadataEnum.EYR_APPROVED_PERCENTAGE;
import static com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider.ColumnMetadataEnum.MYR_RATING_BREAKDOWN_BELOW_EXPECTED_PERCENTAGE;
import static com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider.ColumnMetadataEnum.MYR_RATING_BREAKDOWN_BELOW_EXPECTED_COUNT;
import static com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider.ColumnMetadataEnum.MYR_RATING_BREAKDOWN_SATISFACTORY_PERCENTAGE;
import static com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider.ColumnMetadataEnum.MYR_RATING_BREAKDOWN_SATISFACTORY_COUNT;
import static com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider.ColumnMetadataEnum.MYR_RATING_BREAKDOWN_GREAT_PERCENTAGE;
import static com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider.ColumnMetadataEnum.MYR_RATING_BREAKDOWN_GREAT_COUNT;
import static com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider.ColumnMetadataEnum.MYR_RATING_BREAKDOWN_OUTSTANDING_PERCENTAGE;
import static com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider.ColumnMetadataEnum.MYR_RATING_BREAKDOWN_OUTSTANDING_COUNT;

import static com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider.ColumnMetadataEnum.EYR_RATING_BREAKDOWN_BELOW_EXPECTED_PERCENTAGE;
import static com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider.ColumnMetadataEnum.EYR_RATING_BREAKDOWN_BELOW_EXPECTED_COUNT;
import static com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider.ColumnMetadataEnum.EYR_RATING_BREAKDOWN_SATISFACTORY_PERCENTAGE;
import static com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider.ColumnMetadataEnum.EYR_RATING_BREAKDOWN_SATISFACTORY_COUNT;
import static com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider.ColumnMetadataEnum.EYR_RATING_BREAKDOWN_GREAT_PERCENTAGE;
import static com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider.ColumnMetadataEnum.EYR_RATING_BREAKDOWN_GREAT_COUNT;
import static com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider.ColumnMetadataEnum.EYR_RATING_BREAKDOWN_OUTSTANDING_PERCENTAGE;
import static com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider.ColumnMetadataEnum.EYR_RATING_BREAKDOWN_OUTSTANDING_COUNT;
import static com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider.ColumnMetadataEnum.NEW_TO_BUSINESS_COUNT;
import static com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider.ColumnMetadataEnum.ANNIVERSARY_REVIEW_PER_QUARTER_1_PERCENTAGE;
import static com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider.ColumnMetadataEnum.ANNIVERSARY_REVIEW_PER_QUARTER_1_COUNT;
import static com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider.ColumnMetadataEnum.ANNIVERSARY_REVIEW_PER_QUARTER_2_PERCENTAGE;
import static com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider.ColumnMetadataEnum.ANNIVERSARY_REVIEW_PER_QUARTER_2_COUNT;
import static com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider.ColumnMetadataEnum.ANNIVERSARY_REVIEW_PER_QUARTER_3_PERCENTAGE;
import static com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider.ColumnMetadataEnum.ANNIVERSARY_REVIEW_PER_QUARTER_3_COUNT;
import static com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider.ColumnMetadataEnum.ANNIVERSARY_REVIEW_PER_QUARTER_4_PERCENTAGE;
import static com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider.ColumnMetadataEnum.ANNIVERSARY_REVIEW_PER_QUARTER_4_COUNT;

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
                "ObjectivesSubmittedPercentage", INTEGER,
                "Number of individuals with submitted objectives/all individuals with objectives to submit"),
        OBJECTIVES_APPROVED_PERCENTAGE("ObjectivesApprovedPercentage",
                "ObjectivesApprovedPercentage", INTEGER,
                "Number of individuals with approved objectives/all individuals with objectives to submit"),

        MYR_SUBMITTED_PERCENTAGE("MyrSubmittedPercentage",
                "MyrSubmittedPercentage", INTEGER,
                "Number of individuals with submitted mid-year reviews/all individuals with reviews to submit"),
        MYR_APPROVED_PERCENTAGE("MyrApprovedPercentage",
                "MyrApprovedPercentage", INTEGER,
                "Number of individuals with approved mid-year reviews/all individuals with reviews to submit"),
        EYR_SUBMITTED_PERCENTAGE("EyrSubmittedPercentage",
                "EyrSubmittedPercentage", INTEGER,
                "Number of individuals with submitted end-year reviews/all individuals with reviews to submit"),
        EYR_APPROVED_PERCENTAGE("EyrApprovedPercentage",
                "EyrApprovedPercentage", INTEGER,
                "Number of individuals with approved end-year reviews/all individuals with reviews to submit"),

        // myr ratings

        MYR_RATING_BREAKDOWN_BELOW_EXPECTED_PERCENTAGE("MyrRatingBreakdownBelowExpectedPercentage",
                "MyrRatingBreakdownBelowExpectedPercentage", INTEGER,
                "Number of individuals with approved overall rating 'Below expected'/all individuals with approved mid-year reviews"),
        MYR_RATING_BREAKDOWN_BELOW_EXPECTED_COUNT("MyrRatingBreakdownBelowExpectedCount",
                "MyrRatingBreakdownBelowExpectedCount", INTEGER,
                "Number of individuals with approved overall rating 'Below expected' individuals count"),

        MYR_RATING_BREAKDOWN_SATISFACTORY_PERCENTAGE("MyrRatingBreakdownSatisfactoryPercentage",
                "MyrRatingBreakdownSatisfactoryPercentage", INTEGER,
                "Number of individuals with approved overall rating 'Satisfactory'/all individuals with approved mid-year reviews"),
        MYR_RATING_BREAKDOWN_SATISFACTORY_COUNT("MyrRatingBreakdownSatisfactoryCount",
                "MyrRatingBreakdownSatisfactoryCount", INTEGER,
                "Number of individuals with approved overall rating 'Satisfactory' individuals count"),

        MYR_RATING_BREAKDOWN_GREAT_PERCENTAGE("MyrRatingBreakdownGreatPercentage",
                "MyrRatingBreakdownGreatPercentage", INTEGER,
                "Number of individuals with approved overall rating 'Great'/all individuals with approved mid-year reviews"),
        MYR_RATING_BREAKDOWN_GREAT_COUNT("MyrRatingBreakdownGreatCount",
                "MyrRatingBreakdownGreatCount", INTEGER,
                "Number of individuals with approved overall rating 'Great' individuals count"),

        MYR_RATING_BREAKDOWN_OUTSTANDING_PERCENTAGE("MyrRatingBreakdownOutstandingPercentage",
                "MyrRatingBreakdownOutstandingPercentage", INTEGER,
                "Number of individuals with approved overall rating 'Outstanding'/all individuals with approved mid-year reviews"),
        MYR_RATING_BREAKDOWN_OUTSTANDING_COUNT("MyrRatingBreakdownOutstandingCount",
                "MyrRatingBreakdownOutstandingCount", INTEGER,
                "Number of individuals with approved overall rating 'Outstanding' individuals count"),


        // eyr ratings

        EYR_RATING_BREAKDOWN_BELOW_EXPECTED_PERCENTAGE("EyrRatingBreakdownBelowExpectedPercentage",
                "EyrRatingBreakdownBelowExpectedPercentage", INTEGER,
                "Number of individuals with approved overall rating 'Below expected'/all individuals with approved end-year reviews"),
        EYR_RATING_BREAKDOWN_BELOW_EXPECTED_COUNT("EyrRatingBreakdownBelowExpectedCount",
                "EyrRatingBreakdownBelowExpectedCount", INTEGER,
                "Number of individuals with approved overall rating 'Below expected' individuals count"),

        EYR_RATING_BREAKDOWN_SATISFACTORY_PERCENTAGE("EyrRatingBreakdownSatisfactoryPercentage",
                "EyrRatingBreakdownSatisfactoryPercentage", INTEGER,
                "Number of individuals with approved overall rating 'Satisfactory'/all individuals with approved end-year reviews"),
        EYR_RATING_BREAKDOWN_SATISFACTORY_COUNT("EyrRatingBreakdownSatisfactoryCount",
                "EyrRatingBreakdownSatisfactoryCount", INTEGER,
                "Number of individuals with approved overall rating 'Satisfactory' individuals count"),

        EYR_RATING_BREAKDOWN_GREAT_PERCENTAGE("EyrRatingBreakdownGreatPercentage",
                "EyrRatingBreakdownGreatPercentage", INTEGER,
                "Number of individuals with approved overall rating 'Great'/all individuals with approved end-year reviews"),
        EYR_RATING_BREAKDOWN_GREAT_COUNT("EyrRatingBreakdownGreatCount",
                "EyrRatingBreakdownGreatCount", INTEGER,
                "Number of individuals with approved overall rating 'Great' individuals count"),

        EYR_RATING_BREAKDOWN_OUTSTANDING_PERCENTAGE("EyrRatingBreakdownOutstandingPercentage",
                "EyrRatingBreakdownOutstandingPercentage", INTEGER,
                "Number of individuals with approved overall rating 'Outstanding'/all individuals with approved end-year reviews"),
        EYR_RATING_BREAKDOWN_OUTSTANDING_COUNT("EyrRatingBreakdownOutstandingCount",
                "EyrRatingBreakdownOutstandingCount", INTEGER,
                "Number of individuals with approved overall rating 'Outstanding' individuals count"),

        NEW_TO_BUSINESS_COUNT("NewToBusinessCount",
                "NewToBusinessCount", INTEGER,
                "Number of individuals who have joined the business in the last 90 days"),

        ANNIVERSARY_REVIEW_PER_QUARTER_1_PERCENTAGE("AnniversaryReviewPerQuarter1Percentage",
                "AnniversaryReviewPerQuarter1Percentage", INTEGER,
                "Number of individuals with approved anniversary reviews in Quarter 1/all individuals with approved anniversary reviews"),

        ANNIVERSARY_REVIEW_PER_QUARTER_1_COUNT("AnniversaryReviewPerQuarter1Count",
                "AnniversaryReviewPerQuarter1Count", INTEGER,
                "Number of individuals with approved anniversary reviews in Quarter 1"),

        ANNIVERSARY_REVIEW_PER_QUARTER_2_PERCENTAGE("AnniversaryReviewPerQuarter2Percentage",
                "AnniversaryReviewPerQuarter2Percentage", INTEGER,
                "Number of individuals with approved anniversary reviews in Quarter 2/all individuals with approved anniversary reviews"),

        ANNIVERSARY_REVIEW_PER_QUARTER_2_COUNT("AnniversaryReviewPerQuarter2Count",
                "AnniversaryReviewPerQuarter2Count", INTEGER,
                "Number of individuals with approved anniversary reviews in Quarter 2"),

        ANNIVERSARY_REVIEW_PER_QUARTER_3_PERCENTAGE("AnniversaryReviewPerQuarter3Percentage",
                "AnniversaryReviewPerQuarter3Percentage", INTEGER,
                "Number of individuals with approved anniversary reviews in Quarter 3/all individuals with approved anniversary reviews"),

        ANNIVERSARY_REVIEW_PER_QUARTER_3_COUNT("AnniversaryReviewPerQuarter3Count",
                "AnniversaryReviewPerQuarter3Count", INTEGER,
                "Number of individuals with approved anniversary reviews in Quarter 3"),

        ANNIVERSARY_REVIEW_PER_QUARTER_4_PERCENTAGE("AnniversaryReviewPerQuarter4Percentage",
                "AnniversaryReviewPerQuarter4Percentage", INTEGER,
                "Number of individuals with approved anniversary reviews in Quarter 4/all individuals with approved anniversary reviews"),

        ANNIVERSARY_REVIEW_PER_QUARTER_4_COUNT("AnniversaryReviewPerQuarter4Count",
                "AnniversaryReviewPerQuarter4Count", INTEGER,
                "Number of individuals with approved anniversary reviews in Quarter 4");


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
                OBJECTIVES_APPROVED_PERCENTAGE.getColumnMetadata(),
                MYR_SUBMITTED_PERCENTAGE.getColumnMetadata(),
                MYR_APPROVED_PERCENTAGE.getColumnMetadata(),
                EYR_SUBMITTED_PERCENTAGE.getColumnMetadata(),
                EYR_APPROVED_PERCENTAGE.getColumnMetadata(),

                MYR_RATING_BREAKDOWN_BELOW_EXPECTED_PERCENTAGE.getColumnMetadata(),
                MYR_RATING_BREAKDOWN_BELOW_EXPECTED_COUNT.getColumnMetadata(),
                MYR_RATING_BREAKDOWN_SATISFACTORY_PERCENTAGE.getColumnMetadata(),
                MYR_RATING_BREAKDOWN_SATISFACTORY_COUNT.getColumnMetadata(),
                MYR_RATING_BREAKDOWN_GREAT_PERCENTAGE.getColumnMetadata(),
                MYR_RATING_BREAKDOWN_GREAT_COUNT.getColumnMetadata(),
                MYR_RATING_BREAKDOWN_OUTSTANDING_PERCENTAGE.getColumnMetadata(),
                MYR_RATING_BREAKDOWN_OUTSTANDING_COUNT.getColumnMetadata(),

                EYR_RATING_BREAKDOWN_BELOW_EXPECTED_PERCENTAGE.getColumnMetadata(),
                EYR_RATING_BREAKDOWN_BELOW_EXPECTED_COUNT.getColumnMetadata(),
                EYR_RATING_BREAKDOWN_SATISFACTORY_PERCENTAGE.getColumnMetadata(),
                EYR_RATING_BREAKDOWN_SATISFACTORY_COUNT.getColumnMetadata(),
                EYR_RATING_BREAKDOWN_GREAT_PERCENTAGE.getColumnMetadata(),
                EYR_RATING_BREAKDOWN_GREAT_COUNT.getColumnMetadata(),
                EYR_RATING_BREAKDOWN_OUTSTANDING_PERCENTAGE.getColumnMetadata(),
                EYR_RATING_BREAKDOWN_OUTSTANDING_COUNT.getColumnMetadata(),

                NEW_TO_BUSINESS_COUNT.getColumnMetadata(),

                ANNIVERSARY_REVIEW_PER_QUARTER_1_PERCENTAGE.getColumnMetadata(),
                ANNIVERSARY_REVIEW_PER_QUARTER_1_COUNT.getColumnMetadata(),
                ANNIVERSARY_REVIEW_PER_QUARTER_2_PERCENTAGE.getColumnMetadata(),
                ANNIVERSARY_REVIEW_PER_QUARTER_2_COUNT.getColumnMetadata(),
                ANNIVERSARY_REVIEW_PER_QUARTER_3_PERCENTAGE.getColumnMetadata(),
                ANNIVERSARY_REVIEW_PER_QUARTER_3_COUNT.getColumnMetadata(),
                ANNIVERSARY_REVIEW_PER_QUARTER_4_PERCENTAGE.getColumnMetadata(),
                ANNIVERSARY_REVIEW_PER_QUARTER_4_COUNT.getColumnMetadata()
        );
    }
}