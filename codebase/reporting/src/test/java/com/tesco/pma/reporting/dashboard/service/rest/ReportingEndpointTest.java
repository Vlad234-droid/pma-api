package com.tesco.pma.reporting.dashboard.service.rest;

import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.pagination.Condition;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.reporting.Report;
import com.tesco.pma.reporting.dashboard.domain.ColleagueReportTargeting;
import com.tesco.pma.reporting.dashboard.LocalTestConfig;
import com.tesco.pma.reporting.dashboard.domain.StatsData;
import com.tesco.pma.reporting.review.service.ReviewReportingService;
import com.tesco.pma.reporting.review.domain.provider.ObjectiveLinkedReviewReportProvider;
import com.tesco.pma.reporting.dashboard.domain.provider.StatsReportProvider;
import com.tesco.pma.reporting.dashboard.service.ReportingService;
import com.tesco.pma.rest.AbstractEndpointTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.cycle.api.PMTimelinePointStatus.APPROVED;
import static com.tesco.pma.pagination.Condition.Operand.EQUALS;
import static com.tesco.pma.pagination.Condition.Operand.IN;

import static com.tesco.pma.reporting.ReportingConstants.BELOW_EXPECTED_RATING;
import static com.tesco.pma.reporting.ReportingConstants.EYR_OVERALL_RATING;
import static com.tesco.pma.reporting.ReportingConstants.GREAT_RATING;
import static com.tesco.pma.reporting.ReportingConstants.HAS_FEEDBACK_GIVEN;
import static com.tesco.pma.reporting.ReportingConstants.HAS_FEEDBACK_REQUESTED;
import static com.tesco.pma.reporting.ReportingConstants.MYR_OVERALL_RATING;
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

import static com.tesco.pma.reporting.dashboard.service.rest.ReportingEndpoint.APPLICATION_FORCE_DOWNLOAD_VALUE;

import static com.tesco.pma.reporting.dashboard.service.rest.ReportingEndpoint.TOPICS_PARAM_NAME;
import static java.util.Arrays.asList;
import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReportingEndpoint.class)
@ContextConfiguration(classes = {LocalTestConfig.class, ReportingEndpoint.class})
class ReportingEndpointTest extends AbstractEndpointTest {

    private static final String COLLEAGUE_UUID_STR = "10000000-0000-0000-0000-000000000000";
    private static final String COLLEAGUE_UUID_STR_2 = "20000000-0000-0000-0000-000000000000";
    private static final Integer YEAR = 2021;
    private static final String REPORTS_URL = "/reports/";
    private static final String FORMATS_EXCEL_URL = "/formats/excel";
    private static final String LINKED_OBJECTIVE_REVIEW_REPORT_DATA_URL = REPORTS_URL + ObjectiveLinkedReviewReportProvider.REPORT_NAME;
    private static final String LINKED_OBJECTIVE_REVIEW_REPORT_URL = LINKED_OBJECTIVE_REVIEW_REPORT_DATA_URL + FORMATS_EXCEL_URL;
    private static final String LINKED_OBJECTIVES_REPORT_GET_RESPONSE_JSON_FILE_NAME = "linked_objectives_report_get_ok_response.json";
    private static final String STATS_REPORT_GET_RESPONSE_JSON_FILE_NAME = "stats_report_get_ok_response.json";
    private static final String TARGETING_COLLEAGUES_GET_RESPONSE_JSON_FILE_NAME = "targeting_colleagues_get_ok_response.json";
    private static final String TARGETING_COLLEAGUES_DATA_URL = REPORTS_URL + "targeting-colleagues";
    private static final String STATS_REPORT_DATA_URL = REPORTS_URL + StatsReportProvider.REPORT_NAME;
    private static final String STATS_REPORT_URL = STATS_REPORT_DATA_URL + FORMATS_EXCEL_URL;

    @MockBean
    private ReportingService reportingService;

    @MockBean
    private ReviewReportingService reviewReportingService;

    @Test
    void getLinkedObjectivesReport() throws Exception {
        var requestQuery = buildRequestQuery();
        when(reviewReportingService.getLinkedObjectivesReport(any())).thenReturn(buildObjectiveLinkedReviewReport());

        var result = performGetWith(admin(), status().isCreated(),
                APPLICATION_FORCE_DOWNLOAD_VALUE, LINKED_OBJECTIVE_REVIEW_REPORT_URL, requestQuery);

        assertTrue(result.getResponse().getContentAsByteArray().length > 0);
    }

    @Test
    void cannotGetLinkedObjectivesReportIfUnauthorized() throws Exception {
        mvc.perform(get(LINKED_OBJECTIVE_REVIEW_REPORT_URL, new RequestQuery())
                        .with(anonymous())
                        .accept(APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(reviewReportingService);
    }

    @Test
    void getLinkedObjectivesReportIfNotFound() throws Exception { //NOSONAR used MockMvc checks
        when(reviewReportingService.getLinkedObjectivesReport(any())).thenThrow(NotFoundException.class);

        performGetWith(talentAdmin(), status().isNotFound(),
                LINKED_OBJECTIVE_REVIEW_REPORT_URL, new RequestQuery());
    }

    @Test
    void getLinkedObjectivesReportData() throws Exception {
        var requestQuery = buildRequestQuery();
        when(reviewReportingService.getLinkedObjectivesReport(any())).thenReturn(buildObjectiveLinkedReviewReport());

        var result = performGetWith(talentAdmin(), status().isOk(), LINKED_OBJECTIVE_REVIEW_REPORT_DATA_URL, requestQuery);

        assertResponseContent(result.getResponse(), LINKED_OBJECTIVES_REPORT_GET_RESPONSE_JSON_FILE_NAME);
    }

    @Test
    void cannotGetLinkedObjectivesReportDataIfUnauthorized() throws Exception {
        mvc.perform(get(LINKED_OBJECTIVE_REVIEW_REPORT_DATA_URL, new RequestQuery())
                        .with(anonymous())
                        .accept(APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(reviewReportingService);
    }

    @Test
    void getLinkedObjectivesReportDataIfNotFound() throws Exception { //NOSONAR used MockMvc checks
        when(reviewReportingService.getLinkedObjectivesReport(any())).thenThrow(NotFoundException.class);

        performGetWith(admin(), status().isNotFound(), LINKED_OBJECTIVE_REVIEW_REPORT_DATA_URL, new RequestQuery());
    }

    @Test
    void getReportColleagues() throws Exception {
        var requestQuery = buildRequestQuery();
        when(reportingService.getReportColleagues(any())).thenReturn(buildColleagueTargeting());

        var result = performGetWith(talentAdmin(), status().isOk(), TARGETING_COLLEAGUES_DATA_URL, requestQuery);

        assertResponseContent(result.getResponse(), TARGETING_COLLEAGUES_GET_RESPONSE_JSON_FILE_NAME);
    }

    @Test
    void getReportColleaguesIfNotFound() throws Exception { //NOSONAR used MockMvc checks
        when(reportingService.getReportColleagues(any())).thenThrow(NotFoundException.class);

        performGetWith(admin(), status().isNotFound(), TARGETING_COLLEAGUES_DATA_URL, new RequestQuery());
    }

    @Test
    void getReportColleaguesIfUnauthorized() throws Exception {
        mvc.perform(get(TARGETING_COLLEAGUES_DATA_URL, new RequestQuery())
                        .with(anonymous())
                        .accept(APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(reportingService);
    }

    @Test
    void getStatsReport() throws Exception {
        when(reportingService.getStatsReport(any())).thenReturn(buildReport());

        var result = performGetWith(talentAdmin(), status().isOk(), STATS_REPORT_DATA_URL);

        assertResponseContent(result.getResponse(), STATS_REPORT_GET_RESPONSE_JSON_FILE_NAME);
    }

    @Test
    void getStatsReportIfNotFound() throws Exception { //NOSONAR used MockMvc checks
        when(reportingService.getStatsReport(any())).thenThrow(NotFoundException.class);

        performGetWith(admin(), status().isNotFound(), STATS_REPORT_DATA_URL, new RequestQuery());
    }

    @Test
    void getStatsReportIfUnauthorized() throws Exception {
        mvc.perform(get(STATS_REPORT_DATA_URL, new RequestQuery())
                        .with(anonymous())
                        .accept(APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(reportingService);
    }

    @Test
    void getStatsReportFile() throws Exception {
        var requestQuery = buildRequestQuery();
        requestQuery.setFilters(asList(new Condition("year", EQUALS, 2021),
                                       new Condition(TOPICS_PARAM_NAME, IN, asList("colleagues-count"))));
        when(reportingService.getStatsReport(any())).thenReturn(buildStatsReport(2));

        var result = performGetWith(admin(), status().isCreated(),
                APPLICATION_FORCE_DOWNLOAD_VALUE, STATS_REPORT_URL + "?" + TOPICS_PARAM_NAME + "_in[0]=colleagues-count&year=2021");

        assertTrue(result.getResponse().getContentAsByteArray().length > 0);
    }

    @Test
    void cannotGetStatsReportFiletIfUnauthorized() throws Exception {
        mvc.perform(get(STATS_REPORT_URL, new RequestQuery())
                        .with(anonymous())
                        .accept(APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(reportingService);
    }

    @Test
    void getStatsReportFileIfNotFound() throws Exception { //NOSONAR used MockMvc checks
        when(reportingService.getStatsReport(any())).thenThrow(NotFoundException.class);

        performGetWith(talentAdmin(), status().isNotFound(), STATS_REPORT_URL, new RequestQuery());
    }

    private List<ColleagueReportTargeting> buildColleagueTargeting() {
        var colleague1 = new ColleagueReportTargeting();
        colleague1.setUuid(UUID.fromString(COLLEAGUE_UUID_STR));
        colleague1.setFirstName("first_name");
        colleague1.setTags(Map.ofEntries(
                entry(HAS_OBJECTIVE_APPROVED, "1"),
                entry(HAS_OBJECTIVE_SUBMITTED, "0"),
                entry(HAS_MYR_APPROVED, "1"),
                entry(HAS_EYR_APPROVED, "0"),
                entry(MYR_OVERALL_RATING, GREAT_RATING),
                entry(MYR_HOW_RATING, GREAT_RATING),
                entry(MYR_WHAT_RATING, GREAT_RATING),
                entry(EYR_OVERALL_RATING, OUTSTANDING_RATING),
                entry(EYR_HOW_RATING, OUTSTANDING_RATING),
                entry(EYR_WHAT_RATING, OUTSTANDING_RATING),
                entry(HAS_MYR_SUBMITTED, "0"),
                entry(HAS_EYR_SUBMITTED, "0"),
                entry(MUST_CREATE_OBJECTIVE, "1"),
                entry(MUST_CREATE_MYR, "1"),
                entry(MUST_CREATE_EYR, "0"),
                entry(IS_NEW_TO_BUSINESS, "1"),
                entry(HAS_EYR_APPROVED_1_QUARTER, "1"),
                entry(HAS_EYR_APPROVED_2_QUARTER, "1"),
                entry(HAS_EYR_APPROVED_3_QUARTER, "1"),
                entry(HAS_EYR_APPROVED_4_QUARTER, "0"),
                entry(HAS_FEEDBACK_REQUESTED, "1"),
                entry(HAS_FEEDBACK_GIVEN, "1")));

        var colleague2 = new ColleagueReportTargeting();
        colleague2.setUuid(UUID.fromString(COLLEAGUE_UUID_STR_2));
        colleague2.setFirstName("first_name_2");
        colleague2.setTags(Map.ofEntries(
                entry(HAS_OBJECTIVE_APPROVED, "1"),
                entry(HAS_OBJECTIVE_SUBMITTED, "1"),
                entry(HAS_MYR_APPROVED, "1"),
                entry(HAS_EYR_APPROVED, "1"),
                entry(MYR_OVERALL_RATING, SATISFACTORY_RATING),
                entry(MYR_HOW_RATING, SATISFACTORY_RATING),
                entry(MYR_WHAT_RATING, SATISFACTORY_RATING),
                entry(EYR_OVERALL_RATING, BELOW_EXPECTED_RATING),
                entry(EYR_HOW_RATING, BELOW_EXPECTED_RATING),
                entry(EYR_WHAT_RATING, BELOW_EXPECTED_RATING),
                entry(HAS_MYR_SUBMITTED, "1"),
                entry(HAS_EYR_SUBMITTED, "1"),
                entry(MUST_CREATE_OBJECTIVE, "1"),
                entry(MUST_CREATE_MYR, "1"),
                entry(MUST_CREATE_EYR, "1"),
                entry(IS_NEW_TO_BUSINESS, "1"),
                entry(HAS_EYR_APPROVED_1_QUARTER, "0"),
                entry(HAS_EYR_APPROVED_2_QUARTER, "1"),
                entry(HAS_EYR_APPROVED_3_QUARTER, "1"),
                entry(HAS_EYR_APPROVED_4_QUARTER, "1"),
                entry(HAS_FEEDBACK_REQUESTED, "0"),
                entry(HAS_FEEDBACK_GIVEN, "1")));

        return List.of(colleague1, colleague2);
    }

    private Report buildReport() {
        var reportProvider = new StatsReportProvider();
        var data = new StatsData();
        data.setColleaguesCount(2);
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
        data.setAnniversaryReviewPerQuarter3Count(2);
        data.setAnniversaryReviewPerQuarter4Percentage(33);
        data.setAnniversaryReviewPerQuarter4Count(1);
        data.setNewToBusinessCount(2);
        data.setFeedbackGivenPercentage(100);
        data.setFeedbackRequestedPercentage(50);
        reportProvider.setData(List.of(data));

        return reportProvider.getReport();
    }

    private RequestQuery buildRequestQuery() {
        var requestQuery = new RequestQuery();
        requestQuery.setFilters(List.of(
                new Condition("year", EQUALS, YEAR),
                new Condition("statuses", IN, List.of(APPROVED.getCode()))));

        return requestQuery;
    }

    private Report buildObjectiveLinkedReviewReport() {
        var report = new Report();
        report.setData(List.of(
                List.of("UKE12375189", COLLEAGUE_UUID_STR, "Name", "Surname", "WL5", "JobTitle", "UKE12375188", 1,
                        "APPROVED", "Priority", "Title", "How Achieved", "How Over-Achieved")));
        report.setMetadata(new ObjectiveLinkedReviewReportProvider().getReportMetadata());

        return report;
    }

    private Report buildStatsReport(int colleaguesCount) {
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
        data.setAnniversaryReviewPerQuarter3Count(2);
        data.setAnniversaryReviewPerQuarter4Percentage(33);
        data.setAnniversaryReviewPerQuarter4Count(1);
        data.setNewToBusinessCount(2);
        data.setFeedbackGivenPercentage(100);
        data.setFeedbackRequestedPercentage(50);
        reportProvider.setData(List.of(data));

        return reportProvider.getReport();
    }
}