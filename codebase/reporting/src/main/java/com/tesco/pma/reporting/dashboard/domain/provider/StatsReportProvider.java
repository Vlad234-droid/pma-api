package com.tesco.pma.reporting.dashboard.domain.provider;

import com.tesco.pma.api.ValueType;
import com.tesco.pma.reporting.ReportMetadata;
import com.tesco.pma.reporting.Reportable;
import com.tesco.pma.reporting.metadata.ColumnMetadata;
import com.tesco.pma.reporting.dashboard.domain.StatsData;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.stream.Collectors;

import static com.tesco.pma.api.ValueType.INTEGER;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.COLLEAGUES_COUNT;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.OBJECTIVES_SUBMITTED_PERCENTAGE;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.OBJECTIVES_APPROVED_PERCENTAGE;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.MYR_SUBMITTED_PERCENTAGE;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.MYR_APPROVED_PERCENTAGE;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.EYR_SUBMITTED_PERCENTAGE;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.EYR_APPROVED_PERCENTAGE;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.MYR_RATING_BREAKDOWN_BELOW_EXPECTED_PERCENTAGE;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.MYR_RATING_BREAKDOWN_BELOW_EXPECTED_COUNT;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.MYR_RATING_BREAKDOWN_SATISFACTORY_PERCENTAGE;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.MYR_RATING_BREAKDOWN_SATISFACTORY_COUNT;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.MYR_RATING_BREAKDOWN_GREAT_PERCENTAGE;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.MYR_RATING_BREAKDOWN_GREAT_COUNT;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.MYR_RATING_BREAKDOWN_OUTSTANDING_PERCENTAGE;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.MYR_RATING_BREAKDOWN_OUTSTANDING_COUNT;

import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.EYR_RATING_BREAKDOWN_BELOW_EXPECTED_PERCENTAGE;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.EYR_RATING_BREAKDOWN_BELOW_EXPECTED_COUNT;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.EYR_RATING_BREAKDOWN_SATISFACTORY_PERCENTAGE;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.EYR_RATING_BREAKDOWN_SATISFACTORY_COUNT;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.EYR_RATING_BREAKDOWN_GREAT_PERCENTAGE;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.EYR_RATING_BREAKDOWN_GREAT_COUNT;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.EYR_RATING_BREAKDOWN_OUTSTANDING_PERCENTAGE;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.EYR_RATING_BREAKDOWN_OUTSTANDING_COUNT;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.NEW_TO_BUSINESS_COUNT;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.ANNIVERSARY_REVIEW_PER_QUARTER_1_PERCENTAGE;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.ANNIVERSARY_REVIEW_PER_QUARTER_1_COUNT;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.ANNIVERSARY_REVIEW_PER_QUARTER_2_PERCENTAGE;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.ANNIVERSARY_REVIEW_PER_QUARTER_2_COUNT;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.ANNIVERSARY_REVIEW_PER_QUARTER_3_PERCENTAGE;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.ANNIVERSARY_REVIEW_PER_QUARTER_3_COUNT;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.ANNIVERSARY_REVIEW_PER_QUARTER_4_PERCENTAGE;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.ANNIVERSARY_REVIEW_PER_QUARTER_4_COUNT;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.FEEDBACK_REQUESTED_PERCENTAGE;
import static com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider.ColumnMetadataEnum.FEEDBACK_GIVEN_PERCENTAGE;

/**
 * Statistics Report with data, metadata
 */
@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StatsReportProvider implements Reportable {

    public static final String REPORT_NAME = "statistics-report";
    public static final String REPORT_DESCRIPTION = "Statistics Report";
    public static final String REPORT_FILE_NAME = "StatisticsReport.xlsx";
    public static final String REPORT_SHEET_NAME = "Report";

    enum ColumnMetadataEnum {
        COLLEAGUES_COUNT("colleagues-count",
                "Colleagues count", INTEGER,
                "Total number of individuals"),
        OBJECTIVES_SUBMITTED_PERCENTAGE("objectives-submitted-percentage",
                "Objectives submitted percentage", INTEGER,
                "Number of individuals with submitted objectives/all individuals with objectives to submit"),
        OBJECTIVES_APPROVED_PERCENTAGE("objectives-approved-percentage",
                "Objectives approved percentage", INTEGER,
                "Number of individuals with approved objectives/all individuals with objectives to submit"),

        MYR_SUBMITTED_PERCENTAGE("myr-submitted-percentage",
                "Mid-year forms submitted percentage", INTEGER,
                "Number of individuals with submitted mid-year reviews/all individuals with reviews to submit"),
        MYR_APPROVED_PERCENTAGE("myr-approved-percentage",
                "Mid-year forms approved percentage", INTEGER,
                "Number of individuals with approved mid-year reviews/all individuals with reviews to submit"),

        MYR_RATING_BREAKDOWN_BELOW_EXPECTED_PERCENTAGE("myr-rating-breakdown-below-expected-percentage",
                "Mid-year rating breakdown Below expected percentage", INTEGER,
                "Number of individuals with approved overall rating 'Below expected'/all individuals with approved mid-year reviews"),
        MYR_RATING_BREAKDOWN_BELOW_EXPECTED_COUNT("myr-rating-breakdown-below-expected-count",
                "Mid-year rating breakdown Below expected count", INTEGER,
                "Number of individuals with approved overall rating 'Below expected' individuals count"),

        MYR_RATING_BREAKDOWN_SATISFACTORY_PERCENTAGE("myr-rating-breakdown-satisfactory-percentage",
                "Mid-year rating breakdown Satisfactory percentage", INTEGER,
                "Number of individuals with approved overall rating 'Satisfactory'/all individuals with approved mid-year reviews"),
        MYR_RATING_BREAKDOWN_SATISFACTORY_COUNT("myr-rating-breakdown-satisfactory-count",
                "Mid-year rating breakdown Satisfactory count", INTEGER,
                "Number of individuals with approved overall rating 'Satisfactory' individuals count"),

        MYR_RATING_BREAKDOWN_GREAT_PERCENTAGE("myr-rating-breakdown-great-percentage",
                "Mid-year rating breakdown Great percentage", INTEGER,
                "Number of individuals with approved overall rating 'Great'/all individuals with approved mid-year reviews"),
        MYR_RATING_BREAKDOWN_GREAT_COUNT("myr-rating-breakdown-great-count",
                "Mid-year rating breakdown Great count", INTEGER,
                "Number of individuals with approved overall rating 'Great' individuals count"),

        MYR_RATING_BREAKDOWN_OUTSTANDING_PERCENTAGE("myr-rating-breakdown-outstanding-percentage",
                "Mid-year rating breakdown Outstanding percentage", INTEGER,
                "Number of individuals with approved overall rating 'Outstanding'/all individuals with approved mid-year reviews"),
        MYR_RATING_BREAKDOWN_OUTSTANDING_COUNT("myr-rating-breakdown-outstanding-count",
                "Mid-year rating breakdown Outstanding count", INTEGER,
                "Number of individuals with approved overall rating 'Outstanding' individuals count"),


        EYR_SUBMITTED_PERCENTAGE("eyr-submitted-percentage",
                "End-year forms submitted percentage", INTEGER,
                "Number of individuals with submitted end-year reviews/all individuals with reviews to submit"),
        EYR_APPROVED_PERCENTAGE("eyr-approved-percentage",
                "End-year forms approved percentage", INTEGER,
                "Number of individuals with approved end-year reviews/all individuals with reviews to submit"),

        EYR_RATING_BREAKDOWN_BELOW_EXPECTED_PERCENTAGE("eyr-rating-breakdown-below-expected-percentage",
                "End-year rating breakdown Below expected percentage", INTEGER,
                "Number of individuals with approved overall rating 'Below expected'/all individuals with approved end-year reviews"),
        EYR_RATING_BREAKDOWN_BELOW_EXPECTED_COUNT("eyr-rating-breakdown-below-expected-count",
                "End-year rating breakdown Below expected count", INTEGER,
                "Number of individuals with approved overall rating 'Below expected' individuals count"),

        EYR_RATING_BREAKDOWN_SATISFACTORY_PERCENTAGE("eyr-rating-breakdown-satisfactory-percentage",
                "End-year rating breakdown Satisfactory percentage", INTEGER,
                "Number of individuals with approved overall rating 'Satisfactory'/all individuals with approved end-year reviews"),
        EYR_RATING_BREAKDOWN_SATISFACTORY_COUNT("eyr-rating-breakdown-satisfactory-count",
                "End-year rating breakdown Satisfactory count", INTEGER,
                "Number of individuals with approved overall rating 'Satisfactory' individuals count"),

        EYR_RATING_BREAKDOWN_GREAT_PERCENTAGE("eyr-rating-breakdown-great-percentage",
                "End-year rating breakdown Great percentage", INTEGER,
                "Number of individuals with approved overall rating 'Great'/all individuals with approved end-year reviews"),
        EYR_RATING_BREAKDOWN_GREAT_COUNT("eyr-rating-breakdown-great-count",
                "End-year rating breakdown Great count", INTEGER,
                "Number of individuals with approved overall rating 'Great' individuals count"),

        EYR_RATING_BREAKDOWN_OUTSTANDING_PERCENTAGE("eyr-rating-breakdown-outstanding-percentage",
                "End-year rating breakdown Outstanding percentage", INTEGER,
                "Number of individuals with approved overall rating 'Outstanding'/all individuals with approved end-year reviews"),
        EYR_RATING_BREAKDOWN_OUTSTANDING_COUNT("eyr-rating-breakdown-outstanding-count",
                "End-year rating breakdown Outstanding count", INTEGER,
                "Number of individuals with approved overall rating 'Outstanding' individuals count"),

        FEEDBACK_REQUESTED_PERCENTAGE("feedback-requested-percentage",
                "In the moment Feedback requested percentage", INTEGER,
                "Number of individuals requesting feedback/all individuals"),

        FEEDBACK_GIVEN_PERCENTAGE("feedback-given-percentage",
                "In the moment Feedback given percentage", INTEGER,
                "Number of individuals giving feedback/all individuals"),

        NEW_TO_BUSINESS_COUNT("new-to-business-count",
                "New to business count", INTEGER,
                "Number of individuals who have joined the business in the last 90 days"),

        ANNIVERSARY_REVIEW_PER_QUARTER_1_PERCENTAGE("anniversary-review-per-quarter-1-percentage",
                "Anniversary reviews completed per quarter 1 percentage", INTEGER,
                "Number of individuals with approved anniversary reviews in Quarter 1/all individuals with approved anniversary reviews"),

        ANNIVERSARY_REVIEW_PER_QUARTER_1_COUNT("anniversary-review-per-quarter-1-count",
                "Anniversary reviews completed per quarter 1 count", INTEGER,
                "Number of individuals with approved anniversary reviews in Quarter 1"),

        ANNIVERSARY_REVIEW_PER_QUARTER_2_PERCENTAGE("anniversary-review-per-quarter-2-percentage",
                "Anniversary reviews completed per quarter 2 percentage", INTEGER,
                "Number of individuals with approved anniversary reviews in Quarter 2/all individuals with approved anniversary reviews"),

        ANNIVERSARY_REVIEW_PER_QUARTER_2_COUNT("anniversary-review-per-quarter-2-count",
                "Anniversary reviews completed per quarter 2 count", INTEGER,
                "Number of individuals with approved anniversary reviews in Quarter 2"),

        ANNIVERSARY_REVIEW_PER_QUARTER_3_PERCENTAGE("anniversary-review-per-quarter-3-percentage",
                "Anniversary reviews completed per quarter 3 percentage", INTEGER,
                "Number of individuals with approved anniversary reviews in Quarter 3/all individuals with approved anniversary reviews"),

        ANNIVERSARY_REVIEW_PER_QUARTER_3_COUNT("anniversary-review-per-quarter-3-count",
                "Anniversary reviews completed per quarter 3 count", INTEGER,
                "Number of individuals with approved anniversary reviews in Quarter 3"),

        ANNIVERSARY_REVIEW_PER_QUARTER_4_PERCENTAGE("anniversary-review-per-quarter-4-percentage",
                "Anniversary reviews completed per quarter 4 percentage", INTEGER,
                "Number of individuals with approved anniversary reviews in Quarter 4/all individuals with approved anniversary reviews"),

        ANNIVERSARY_REVIEW_PER_QUARTER_4_COUNT("anniversary-review-per-quarter-4-count",
                "Anniversary reviews completed per quarter 4 count", INTEGER,
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

    List<StatsData> data;

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
                COLLEAGUES_COUNT.getColumnMetadata(),

                OBJECTIVES_SUBMITTED_PERCENTAGE.getColumnMetadata(),
                OBJECTIVES_APPROVED_PERCENTAGE.getColumnMetadata(),

                MYR_SUBMITTED_PERCENTAGE.getColumnMetadata(),
                MYR_APPROVED_PERCENTAGE.getColumnMetadata(),
                MYR_RATING_BREAKDOWN_BELOW_EXPECTED_PERCENTAGE.getColumnMetadata(),
                MYR_RATING_BREAKDOWN_BELOW_EXPECTED_COUNT.getColumnMetadata(),
                MYR_RATING_BREAKDOWN_SATISFACTORY_PERCENTAGE.getColumnMetadata(),
                MYR_RATING_BREAKDOWN_SATISFACTORY_COUNT.getColumnMetadata(),
                MYR_RATING_BREAKDOWN_GREAT_PERCENTAGE.getColumnMetadata(),
                MYR_RATING_BREAKDOWN_GREAT_COUNT.getColumnMetadata(),
                MYR_RATING_BREAKDOWN_OUTSTANDING_PERCENTAGE.getColumnMetadata(),
                MYR_RATING_BREAKDOWN_OUTSTANDING_COUNT.getColumnMetadata(),

                EYR_SUBMITTED_PERCENTAGE.getColumnMetadata(),
                EYR_APPROVED_PERCENTAGE.getColumnMetadata(),
                EYR_RATING_BREAKDOWN_BELOW_EXPECTED_PERCENTAGE.getColumnMetadata(),
                EYR_RATING_BREAKDOWN_BELOW_EXPECTED_COUNT.getColumnMetadata(),
                EYR_RATING_BREAKDOWN_SATISFACTORY_PERCENTAGE.getColumnMetadata(),
                EYR_RATING_BREAKDOWN_SATISFACTORY_COUNT.getColumnMetadata(),
                EYR_RATING_BREAKDOWN_GREAT_PERCENTAGE.getColumnMetadata(),
                EYR_RATING_BREAKDOWN_GREAT_COUNT.getColumnMetadata(),
                EYR_RATING_BREAKDOWN_OUTSTANDING_PERCENTAGE.getColumnMetadata(),
                EYR_RATING_BREAKDOWN_OUTSTANDING_COUNT.getColumnMetadata(),

                FEEDBACK_REQUESTED_PERCENTAGE.getColumnMetadata(),
                FEEDBACK_GIVEN_PERCENTAGE.getColumnMetadata(),

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