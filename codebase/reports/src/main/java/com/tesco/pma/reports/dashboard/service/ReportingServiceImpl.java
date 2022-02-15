package com.tesco.pma.reports.dashboard.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.error.ErrorCodeAware;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.reporting.Report;
import com.tesco.pma.reports.dashboard.dao.ReportingDAO;
import com.tesco.pma.reports.dashboard.domain.RatingStatsData;
import com.tesco.pma.reports.dashboard.domain.StatsData;
import com.tesco.pma.reports.dashboard.domain.provider.StatsReportProvider;
import com.tesco.pma.reports.dashboard.domain.ColleagueReportTargeting;
import com.tesco.pma.reports.rating.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.tesco.pma.reports.ReportingConstants.BELOW_EXPECTED_RATING;
import static com.tesco.pma.reports.ReportingConstants.EYR_OVERALL_RATING;
import static com.tesco.pma.reports.ReportingConstants.GREAT_RATING;
import static com.tesco.pma.reports.ReportingConstants.MYR_OVERALL_RATING;
import static com.tesco.pma.reports.ReportingConstants.OUTSTANDING_RATING;
import static com.tesco.pma.reports.ReportingConstants.SATISFACTORY_RATING;
import static com.tesco.pma.reports.ReportingConstants.QUERY_PARAMS;
import static com.tesco.pma.reports.ReportingConstants.EYR_HOW_RATING;
import static com.tesco.pma.reports.ReportingConstants.EYR_WHAT_RATING;
import static com.tesco.pma.reports.ReportingConstants.HAS_EYR_APPROVED;
import static com.tesco.pma.reports.ReportingConstants.HAS_EYR_APPROVED_1_QUARTER;
import static com.tesco.pma.reports.ReportingConstants.HAS_EYR_APPROVED_2_QUARTER;
import static com.tesco.pma.reports.ReportingConstants.HAS_EYR_APPROVED_3_QUARTER;
import static com.tesco.pma.reports.ReportingConstants.HAS_EYR_APPROVED_4_QUARTER;
import static com.tesco.pma.reports.ReportingConstants.HAS_EYR_SUBMITTED;
import static com.tesco.pma.reports.ReportingConstants.HAS_MYR_APPROVED;
import static com.tesco.pma.reports.ReportingConstants.HAS_MYR_SUBMITTED;
import static com.tesco.pma.reports.ReportingConstants.HAS_OBJECTIVE_APPROVED;
import static com.tesco.pma.reports.ReportingConstants.HAS_OBJECTIVE_SUBMITTED;
import static com.tesco.pma.reports.ReportingConstants.IS_NEW_TO_BUSINESS;
import static com.tesco.pma.reports.ReportingConstants.MUST_CREATE_EYR;
import static com.tesco.pma.reports.ReportingConstants.MUST_CREATE_MYR;
import static com.tesco.pma.reports.ReportingConstants.MUST_CREATE_OBJECTIVE;
import static com.tesco.pma.reports.ReportingConstants.MYR_HOW_RATING;
import static com.tesco.pma.reports.ReportingConstants.MYR_WHAT_RATING;
import static com.tesco.pma.reports.ReportingConstants.HAS_FEEDBACK_REQUESTED;
import static com.tesco.pma.reports.ReportingConstants.HAS_FEEDBACK_GIVEN;
import static com.tesco.pma.reports.exception.ErrorCodes.REPORT_NOT_FOUND;

/**
 * Service for reporting data
 */
@Service
@Validated
@RequiredArgsConstructor
public class ReportingServiceImpl implements ReportingService {
    private final ReportingDAO reportingDAO;
    private final RatingService ratingService;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Override
    public List<ColleagueReportTargeting> getReportColleagues(RequestQuery requestQuery) {
        var targetingColleagues = Optional.ofNullable(reportingDAO.getColleagueTargeting(requestQuery))
                .orElseThrow(() -> notFound(REPORT_NOT_FOUND, Map.of(QUERY_PARAMS, requestQuery)));

        targetingColleagues.forEach(c -> {
            c.getTags().put(MYR_OVERALL_RATING, getOverallRating(c, MYR_WHAT_RATING, MYR_HOW_RATING));
            c.getTags().put(EYR_OVERALL_RATING, getOverallRating(c, EYR_WHAT_RATING, EYR_HOW_RATING));
        });
        return targetingColleagues;
    }

    @Override
    public Report getStatsReport(RequestQuery requestQuery) {
        var targetingColleagues = getReportColleagues(requestQuery);
        var statsReportProvider = new StatsReportProvider();
        statsReportProvider.setData(findStatsData(targetingColleagues, requestQuery));

        return statsReportProvider.getReport();
    }

    private String getOverallRating(ColleagueReportTargeting colleague, String whatRatingTag, String howRatingTag) {
        return ratingService.getOverallRating(colleague.getTags().get(whatRatingTag), colleague.getTags().get(howRatingTag));
    }

    private List<StatsData> findStatsData(List<ColleagueReportTargeting> colleagues, RequestQuery requestQuery) {
        var statsData = new StatsData();
        statsData.setColleaguesCount(colleagues.size());
        final var myrApprovedCount = getCountWithTag(colleagues, HAS_MYR_APPROVED);
        final var eyrApprovedCount = getCountWithTag(colleagues, HAS_EYR_APPROVED);

        fillObjectives(statsData, colleagues);
        fillMyrForms(statsData, colleagues, myrApprovedCount);
        fillEyrForms(statsData, colleagues, eyrApprovedCount);

        if (myrApprovedCount != 0) {
            fillMyrRatings(statsData, colleagues, myrApprovedCount);
        }
        if (eyrApprovedCount != 0) {
            fillEyrRatings(statsData, colleagues, eyrApprovedCount);
        }

        statsData.setNewToBusinessCount(getCountWithTag(colleagues, IS_NEW_TO_BUSINESS));

        var colleaguesAnniversary = reportingDAO.getColleagueTargetingAnniversary(requestQuery);
        if (!CollectionUtils.isEmpty(colleaguesAnniversary)) {
            fillAnniversaryReviewPerQuarters(statsData, colleaguesAnniversary);
        }

        fillFeedbacks(statsData, colleagues);

        return List.of(statsData);
    }

    private void fillFeedbacks(StatsData statsData, List<ColleagueReportTargeting> colleagues) {
        var colleaguesCount = colleagues.size();
        if (colleaguesCount != 0) {
            var feedbackRequestedPercentage = (int) (100 * getCountWithTag(colleagues, HAS_FEEDBACK_REQUESTED) / colleaguesCount);
            statsData.setFeedbackRequestedPercentage(feedbackRequestedPercentage);

            var feedbackGivenPercentage = (int) (100 * getCountWithTag(colleagues, HAS_FEEDBACK_GIVEN) / colleaguesCount);
            statsData.setFeedbackGivenPercentage(feedbackGivenPercentage);
        }
    }

    private void fillObjectives(StatsData statsData, List<ColleagueReportTargeting> colleagues) {
        final var objectivesToSubmitCount = getCountWithTag(colleagues, MUST_CREATE_OBJECTIVE);
        if (objectivesToSubmitCount != 0) {
            var objectivesSubmittedPercentage =
                    (int) (100 * getCountWithTag(colleagues, HAS_OBJECTIVE_SUBMITTED) / objectivesToSubmitCount);
            statsData.setObjectivesSubmittedPercentage(objectivesSubmittedPercentage);

            var objectivesApprovedPercentage =
                    (int) (100 * getCountWithTag(colleagues, HAS_OBJECTIVE_APPROVED) / objectivesToSubmitCount);
            statsData.setObjectivesApprovedPercentage(objectivesApprovedPercentage);
        }
    }

    private void fillMyrForms(StatsData statsData, List<ColleagueReportTargeting> colleagues, long myrApprovedCount) {
        final var myrToSubmitCount = getCountWithTag(colleagues, MUST_CREATE_MYR);
        if (myrToSubmitCount != 0) {
            var myrSubmittedPercentage = (int) (100 * getCountWithTag(colleagues, HAS_MYR_SUBMITTED) / myrToSubmitCount);
            statsData.setMyrSubmittedPercentage(myrSubmittedPercentage);

            var myrApprovedPercentage = (int) (100 * myrApprovedCount / myrToSubmitCount);
            statsData.setMyrApprovedPercentage(myrApprovedPercentage);
        }
    }

    private void fillEyrForms(StatsData statsData, List<ColleagueReportTargeting> colleagues, long eyrApprovedCount) {
        final var eyrToSubmitCount = getCountWithTag(colleagues, MUST_CREATE_EYR);
        if (eyrToSubmitCount != 0) {
            var eyrSubmittedPercentage = (int) (100 * getCountWithTag(colleagues, HAS_EYR_SUBMITTED) / eyrToSubmitCount);
            statsData.setEyrSubmittedPercentage(eyrSubmittedPercentage);

            var eyrApprovedPercentage = (int) (100 * eyrApprovedCount / eyrToSubmitCount);
            statsData.setEyrApprovedPercentage(eyrApprovedPercentage);
        }
    }

    private void fillAnniversaryReviewPerQuarters(StatsData statsData, List<ColleagueReportTargeting> colleaguesAnniversary) {
        final var anniversaryCount = colleaguesAnniversary.size();

        if (anniversaryCount != 0) {
            final var anniversaryReviewPerQuarter1Count = getCountWithTag(colleaguesAnniversary, HAS_EYR_APPROVED_1_QUARTER);
            statsData.setAnniversaryReviewPerQuarter1Count(anniversaryReviewPerQuarter1Count);
            statsData.setAnniversaryReviewPerQuarter1Percentage((int) (100 * anniversaryReviewPerQuarter1Count / anniversaryCount));

            final var anniversaryReviewPerQuarter2Count = getCountWithTag(colleaguesAnniversary, HAS_EYR_APPROVED_2_QUARTER);
            statsData.setAnniversaryReviewPerQuarter2Count(anniversaryReviewPerQuarter2Count);
            statsData.setAnniversaryReviewPerQuarter2Percentage((int) (100 * anniversaryReviewPerQuarter2Count / anniversaryCount));

            final var anniversaryReviewPerQuarter3Count = getCountWithTag(colleaguesAnniversary, HAS_EYR_APPROVED_3_QUARTER);
            statsData.setAnniversaryReviewPerQuarter3Count(anniversaryReviewPerQuarter3Count);
            statsData.setAnniversaryReviewPerQuarter3Percentage((int) (100 * anniversaryReviewPerQuarter3Count / anniversaryCount));

            final var anniversaryReviewPerQuarter4Count = getCountWithTag(colleaguesAnniversary, HAS_EYR_APPROVED_4_QUARTER);
            statsData.setAnniversaryReviewPerQuarter4Count(anniversaryReviewPerQuarter4Count);
            statsData.setAnniversaryReviewPerQuarter4Percentage((int) (100 * anniversaryReviewPerQuarter4Count / anniversaryCount));
        }
    }

    private void fillMyrRatings(StatsData statsData, List<ColleagueReportTargeting> colleagues, long myrApprovedCount) {
        var myrRatingBelowExpected = getMyrRatingTypeStats(colleagues, BELOW_EXPECTED_RATING, myrApprovedCount);
        statsData.setMyrRatingBreakdownBelowExpectedPercentage(myrRatingBelowExpected.getRatingPercentage());
        statsData.setMyrRatingBreakdownBelowExpectedCount(myrRatingBelowExpected.getRatingCount());

        var myrRatingSatisfactory = getMyrRatingTypeStats(colleagues, SATISFACTORY_RATING, myrApprovedCount);
        statsData.setMyrRatingBreakdownSatisfactoryPercentage(myrRatingSatisfactory.getRatingPercentage());
        statsData.setMyrRatingBreakdownSatisfactoryCount(myrRatingSatisfactory.getRatingCount());

        var myrRatingGreat = getMyrRatingTypeStats(colleagues, GREAT_RATING, myrApprovedCount);
        statsData.setMyrRatingBreakdownGreatPercentage(myrRatingGreat.getRatingPercentage());
        statsData.setMyrRatingBreakdownGreatCount(myrRatingGreat.getRatingCount());

        var myrRatingOutstanding = getMyrRatingTypeStats(colleagues, OUTSTANDING_RATING, myrApprovedCount);
        statsData.setMyrRatingBreakdownOutstandingPercentage(myrRatingOutstanding.getRatingPercentage());
        statsData.setMyrRatingBreakdownOutstandingCount(myrRatingOutstanding.getRatingCount());
    }

    private void fillEyrRatings(StatsData statsData, List<ColleagueReportTargeting> colleagues, long eyrApprovedCount) {
        var eyrRatingBelowExpected = getEyrRatingTypeStats(colleagues, BELOW_EXPECTED_RATING, eyrApprovedCount);
        statsData.setEyrRatingBreakdownBelowExpectedPercentage(eyrRatingBelowExpected.getRatingPercentage());
        statsData.setEyrRatingBreakdownBelowExpectedCount(eyrRatingBelowExpected.getRatingCount());

        var eyrRatingSatisfactory = getEyrRatingTypeStats(colleagues, SATISFACTORY_RATING, eyrApprovedCount);
        statsData.setEyrRatingBreakdownSatisfactoryPercentage(eyrRatingSatisfactory.getRatingPercentage());
        statsData.setEyrRatingBreakdownSatisfactoryCount(eyrRatingSatisfactory.getRatingCount());

        var eyrRatingGreat = getEyrRatingTypeStats(colleagues, GREAT_RATING, eyrApprovedCount);
        statsData.setEyrRatingBreakdownGreatPercentage(eyrRatingGreat.getRatingPercentage());
        statsData.setEyrRatingBreakdownGreatCount(eyrRatingGreat.getRatingCount());

        var eyrRatingOutstanding = getEyrRatingTypeStats(colleagues, OUTSTANDING_RATING, eyrApprovedCount);
        statsData.setEyrRatingBreakdownOutstandingPercentage(eyrRatingOutstanding.getRatingPercentage());
        statsData.setEyrRatingBreakdownOutstandingCount(eyrRatingOutstanding.getRatingCount());
    }

    private RatingStatsData getMyrRatingTypeStats(List<ColleagueReportTargeting> colleagues,
                                                  String ratingType, long myrRatingToSubmitCount) {
        return getRatingTypeStats(colleagues, MYR_OVERALL_RATING, ratingType, myrRatingToSubmitCount);
    }

    private RatingStatsData getEyrRatingTypeStats(List<ColleagueReportTargeting> colleagues,
                                                  String ratingType, long eyrRatingToSubmitCount) {
        return getRatingTypeStats(colleagues, EYR_OVERALL_RATING, ratingType, eyrRatingToSubmitCount);
    }

    private RatingStatsData getRatingTypeStats(List<ColleagueReportTargeting> colleagues,
                                               String ratingTag, String ratingType, long ratingToSubmitCount) {
        var ratingStats = new RatingStatsData();
        var ratingTypeCount = getRatingCountWithTag(colleagues, ratingType, ratingTag);
        var ratingTypePercentage = (int) (100 * ratingTypeCount / ratingToSubmitCount);

        ratingStats.setRatingPercentage(ratingTypePercentage);
        ratingStats.setRatingCount(ratingTypeCount);

        return ratingStats;
    }

    private long getRatingCountWithTag(List<ColleagueReportTargeting> colleagues, String rating, String ratingTag) {
        return colleagues.stream()
                .filter(c -> rating.equalsIgnoreCase(c.getTags().get(ratingTag)))
                .count();
    }

    private long getCountWithTag(List<ColleagueReportTargeting> colleagues, String tag) {
        return colleagues.stream()
                .filter(c -> "1".equals(c.getTags().get(tag)))
                .count();
    }

    private NotFoundException notFound(ErrorCodeAware errorCode, Map<String, ?> params) {
        return new NotFoundException(
                errorCode.getCode(),
                messageSourceAccessor.getMessageForParams(errorCode.getCode(), params),
                null,
                null);
    }
}