package com.tesco.pma.reports.review.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.error.ErrorCodeAware;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.reporting.Report;
import com.tesco.pma.reports.domain.ColleagueReportTargeting;
import com.tesco.pma.reports.rating.service.RatingService;
import com.tesco.pma.reports.review.dao.ReviewReportingDAO;
import com.tesco.pma.reports.exception.ErrorCodes;
import com.tesco.pma.reports.review.domain.RatingStatsData;
import com.tesco.pma.reports.review.domain.ReviewStatsData;
import com.tesco.pma.reports.review.domain.provider.ObjectiveLinkedReviewReportProvider;
import com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider;
import com.tesco.pma.reports.review.domain.RatingStatsData.OverallRating;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.tesco.pma.reports.review.domain.RatingStatsData.OverallRating.BELOW_EXPECTED;
import static com.tesco.pma.reports.review.domain.RatingStatsData.OverallRating.SATISFACTORY;
import static com.tesco.pma.reports.review.domain.RatingStatsData.OverallRating.GREAT;
import static com.tesco.pma.reports.review.domain.RatingStatsData.OverallRating.OUTSTANDING;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Service for mapping data management
 */
@Service
@Validated
@RequiredArgsConstructor
public class ReviewReportingServiceImpl implements ReviewReportingService {
    static final String QUERY_PARAMS = "queryParams";

    private final ReviewReportingDAO reviewReportingDAO;
    private final RatingService ratingService;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Override
    public Report getLinkedObjectivesReport(RequestQuery requestQuery) {
        var reportProvider = new ObjectiveLinkedReviewReportProvider();
        reportProvider.setObjectives(reviewReportingDAO.getLinkedObjectivesData(requestQuery));

        if (isEmpty(reportProvider.getObjectives())) {
            throw notFound(ErrorCodes.REVIEW_REPORT_NOT_FOUND, Map.of(QUERY_PARAMS, requestQuery));
        }
        return reportProvider.getReport();
    }

    @Override
    public List<ColleagueReportTargeting> getReviewReportColleagues(RequestQuery requestQuery) {
        return Optional.ofNullable(reviewReportingDAO.getColleagueTargeting(requestQuery))
                .orElseThrow(() -> notFound(ErrorCodes.REVIEW_REPORT_NOT_FOUND, Map.of(QUERY_PARAMS, requestQuery)));
    }

    @Override
    public Report getReviewStatsReport(RequestQuery requestQuery) {
        var targetingColleagues = getReviewReportColleagues(requestQuery);
        var statsReportProvider = new ReviewStatsReportProvider();
        statsReportProvider.setData(findReviewStatsData(targetingColleagues, requestQuery));

        return statsReportProvider.getReport();
    }

    private List<ReviewStatsData> findReviewStatsData(List<ColleagueReportTargeting> colleagues, RequestQuery requestQuery) {
        var reviewStatsData = new ReviewStatsData();
        // objectives
        final var objectivesToSubmitCount = getCountWithTag(colleagues, "must_create_objective");
        if (objectivesToSubmitCount != 0) {
            var objectivesSubmittedPercentage =
                    (int) (100 * getCountWithTag(colleagues, "has_objective_submitted") / objectivesToSubmitCount);
            reviewStatsData.setObjectivesSubmittedPercentage(objectivesSubmittedPercentage);

            var objectivesApprovedPercentage =
                    (int) (100 * getCountWithTag(colleagues, "has_objective_approved") / objectivesToSubmitCount);
            reviewStatsData.setObjectivesApprovedPercentage(objectivesApprovedPercentage);
        }
        // myr forms submitted, approved
        final var myrToSubmitCount = getCountWithTag(colleagues, "must_create_myr");
        final var myrApprovedCount = getCountWithTag(colleagues, "has_myr_approved");
        if (myrToSubmitCount != 0) {
            var myrSubmittedPercentage = (int) (100 * getCountWithTag(colleagues, "has_myr_submitted") / myrToSubmitCount);
            reviewStatsData.setMyrSubmittedPercentage(myrSubmittedPercentage);

            var myrApprovedPercentage = (int) (100 * myrApprovedCount / myrToSubmitCount);
            reviewStatsData.setMyrSubmittedPercentage(myrApprovedPercentage);
        }
        // eyr forms submitted, approved
        final var eyrToSubmitCount = getCountWithTag(colleagues, "must_create_eyr");
        final var eyrApprovedCount = getCountWithTag(colleagues, "has_eyr_approved");
        if (eyrToSubmitCount != 0) {
            var eyrSubmittedPercentage = (int) (100 * getCountWithTag(colleagues, "has_eyr_submitted") / eyrToSubmitCount);
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
        reviewStatsData.setNewToBusinessCount(getCountWithTag(colleagues, "is_new_to_business"));
        // anniversary reviews completed per quarter
        var colleaguesAnniversary = reviewReportingDAO.getColleagueTargetingAnniversary(requestQuery);
        if (!CollectionUtils.isEmpty(colleaguesAnniversary)) {
            fillAnniversaryReviewPerQuarters(reviewStatsData, colleaguesAnniversary);
        }

        return List.of(reviewStatsData);
    }

    private void fillAnniversaryReviewPerQuarters(ReviewStatsData reviewStatsData, List<ColleagueReportTargeting> colleaguesAnniversary) {
        final var anniversaryCount = colleaguesAnniversary.size();

        final var anniversaryReviewPerQuarter1Count = getCountWithTag(colleaguesAnniversary, "has_eyr_approved_1_quarter");
        reviewStatsData.setAnniversaryReviewPerQuarter1Count(anniversaryReviewPerQuarter1Count);
        reviewStatsData.setAnniversaryReviewPerQuarter1Percentage((int) (100 * anniversaryReviewPerQuarter1Count / anniversaryCount));

        final var anniversaryReviewPerQuarter2Count = getCountWithTag(colleaguesAnniversary, "has_eyr_approved_2_quarter");
        reviewStatsData.setAnniversaryReviewPerQuarter2Count(anniversaryReviewPerQuarter2Count);
        reviewStatsData.setAnniversaryReviewPerQuarter2Percentage((int) (100 * anniversaryReviewPerQuarter2Count / anniversaryCount));

        final var anniversaryReviewPerQuarter3Count = getCountWithTag(colleaguesAnniversary, "has_eyr_approved_3_quarter");
        reviewStatsData.setAnniversaryReviewPerQuarter3Count(anniversaryReviewPerQuarter3Count);
        reviewStatsData.setAnniversaryReviewPerQuarter3Percentage((int) (100 * anniversaryReviewPerQuarter3Count / anniversaryCount));

        final var anniversaryReviewPerQuarter4Count = getCountWithTag(colleaguesAnniversary, "has_eyr_approved_4_quarter");
        reviewStatsData.setAnniversaryReviewPerQuarter4Count(anniversaryReviewPerQuarter4Count);
        reviewStatsData.setAnniversaryReviewPerQuarter4Percentage((int) (100 * anniversaryReviewPerQuarter4Count / anniversaryCount));
    }

    private void fillMyrRatings(ReviewStatsData reviewStatsData, List<ColleagueReportTargeting> colleagues, long myrApprovedCount) {
        var myrRatingBelowExpected = getMyrRatingTypeStats(colleagues, BELOW_EXPECTED, myrApprovedCount);
        reviewStatsData.setMyrRatingBreakdownBelowExpectedPercentage(myrRatingBelowExpected.getRatingPercentage());
        reviewStatsData.setMyrRatingBreakdownBelowExpectedCount(myrRatingBelowExpected.getRatingCount());

        var myrRatingSatisfactory = getMyrRatingTypeStats(colleagues, SATISFACTORY, myrApprovedCount);
        reviewStatsData.setMyrRatingBreakdownSatisfactoryPercentage(myrRatingSatisfactory.getRatingPercentage());
        reviewStatsData.setMyrRatingBreakdownSatisfactoryCount(myrRatingSatisfactory.getRatingCount());

        var myrRatingGreat = getMyrRatingTypeStats(colleagues, GREAT, myrApprovedCount);
        reviewStatsData.setMyrRatingBreakdownGreatPercentage(myrRatingGreat.getRatingPercentage());
        reviewStatsData.setMyrRatingBreakdownGreatCount(myrRatingGreat.getRatingCount());

        var myrRatingOutstanding = getMyrRatingTypeStats(colleagues, OUTSTANDING, myrApprovedCount);
        reviewStatsData.setMyrRatingBreakdownOutstandingPercentage(myrRatingOutstanding.getRatingPercentage());
        reviewStatsData.setMyrRatingBreakdownOutstandingCount(myrRatingOutstanding.getRatingCount());
    }

    private void fillEyrRatings(ReviewStatsData reviewStatsData, List<ColleagueReportTargeting> colleagues, long eyrApprovedCount) {
        var eyrRatingBelowExpected = getEyrRatingTypeStats(colleagues, BELOW_EXPECTED, eyrApprovedCount);
        reviewStatsData.setEyrRatingBreakdownBelowExpectedPercentage(eyrRatingBelowExpected.getRatingPercentage());
        reviewStatsData.setEyrRatingBreakdownBelowExpectedCount(eyrRatingBelowExpected.getRatingCount());

        var eyrRatingSatisfactory = getEyrRatingTypeStats(colleagues, SATISFACTORY, eyrApprovedCount);
        reviewStatsData.setEyrRatingBreakdownSatisfactoryPercentage(eyrRatingSatisfactory.getRatingPercentage());
        reviewStatsData.setEyrRatingBreakdownSatisfactoryCount(eyrRatingSatisfactory.getRatingCount());

        var eyrRatingGreat = getEyrRatingTypeStats(colleagues, GREAT, eyrApprovedCount);
        reviewStatsData.setEyrRatingBreakdownGreatPercentage(eyrRatingGreat.getRatingPercentage());
        reviewStatsData.setEyrRatingBreakdownGreatCount(eyrRatingGreat.getRatingCount());

        var eyrRatingOutstanding = getEyrRatingTypeStats(colleagues, OUTSTANDING, eyrApprovedCount);
        reviewStatsData.setEyrRatingBreakdownOutstandingPercentage(eyrRatingOutstanding.getRatingPercentage());
        reviewStatsData.setEyrRatingBreakdownOutstandingCount(eyrRatingOutstanding.getRatingCount());
    }

    private RatingStatsData getMyrRatingTypeStats(List<ColleagueReportTargeting> colleagues,
                                                  OverallRating ratingType, long myrRatingToSubmitCount) {
        return getRatingTypeStats(colleagues, "myr_what_rating", "myr_how_rating",
                ratingType, myrRatingToSubmitCount);
    }

    private RatingStatsData getEyrRatingTypeStats(List<ColleagueReportTargeting> colleagues,
                                                  OverallRating ratingType, long eyrRatingToSubmitCount) {
        return getRatingTypeStats(colleagues, "eyr_what_rating", "eyr_how_rating",
                ratingType, eyrRatingToSubmitCount);
    }

    private RatingStatsData getRatingTypeStats(List<ColleagueReportTargeting> colleagues,
                                               String whatRatingTag, String howRatingTag,
                                               OverallRating ratingType, long ratingToSubmitCount) {
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
                .filter(c -> rating.equals(getOverallRating(c, whatRatingTag, howRatingTag)))
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