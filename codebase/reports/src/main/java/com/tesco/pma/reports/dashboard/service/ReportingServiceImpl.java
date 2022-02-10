package com.tesco.pma.reports.dashboard.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.error.ErrorCodeAware;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.reporting.Report;
import com.tesco.pma.reports.dashboard.dao.ReportingDAO;
import com.tesco.pma.reports.dashboard.domain.RatingStatsData;
import com.tesco.pma.reports.dashboard.domain.StatsData;
import com.tesco.pma.reports.review.domain.provider.ObjectiveLinkedReviewReportProvider;
import com.tesco.pma.reports.dashboard.domain.provider.StatsReportProvider;
import com.tesco.pma.reports.dashboard.domain.ColleagueReportTargeting;
import com.tesco.pma.reports.rating.service.RatingService;
import com.tesco.pma.reports.exception.ErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import static org.springframework.util.CollectionUtils.isEmpty;

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
    public Report getLinkedObjectivesReport(RequestQuery requestQuery) {
        var reportProvider = new ObjectiveLinkedReviewReportProvider();
        reportProvider.setObjectives(reportingDAO.getLinkedObjectivesData(requestQuery));

        if (isEmpty(reportProvider.getObjectives())) {
            throw notFound(ErrorCodes.REPORT_NOT_FOUND, Map.of(QUERY_PARAMS, requestQuery));
        }
        return reportProvider.getReport();
    }

    @Override
    public List<ColleagueReportTargeting> getReportColleagues(RequestQuery requestQuery) {
        return Optional.ofNullable(reportingDAO.getColleagueTargeting(requestQuery))
                .orElseThrow(() -> notFound(ErrorCodes.REPORT_NOT_FOUND, Map.of(QUERY_PARAMS, requestQuery)));
    }

    @Override
    public Report getStatsReport(RequestQuery requestQuery) {
        var targetingColleagues = getReportColleagues(requestQuery);
        var statsReportProvider = new StatsReportProvider();
        statsReportProvider.setData(findReviewStatsData(targetingColleagues, requestQuery));

        return statsReportProvider.getReport();
    }

    private List<StatsData> findReviewStatsData(List<ColleagueReportTargeting> colleagues, RequestQuery requestQuery) {
        var reviewStatsData = new StatsData();
        // objectives
        final var objectivesToSubmitCount = getCountWithTag(colleagues, MUST_CREATE_OBJECTIVE);
        if (objectivesToSubmitCount != 0) {
            var objectivesSubmittedPercentage =
                    (int) (100 * getCountWithTag(colleagues, HAS_OBJECTIVE_SUBMITTED) / objectivesToSubmitCount);
            reviewStatsData.setObjectivesSubmittedPercentage(objectivesSubmittedPercentage);

            var objectivesApprovedPercentage =
                    (int) (100 * getCountWithTag(colleagues, HAS_OBJECTIVE_APPROVED) / objectivesToSubmitCount);
            reviewStatsData.setObjectivesApprovedPercentage(objectivesApprovedPercentage);
        }
        // myr forms submitted, approved
        final var myrToSubmitCount = getCountWithTag(colleagues, MUST_CREATE_MYR);
        final var myrApprovedCount = getCountWithTag(colleagues, HAS_MYR_APPROVED);
        if (myrToSubmitCount != 0) {
            var myrSubmittedPercentage = (int) (100 * getCountWithTag(colleagues, HAS_MYR_SUBMITTED) / myrToSubmitCount);
            reviewStatsData.setMyrSubmittedPercentage(myrSubmittedPercentage);

            var myrApprovedPercentage = (int) (100 * myrApprovedCount / myrToSubmitCount);
            reviewStatsData.setMyrApprovedPercentage(myrApprovedPercentage);
        }
        // eyr forms submitted, approved
        final var eyrToSubmitCount = getCountWithTag(colleagues, MUST_CREATE_EYR);
        final var eyrApprovedCount = getCountWithTag(colleagues, HAS_EYR_APPROVED);
        if (eyrToSubmitCount != 0) {
            var eyrSubmittedPercentage = (int) (100 * getCountWithTag(colleagues, HAS_EYR_SUBMITTED) / eyrToSubmitCount);
            reviewStatsData.setEyrSubmittedPercentage(eyrSubmittedPercentage);

            var eyrApprovedPercentage = (int) (100 * eyrApprovedCount / eyrToSubmitCount);
            reviewStatsData.setEyrApprovedPercentage(eyrApprovedPercentage);
        }
        // ratings
        if (myrApprovedCount != 0) {
            fillMyrRatings(reviewStatsData, colleagues, myrApprovedCount);
        }
        if (eyrApprovedCount != 0) {
            fillEyrRatings(reviewStatsData, colleagues, eyrApprovedCount);
        }
        // new to business
        reviewStatsData.setNewToBusinessCount(getCountWithTag(colleagues, IS_NEW_TO_BUSINESS));
        // anniversary reviews completed per quarter
        var colleaguesAnniversary = reportingDAO.getColleagueTargetingAnniversary(requestQuery);
        if (!CollectionUtils.isEmpty(colleaguesAnniversary)) {
            fillAnniversaryReviewPerQuarters(reviewStatsData, colleaguesAnniversary);
        }

        return List.of(reviewStatsData);
    }

    private void fillAnniversaryReviewPerQuarters(StatsData statsData, List<ColleagueReportTargeting> colleaguesAnniversary) {
        final var anniversaryCount = colleaguesAnniversary.size();

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

    private void fillMyrRatings(StatsData statsData, List<ColleagueReportTargeting> colleagues, long myrApprovedCount) {
        var myrRatingBelowExpected = getMyrRatingTypeStats(colleagues, RatingStatsData.OverallRating.BELOW_EXPECTED, myrApprovedCount);
        statsData.setMyrRatingBreakdownBelowExpectedPercentage(myrRatingBelowExpected.getRatingPercentage());
        statsData.setMyrRatingBreakdownBelowExpectedCount(myrRatingBelowExpected.getRatingCount());

        var myrRatingSatisfactory = getMyrRatingTypeStats(colleagues, RatingStatsData.OverallRating.SATISFACTORY, myrApprovedCount);
        statsData.setMyrRatingBreakdownSatisfactoryPercentage(myrRatingSatisfactory.getRatingPercentage());
        statsData.setMyrRatingBreakdownSatisfactoryCount(myrRatingSatisfactory.getRatingCount());

        var myrRatingGreat = getMyrRatingTypeStats(colleagues, RatingStatsData.OverallRating.GREAT, myrApprovedCount);
        statsData.setMyrRatingBreakdownGreatPercentage(myrRatingGreat.getRatingPercentage());
        statsData.setMyrRatingBreakdownGreatCount(myrRatingGreat.getRatingCount());

        var myrRatingOutstanding = getMyrRatingTypeStats(colleagues, RatingStatsData.OverallRating.OUTSTANDING, myrApprovedCount);
        statsData.setMyrRatingBreakdownOutstandingPercentage(myrRatingOutstanding.getRatingPercentage());
        statsData.setMyrRatingBreakdownOutstandingCount(myrRatingOutstanding.getRatingCount());
    }

    private void fillEyrRatings(StatsData statsData, List<ColleagueReportTargeting> colleagues, long eyrApprovedCount) {
        var eyrRatingBelowExpected = getEyrRatingTypeStats(colleagues, RatingStatsData.OverallRating.BELOW_EXPECTED, eyrApprovedCount);
        statsData.setEyrRatingBreakdownBelowExpectedPercentage(eyrRatingBelowExpected.getRatingPercentage());
        statsData.setEyrRatingBreakdownBelowExpectedCount(eyrRatingBelowExpected.getRatingCount());

        var eyrRatingSatisfactory = getEyrRatingTypeStats(colleagues, RatingStatsData.OverallRating.SATISFACTORY, eyrApprovedCount);
        statsData.setEyrRatingBreakdownSatisfactoryPercentage(eyrRatingSatisfactory.getRatingPercentage());
        statsData.setEyrRatingBreakdownSatisfactoryCount(eyrRatingSatisfactory.getRatingCount());

        var eyrRatingGreat = getEyrRatingTypeStats(colleagues, RatingStatsData.OverallRating.GREAT, eyrApprovedCount);
        statsData.setEyrRatingBreakdownGreatPercentage(eyrRatingGreat.getRatingPercentage());
        statsData.setEyrRatingBreakdownGreatCount(eyrRatingGreat.getRatingCount());

        var eyrRatingOutstanding = getEyrRatingTypeStats(colleagues, RatingStatsData.OverallRating.OUTSTANDING, eyrApprovedCount);
        statsData.setEyrRatingBreakdownOutstandingPercentage(eyrRatingOutstanding.getRatingPercentage());
        statsData.setEyrRatingBreakdownOutstandingCount(eyrRatingOutstanding.getRatingCount());
    }

    private RatingStatsData getMyrRatingTypeStats(List<ColleagueReportTargeting> colleagues,
                                                  RatingStatsData.OverallRating ratingType, long myrRatingToSubmitCount) {
        return getRatingTypeStats(colleagues, MYR_WHAT_RATING, MYR_HOW_RATING,
                ratingType, myrRatingToSubmitCount);
    }

    private RatingStatsData getEyrRatingTypeStats(List<ColleagueReportTargeting> colleagues,
                                                  RatingStatsData.OverallRating ratingType, long eyrRatingToSubmitCount) {
        return getRatingTypeStats(colleagues, EYR_WHAT_RATING, EYR_HOW_RATING,
                ratingType, eyrRatingToSubmitCount);
    }

    private RatingStatsData getRatingTypeStats(List<ColleagueReportTargeting> colleagues,
                                               String whatRatingTag, String howRatingTag,
                                               RatingStatsData.OverallRating ratingType, long ratingToSubmitCount) {
        var ratingStats = new RatingStatsData();
        var ratingTypeCount = getRatingCountWithTag(colleagues, ratingType.getDescription(), whatRatingTag, howRatingTag);
        var ratingTypePercentage = (int) (100 * ratingTypeCount / ratingToSubmitCount);

        ratingStats.setRatingPercentage(ratingTypePercentage);
        ratingStats.setRatingCount(ratingTypeCount);

        return ratingStats;
    }

    private long getRatingCountWithTag(List<ColleagueReportTargeting> colleagues, String rating,
                                       String whatRatingTag, String howRatingTag) {
        return colleagues.stream()
                .filter(c -> rating.equalsIgnoreCase(getOverallRating(c, whatRatingTag, howRatingTag)))
                .count();
    }

    private String getOverallRating(ColleagueReportTargeting colleague, String whatRatingTag, String howRatingTag) {
        return ratingService.getOverallRating(colleague.getTags().get(whatRatingTag), colleague.getTags().get(howRatingTag));
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