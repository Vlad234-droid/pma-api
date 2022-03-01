package com.tesco.pma.reporting.dashboard.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.pagination.Condition;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.reporting.Report;
import com.tesco.pma.reporting.dashboard.domain.ColleagueReportTargeting;
import com.tesco.pma.reporting.rating.service.RatingService;
import com.tesco.pma.reporting.dashboard.LocalServiceTestConfig;
import com.tesco.pma.reporting.dashboard.dao.ReportingDAO;
import com.tesco.pma.reporting.dashboard.domain.StatsData;
import com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.pagination.Condition.Operand.IN;
import static com.tesco.pma.reporting.ReportingConstants.EYR_OVERALL_RATING;
import static com.tesco.pma.reporting.ReportingConstants.HAS_FEEDBACK_GIVEN;
import static com.tesco.pma.reporting.ReportingConstants.HAS_FEEDBACK_REQUESTED;
import static com.tesco.pma.reporting.ReportingConstants.MYR_OVERALL_RATING;
import static com.tesco.pma.reporting.exception.ErrorCodes.REPORT_NOT_FOUND;

import static com.tesco.pma.reporting.ReportingConstants.BELOW_EXPECTED_RATING;
import static com.tesco.pma.reporting.ReportingConstants.GREAT_RATING;
import static com.tesco.pma.reporting.ReportingConstants.OUTSTANDING_RATING;
import static com.tesco.pma.reporting.ReportingConstants.SATISFACTORY_RATING;
import static com.tesco.pma.reporting.ReportingConstants.EYR_HOW_RATING;
import static com.tesco.pma.reporting.ReportingConstants.EYR_WHAT_RATING;
import static com.tesco.pma.reporting.ReportingConstants.HAS_EYR_APPROVED;
import static com.tesco.pma.reporting.ReportingConstants.HAS_EYR_APPROVED_1_QUARTER;
import static com.tesco.pma.reporting.ReportingConstants.HAS_EYR_APPROVED_2_QUARTER;
import static com.tesco.pma.reporting.ReportingConstants.HAS_EYR_APPROVED_3_QUARTER;
import static com.tesco.pma.reporting.ReportingConstants.HAS_EYR_APPROVED_4_QUARTER;
import static com.tesco.pma.reporting.ReportingConstants.HAS_EYR_SUBMITTED;
import static com.tesco.pma.reporting.ReportingConstants.HAS_MYR_APPROVED;
import static com.tesco.pma.reporting.ReportingConstants.HAS_MYR_SUBMITTED;
import static com.tesco.pma.reporting.ReportingConstants.HAS_OBJECTIVE_APPROVED;
import static com.tesco.pma.reporting.ReportingConstants.HAS_OBJECTIVE_SUBMITTED;
import static com.tesco.pma.reporting.ReportingConstants.IS_NEW_TO_BUSINESS;
import static com.tesco.pma.reporting.ReportingConstants.MUST_CREATE_EYR;
import static com.tesco.pma.reporting.ReportingConstants.MUST_CREATE_MYR;
import static com.tesco.pma.reporting.ReportingConstants.MUST_CREATE_OBJECTIVE;
import static com.tesco.pma.reporting.ReportingConstants.MYR_HOW_RATING;
import static com.tesco.pma.reporting.ReportingConstants.MYR_WHAT_RATING;

import static com.tesco.pma.reporting.util.ExcelReportUtils.TOPICS_PARAM_NAME;
import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(classes = {ReportingServiceImpl.class, LocalServiceTestConfig.class})
@ExtendWith(MockitoExtension.class)
class ReportingServiceImplTest {

    private static final String COLLEAGUE_UUID = "10000000-0000-0000-0000-000000000000";
    private static final String COLLEAGUE_UUID_2 = "20000000-0000-0000-0000-000000000000";
    private static final String LINE_MANAGER_UUID = "10000000-0000-0000-0000-000000000002";

    private static final String REPORT_NOT_FOUND_MESSAGE = "Report not found for: {queryParams=%s}";

    @Autowired
    private NamedMessageSourceAccessor messageSourceAccessor;

    @MockBean
    private ReportingDAO reportingDAO;

    @MockBean
    private RatingService ratingService;

    @Autowired
    private ReportingServiceImpl reportingService;

    @Test
    void getReportColleaguesData() {
        final var requestQuery = new RequestQuery();
        when(reportingDAO.getColleagueTargeting(requestQuery)).thenReturn(buildColleagueTargeting());
        when(ratingService.getOverallRating(GREAT_RATING, GREAT_RATING)).thenReturn(GREAT_RATING);
        when(ratingService.getOverallRating(OUTSTANDING_RATING, OUTSTANDING_RATING)).thenReturn(OUTSTANDING_RATING);
        when(ratingService.getOverallRating(SATISFACTORY_RATING, SATISFACTORY_RATING)).thenReturn(SATISFACTORY_RATING);
        when(ratingService.getOverallRating(BELOW_EXPECTED_RATING, BELOW_EXPECTED_RATING)).thenReturn(BELOW_EXPECTED_RATING);

        final var res = reportingService.getReportColleagues(requestQuery);

        var expected = buildColleagueTargeting();
        expected.get(0).getTags().put(MYR_OVERALL_RATING, GREAT_RATING);
        expected.get(0).getTags().put(EYR_OVERALL_RATING, OUTSTANDING_RATING);
        expected.get(1).getTags().put(MYR_OVERALL_RATING, SATISFACTORY_RATING);
        expected.get(1).getTags().put(EYR_OVERALL_RATING, BELOW_EXPECTED_RATING);
        assertEquals(expected, res);
    }

    @Test
    void getReportColleaguesDataNotExists() {
        final var requestQuery = new RequestQuery();
        when(reportingDAO.getColleagueTargeting(requestQuery)).thenReturn(null);

        final var exception = assertThrows(NotFoundException.class,
                () -> reportingService.getReportColleagues(requestQuery));

        assertEquals(REPORT_NOT_FOUND.getCode(), exception.getCode());
        assertEquals(getReportNotFoundMessage(requestQuery), exception.getMessage());
    }

    @Test
    void getStatsReport() {
        final var requestQuery = new RequestQuery();
        requestQuery.setFilters(List.of(new Condition(TOPICS_PARAM_NAME, IN, List.of("colleagues-count", "new-to-business-count"))));
        final var colleagues = buildColleagueTargeting();
        when(reportingDAO.getColleagueTargeting(requestQuery)).thenReturn(colleagues);
        when(reportingDAO.getColleagueTargetingAnniversary(requestQuery)).thenReturn(buildColleagueTargetingAnniversary());
        when(ratingService.getOverallRating(GREAT_RATING, GREAT_RATING)).thenReturn(GREAT_RATING);
        when(ratingService.getOverallRating(OUTSTANDING_RATING, OUTSTANDING_RATING)).thenReturn(OUTSTANDING_RATING);
        when(ratingService.getOverallRating(SATISFACTORY_RATING, SATISFACTORY_RATING)).thenReturn(SATISFACTORY_RATING);
        when(ratingService.getOverallRating(BELOW_EXPECTED_RATING, BELOW_EXPECTED_RATING)).thenReturn(BELOW_EXPECTED_RATING);

        final var res = reportingService.getStatsReport(requestQuery);

        assertEquals(getReport(colleagues.size()), res);
    }

    @Test
    void getStatsReportNotExists() {
        final var requestQuery = new RequestQuery();
        requestQuery.setFilters(List.of(new Condition(TOPICS_PARAM_NAME, IN, List.of("colleagues-count", "new-to-business-count"))));
        when(reportingDAO.getColleagueTargeting(requestQuery)).thenReturn(null);

        final var exception = assertThrows(NotFoundException.class,
                () -> reportingService.getStatsReport(requestQuery));

        assertEquals(REPORT_NOT_FOUND.getCode(), exception.getCode());
        assertEquals(getReportNotFoundMessage(requestQuery), exception.getMessage());
    }

    @Test
    void getStatsReportNotTopics() {
        final var requestQuery = new RequestQuery();

        final var exception = assertThrows(NotFoundException.class,
                () -> reportingService.getStatsReport(requestQuery));

        assertEquals(REPORT_NOT_FOUND.getCode(), exception.getCode());
        assertEquals(getReportNotFoundMessage(requestQuery), exception.getMessage());
        verifyNoInteractions(reportingDAO);
    }

    private String getReportNotFoundMessage(RequestQuery requestQuery) {
        return String.format(REPORT_NOT_FOUND_MESSAGE, requestQuery);
    }

    private Report getReport(int colleaguesCount) {
        var reportProvider = new StatsReportProvider();
        var data = new StatsData();
        data.setColleaguesCount(colleaguesCount);
        data.setObjectivesSubmittedPercentage(50);
        data.setObjectivesApprovedPercentage(100);
        data.setMyrApprovedPercentage(100);
        data.setMyrSubmittedPercentage(50);
        data.setEyrSubmittedPercentage(100);
        data.setEyrApprovedPercentage(100);
        data.setMyrRatingBreakdownSatisfactoryPercentage(50);
        data.setMyrRatingBreakdownSatisfactoryCount(1);
        data.setMyrRatingBreakdownGreatPercentage(50);
        data.setMyrRatingBreakdownGreatCount(1);
        data.setEyrRatingBreakdownBelowExpectedPercentage(100);
        data.setEyrRatingBreakdownBelowExpectedCount(1);
        data.setEyrRatingBreakdownOutstandingPercentage(100);
        data.setEyrRatingBreakdownOutstandingCount(1);
        data.setAnniversaryReviewPerQuarter1Percentage(33);
        data.setAnniversaryReviewPerQuarter1Count(1);
        data.setAnniversaryReviewPerQuarter2Percentage(66);
        data.setAnniversaryReviewPerQuarter2Count(2);
        data.setAnniversaryReviewPerQuarter3Percentage(100);
        data.setAnniversaryReviewPerQuarter3Count(3);
        data.setAnniversaryReviewPerQuarter4Percentage(33);
        data.setAnniversaryReviewPerQuarter4Count(1);
        data.setNewToBusinessCount(2);
        data.setFeedbackGivenPercentage(100);
        data.setFeedbackRequestedPercentage(50);
        reportProvider.setData(List.of(data));

        return reportProvider.getReport();
    }

    private List<ColleagueReportTargeting> buildColleagueTargeting() {
        var colleague1 = new ColleagueReportTargeting();
        colleague1.setUuid(UUID.fromString(COLLEAGUE_UUID));
        colleague1.setFirstName("first_name");
        colleague1.setTags(new HashMap<>(Map.ofEntries(
                entry(HAS_OBJECTIVE_APPROVED, "1"),
                entry(HAS_OBJECTIVE_SUBMITTED, "0"),
                entry(HAS_MYR_APPROVED, "1"),
                entry(HAS_EYR_APPROVED, "0"),
                entry(MYR_HOW_RATING, GREAT_RATING),
                entry(MYR_WHAT_RATING, GREAT_RATING),
                entry(EYR_HOW_RATING, OUTSTANDING_RATING),
                entry(EYR_WHAT_RATING, OUTSTANDING_RATING),
                entry(HAS_MYR_SUBMITTED, "0"),
                entry(HAS_EYR_SUBMITTED, "0"),
                entry(MUST_CREATE_OBJECTIVE, "1"),
                entry(MUST_CREATE_MYR, "1"),
                entry(MUST_CREATE_EYR, "0"),
                entry(IS_NEW_TO_BUSINESS, "1"),
                entry(HAS_FEEDBACK_REQUESTED, "1"),
                entry(HAS_FEEDBACK_GIVEN, "1"))));

        var colleague2 = new ColleagueReportTargeting();
        colleague2.setUuid(UUID.fromString(LINE_MANAGER_UUID));
        colleague2.setFirstName("first_name_2");
        colleague2.setTags(new HashMap<>(Map.ofEntries(
                entry(HAS_OBJECTIVE_APPROVED, "1"),
                entry(HAS_OBJECTIVE_SUBMITTED, "1"),
                entry(HAS_MYR_APPROVED, "1"),
                entry(HAS_EYR_APPROVED, "1"),
                entry(MYR_HOW_RATING, SATISFACTORY_RATING),
                entry(MYR_WHAT_RATING, SATISFACTORY_RATING),
                entry(EYR_HOW_RATING, BELOW_EXPECTED_RATING),
                entry(EYR_WHAT_RATING, BELOW_EXPECTED_RATING),
                entry(HAS_MYR_SUBMITTED, "1"),
                entry(HAS_EYR_SUBMITTED, "1"),
                entry(MUST_CREATE_OBJECTIVE, "1"),
                entry(MUST_CREATE_MYR, "1"),
                entry(MUST_CREATE_EYR, "1"),
                entry(IS_NEW_TO_BUSINESS, "1"),
                entry(HAS_FEEDBACK_REQUESTED, "0"),
                entry(HAS_FEEDBACK_GIVEN, "1"))));

        return List.of(colleague1, colleague2);
    }

    private List<ColleagueReportTargeting> buildColleagueTargetingAnniversary() {
        var colleague1 = new ColleagueReportTargeting();
        colleague1.setUuid(UUID.fromString(COLLEAGUE_UUID));
        colleague1.setFirstName("first_name");
        colleague1.setTags(Map.ofEntries(
                entry(HAS_EYR_APPROVED_1_QUARTER, "0"),
                entry(HAS_EYR_APPROVED_2_QUARTER, "1"),
                entry(HAS_EYR_APPROVED_3_QUARTER, "1"),
                entry(HAS_EYR_APPROVED_4_QUARTER, "0")));

        var colleague2 = new ColleagueReportTargeting();
        colleague2.setUuid(UUID.fromString(COLLEAGUE_UUID_2));
        colleague2.setFirstName("first_name_2");
        colleague2.setTags(Map.ofEntries(
                entry(HAS_EYR_APPROVED_1_QUARTER, "0"),
                entry(HAS_EYR_APPROVED_2_QUARTER, "1"),
                entry(HAS_EYR_APPROVED_3_QUARTER, "1"),
                entry(HAS_EYR_APPROVED_4_QUARTER, "0")));

        var colleague3 = new ColleagueReportTargeting();
        colleague3.setUuid(UUID.fromString(LINE_MANAGER_UUID));
        colleague3.setFirstName("first_name_3");
        colleague3.setTags(Map.ofEntries(
                entry(HAS_EYR_APPROVED_1_QUARTER, "1"),
                entry(HAS_EYR_APPROVED_2_QUARTER, "0"),
                entry(HAS_EYR_APPROVED_3_QUARTER, "1"),
                entry(HAS_EYR_APPROVED_4_QUARTER, "1")));

        return List.of(colleague1, colleague2, colleague3);
    }
}