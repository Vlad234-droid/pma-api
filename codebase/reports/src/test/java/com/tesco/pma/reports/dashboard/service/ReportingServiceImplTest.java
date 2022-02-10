package com.tesco.pma.reports.dashboard.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.pagination.Condition;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.reporting.Report;
import com.tesco.pma.reports.dashboard.domain.ColleagueReportTargeting;
import com.tesco.pma.reports.rating.service.RatingService;
import com.tesco.pma.reports.dashboard.LocalServiceTestConfig;
import com.tesco.pma.reports.dashboard.dao.ReportingDAO;
import com.tesco.pma.reports.review.domain.ObjectiveLinkedReviewData;
import com.tesco.pma.reports.dashboard.domain.StatsData;
import com.tesco.pma.reports.dashboard.domain.provider.StatsReportProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.tesco.pma.cycle.api.PMTimelinePointStatus.APPROVED;
import static com.tesco.pma.pagination.Condition.Operand.EQUALS;
import static com.tesco.pma.pagination.Condition.Operand.IN;
import static com.tesco.pma.reports.exception.ErrorCodes.REPORT_NOT_FOUND;

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

import static com.tesco.pma.reports.dashboard.domain.RatingStatsData.OverallRating.GREAT;
import static com.tesco.pma.reports.dashboard.domain.RatingStatsData.OverallRating.OUTSTANDING;
import static com.tesco.pma.reports.dashboard.domain.RatingStatsData.OverallRating.SATISFACTORY;
import static com.tesco.pma.reports.dashboard.domain.RatingStatsData.OverallRating.BELOW_EXPECTED;
import static com.tesco.pma.reports.ReportingConstants.QUERY_PARAMS;
import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(classes = {ReportingServiceImpl.class, LocalServiceTestConfig.class})
@ExtendWith(MockitoExtension.class)
class ReportingServiceImplTest {

    private static final String COLLEAGUE_UUID = "10000000-0000-0000-0000-000000000000";
    private static final String COLLEAGUE_UUID_2 = "20000000-0000-0000-0000-000000000000";
    private static final String LINE_MANAGER_UUID = "10000000-0000-0000-0000-000000000002";
    private static final Integer YEAR = 2021;

    private static final String REPORT_NOT_FOUND_MESSAGE = "Report not found for: {" +
            QUERY_PARAMS + "=RequestQuery(offset=null, limit=null, sort=[], filters=[" +
            "Condition(property=year, operand=" + EQUALS + ", value=" + YEAR + "), " +
            "Condition(property=statuses, operand=" + IN + ", value=[" + APPROVED + "])], search=null)}";

    private static final String REPORT_NOT_FOUND_MESSAGE_EMPTY_QUERY = "Report not found for: {" +
            QUERY_PARAMS + "=RequestQuery(offset=null, limit=null, sort=[], filters=[], search=null)}";

    @Autowired
    private NamedMessageSourceAccessor messageSourceAccessor;

    @MockBean
    private ReportingDAO reportingDAO;

    @MockBean
    private RatingService ratingService;

    @Autowired
    private ReportingServiceImpl reportingService;

    @Test
    void getLinkedObjectivesData() {
        var reportData = List.of(buildObjectiveLinkedReviewData(1),
                                                            buildObjectiveLinkedReviewData(2));
        final var requestQuery = new RequestQuery();
        requestQuery.setFilters(new ArrayList<>(Arrays.asList(new Condition("year", EQUALS, YEAR),
                                                              new Condition("statuses", IN, List.of(APPROVED.getCode())))));

        when(reportingDAO.getLinkedObjectivesData(requestQuery)).thenReturn(reportData);

        final var res = reportingService.getLinkedObjectivesReport(requestQuery);

        assertEquals(getExpectedReportData(reportData), res.getData());
    }

    @Test
    void getLinkedObjectivesDataNotExists() {
        final var requestQuery = new RequestQuery();
        requestQuery.setFilters(new ArrayList<>(Arrays.asList(new Condition("year", EQUALS, YEAR),
                                                              new Condition("statuses", IN, List.of(APPROVED.getCode())))));
        when(reportingDAO.getLinkedObjectivesData(requestQuery)).thenReturn(null);

        final var exception = assertThrows(NotFoundException.class,
                () -> reportingService.getLinkedObjectivesReport(requestQuery));

        assertEquals(REPORT_NOT_FOUND.getCode(), exception.getCode());
        assertEquals(REPORT_NOT_FOUND_MESSAGE, exception.getMessage());
    }

    @Test
    void getReportColleaguesData() {
        final var requestQuery = new RequestQuery();
        var colleagues = buildColleagueTargeting();
        when(reportingDAO.getColleagueTargeting(requestQuery)).thenReturn(colleagues);

        final var res = reportingService.getReportColleagues(requestQuery);

        assertEquals(colleagues, res);
    }

    @Test
    void getReportColleaguesDataNotExists() {
        final var requestQuery = new RequestQuery();
        when(reportingDAO.getColleagueTargeting(requestQuery)).thenReturn(null);

        final var exception = assertThrows(NotFoundException.class,
                () -> reportingService.getReportColleagues(requestQuery));

        assertEquals(REPORT_NOT_FOUND.getCode(), exception.getCode());
        assertEquals(REPORT_NOT_FOUND_MESSAGE_EMPTY_QUERY, exception.getMessage());
    }

    @Test
    void getStatsReport() {
        final var requestQuery = new RequestQuery();
        when(reportingDAO.getColleagueTargeting(requestQuery)).thenReturn(buildColleagueTargeting());
        when(reportingDAO.getColleagueTargetingAnniversary(requestQuery)).thenReturn(buildColleagueTargetingAnniversary());
        when(ratingService.getOverallRating(GREAT.getDescription(), GREAT.getDescription()))
                .thenReturn(GREAT.getDescription());
        when(ratingService.getOverallRating(OUTSTANDING.getDescription(), OUTSTANDING.getDescription()))
                .thenReturn(OUTSTANDING.getDescription());
        when(ratingService.getOverallRating(SATISFACTORY.getDescription(), SATISFACTORY.getDescription()))
                .thenReturn(SATISFACTORY.getDescription());
        when(ratingService.getOverallRating(BELOW_EXPECTED.getDescription(), BELOW_EXPECTED.getDescription()))
                .thenReturn(BELOW_EXPECTED.getDescription());

        final var res = reportingService.getStatsReport(requestQuery);

        assertEquals(getReport(), res);
    }

    @Test
    void getStatsReportNotExists() {
        final var requestQuery = new RequestQuery();
        when(reportingDAO.getColleagueTargeting(requestQuery)).thenReturn(null);

        final var exception = assertThrows(NotFoundException.class,
                () -> reportingService.getStatsReport(requestQuery));

        assertEquals(REPORT_NOT_FOUND.getCode(), exception.getCode());
        assertEquals(REPORT_NOT_FOUND_MESSAGE_EMPTY_QUERY, exception.getMessage());
    }

    private List<List<Object>> getExpectedReportData(List<ObjectiveLinkedReviewData> reportData) {
        return reportData.stream()
                .map(ObjectiveLinkedReviewData::toList)
                .collect(Collectors.toList());
    }

    private Report getReport() {
        var reportProvider = new StatsReportProvider();
        var data = new StatsData();
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
        reportProvider.setData(List.of(data));

        return reportProvider.getReport();
    }

    private List<ColleagueReportTargeting> buildColleagueTargeting() {
        var colleague1 = new ColleagueReportTargeting();
        colleague1.setUuid(UUID.fromString(COLLEAGUE_UUID));
        colleague1.setFirstName("first_name");
        colleague1.setTags(Map.ofEntries(
                entry(HAS_OBJECTIVE_APPROVED, "1"),
                entry(HAS_OBJECTIVE_SUBMITTED, "0"),
                entry(HAS_MYR_APPROVED, "1"),
                entry(HAS_EYR_APPROVED, "0"),
                entry(MYR_HOW_RATING, GREAT.getDescription()),
                entry(MYR_WHAT_RATING, GREAT.getDescription()),
                entry(EYR_HOW_RATING, OUTSTANDING.getDescription()),
                entry(EYR_WHAT_RATING, OUTSTANDING.getDescription()),
                entry(HAS_MYR_SUBMITTED, "0"),
                entry(HAS_EYR_SUBMITTED, "0"),
                entry(MUST_CREATE_OBJECTIVE, "1"),
                entry(MUST_CREATE_MYR, "1"),
                entry(MUST_CREATE_EYR, "0"),
                entry(IS_NEW_TO_BUSINESS, "1")));

        var colleague2 = new ColleagueReportTargeting();
        colleague2.setUuid(UUID.fromString(LINE_MANAGER_UUID));
        colleague2.setFirstName("first_name_2");
        colleague2.setTags(Map.ofEntries(
                entry(HAS_OBJECTIVE_APPROVED, "1"),
                entry(HAS_OBJECTIVE_SUBMITTED, "1"),
                entry(HAS_MYR_APPROVED, "1"),
                entry(HAS_EYR_APPROVED, "1"),
                entry(MYR_HOW_RATING, SATISFACTORY.getDescription()),
                entry(MYR_WHAT_RATING, SATISFACTORY.getDescription()),
                entry(EYR_HOW_RATING, BELOW_EXPECTED.getDescription()),
                entry(EYR_WHAT_RATING, BELOW_EXPECTED.getDescription()),
                entry(HAS_MYR_SUBMITTED, "1"),
                entry(HAS_EYR_SUBMITTED, "1"),
                entry(MUST_CREATE_OBJECTIVE, "1"),
                entry(MUST_CREATE_MYR, "1"),
                entry(MUST_CREATE_EYR, "1"),
                entry(IS_NEW_TO_BUSINESS, "1")));

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

    private ObjectiveLinkedReviewData buildObjectiveLinkedReviewData(Integer objectiveNumber) {
        var reportData = new ObjectiveLinkedReviewData();
        reportData.setIamId("UKE12375189");
        reportData.setColleagueUUID(COLLEAGUE_UUID);
        reportData.setFirstName("Name");
        reportData.setLastName("Surname");
        reportData.setWorkLevel("WL5");
        reportData.setJobTitle("JobTitle");
        reportData.setLineManager(LINE_MANAGER_UUID);
        reportData.setObjectiveNumber(objectiveNumber);
        reportData.setStatus(APPROVED);
        reportData.setStrategicPriority("Priority");
        reportData.setObjectiveTitle("Title");
        reportData.setHowAchieved("HowAchieved");
        reportData.setHowOverAchieved("HowOverAchieved");

        return reportData;
    }
}