package com.tesco.pma.reports.review.service.rest;

import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.pagination.Condition;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.reporting.Report;
import com.tesco.pma.reports.domain.ColleagueReportTargeting;
import com.tesco.pma.reports.review.LocalTestConfig;
import com.tesco.pma.reports.review.domain.ReviewStatsData;
import com.tesco.pma.reports.review.domain.provider.ObjectiveLinkedReviewReportProvider;
import com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider;
import com.tesco.pma.reports.review.service.ReviewReportingService;
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
import static com.tesco.pma.reports.review.domain.RatingStatsData.OverallRating.BELOW_EXPECTED;
import static com.tesco.pma.reports.review.domain.RatingStatsData.OverallRating.GREAT;
import static com.tesco.pma.reports.review.domain.RatingStatsData.OverallRating.OUTSTANDING;
import static com.tesco.pma.reports.review.domain.RatingStatsData.OverallRating.SATISFACTORY;
import static com.tesco.pma.reports.review.service.rest.ReviewReportingEndpoint.APPLICATION_FORCE_DOWNLOAD_VALUE;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReviewReportingEndpoint.class)
@ContextConfiguration(classes = {LocalTestConfig.class, ReviewReportingEndpoint.class})
class ReviewReportingEndpointTest extends AbstractEndpointTest {

    private static final String COLLEAGUE_UUID_STR = "10000000-0000-0000-0000-000000000000";
    private static final String COLLEAGUE_UUID_STR_2 = "20000000-0000-0000-0000-000000000000";
    private static final Integer YEAR = 2021;
    private static final String REPORTS_URL = "/reports/";
    private static final String LINKED_OBJECTIVE_REVIEW_REPORT_URL = REPORTS_URL + "linked-objective-report/formats/excel";
    private static final String LINKED_OBJECTIVE_REVIEW_REPORT_DATA_URL = REPORTS_URL + ObjectiveLinkedReviewReportProvider.REPORT_NAME;
    private static final String LINKED_OBJECTIVES_REPORT_GET_RESPONSE_JSON_FILE_NAME = "linked_objectives_report_get_ok_response.json";
    private static final String REVIEW_STATS_REPORT_GET_RESPONSE_JSON_FILE_NAME = "review_stats_report_get_ok_response.json";
    private static final String TARGETING_COLLEAGUES_GET_RESPONSE_JSON_FILE_NAME = "targeting_colleagues_get_ok_response.json";
    private static final String TARGETING_COLLEAGUES_DATA_URL = REPORTS_URL + "targeting-colleagues";
    private static final String REVIEW_STATS_REPORT_DATA_URL = REPORTS_URL + ReviewStatsReportProvider.REPORT_NAME;

    @MockBean
    private ReviewReportingService service;

    @Test
    void getLinkedObjectivesReport() throws Exception {
        var requestQuery = buildRequestQuery();
        when(service.getLinkedObjectivesReport(any())).thenReturn(buildObjectiveLinkedReviewReport());

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

        verifyNoInteractions(service);
    }

    @Test
    void getLinkedObjectivesReportIfNotFound() throws Exception {
        when(service.getLinkedObjectivesReport(any())).thenThrow(NotFoundException.class);

        performGetWith(talentAdmin(), status().isNotFound(),
                LINKED_OBJECTIVE_REVIEW_REPORT_URL, new RequestQuery());
    }

    @Test
    void getLinkedObjectivesReportData() throws Exception {
        var requestQuery = buildRequestQuery();
        when(service.getLinkedObjectivesReport(any())).thenReturn(buildObjectiveLinkedReviewReport());

        var result = performGetWith(talentAdmin(), status().isOk(), LINKED_OBJECTIVE_REVIEW_REPORT_DATA_URL, requestQuery);

        assertResponseContent(result.getResponse(), LINKED_OBJECTIVES_REPORT_GET_RESPONSE_JSON_FILE_NAME);
    }

    @Test
    void cannotGetLinkedObjectivesReportDataIfUnauthorized() throws Exception {
        mvc.perform(get(LINKED_OBJECTIVE_REVIEW_REPORT_DATA_URL, new RequestQuery())
                        .with(anonymous())
                        .accept(APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    @Test
    void getLinkedObjectivesReportDataIfNotFound() throws Exception {
        when(service.getLinkedObjectivesReport(any())).thenThrow(NotFoundException.class);

        performGetWith(admin(), status().isNotFound(), LINKED_OBJECTIVE_REVIEW_REPORT_DATA_URL, new RequestQuery());
    }

    @Test
    void getReviewReportColleagues() throws Exception {
        var requestQuery = buildRequestQuery();
        when(service.getReviewReportColleagues(any())).thenReturn(buildColleagueTargeting());

        var result = performGetWith(talentAdmin(), status().isOk(), TARGETING_COLLEAGUES_DATA_URL, requestQuery);

        assertResponseContent(result.getResponse(), TARGETING_COLLEAGUES_GET_RESPONSE_JSON_FILE_NAME);
    }

    @Test
    void getReviewReportColleaguesIfNotFound() throws Exception {
        when(service.getReviewReportColleagues(any())).thenThrow(NotFoundException.class);

        performGetWith(admin(), status().isNotFound(), TARGETING_COLLEAGUES_DATA_URL, new RequestQuery());
    }

    @Test
    void getReviewReportColleaguesIfUnauthorized() throws Exception {
        mvc.perform(get(TARGETING_COLLEAGUES_DATA_URL, new RequestQuery())
                        .with(anonymous())
                        .accept(APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    @Test
    void getReviewStatsReport() throws Exception {
        when(service.getReviewStatsReport(any())).thenReturn(buildReport());

        var result = performGetWith(talentAdmin(), status().isOk(), REVIEW_STATS_REPORT_DATA_URL);

        assertResponseContent(result.getResponse(), REVIEW_STATS_REPORT_GET_RESPONSE_JSON_FILE_NAME);
    }

    @Test
    void getReviewStatsReportIfNotFound() throws Exception {
        when(service.getReviewStatsReport(any())).thenThrow(NotFoundException.class);

        performGetWith(admin(), status().isNotFound(), REVIEW_STATS_REPORT_DATA_URL, new RequestQuery());
    }

    @Test
    void getReviewStatsReportIfUnauthorized() throws Exception {
        mvc.perform(get(REVIEW_STATS_REPORT_DATA_URL, new RequestQuery())
                        .with(anonymous())
                        .accept(APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    private List<ColleagueReportTargeting> buildColleagueTargeting() {
        var colleague1 = new ColleagueReportTargeting();
        colleague1.setUuid(UUID.fromString(COLLEAGUE_UUID_STR));
        colleague1.setFirstName("first_name");
        colleague1.setTags(Map.ofEntries(
                entry("has_objective_approved", "1"),
                entry("has_objective_submitted", "0"),
                entry("has_myr_approved", "1"),
                entry("has_eyr_approved", "0"),
                entry("myr_how_rating", GREAT.getDescription()),
                entry("myr_what_rating", GREAT.getDescription()),
                entry("eyr_how_rating", OUTSTANDING.getDescription()),
                entry("eyr_what_rating", OUTSTANDING.getDescription()),
                entry("has_myr_submitted", "0"),
                entry("has_eyr_submitted", "0"),
                entry("must_create_objective", "1"),
                entry("must_create_myr", "1"),
                entry("must_create_eyr", "0"),
                entry("is_new_to_business", "1"),
                entry("has_eyr_approved_1_quarter", "1"),
                entry("has_eyr_approved_2_quarter", "1"),
                entry("has_eyr_approved_3_quarter", "1"),
                entry("has_eyr_approved_4_quarter", "0")));

        var colleague2 = new ColleagueReportTargeting();
        colleague2.setUuid(UUID.fromString(COLLEAGUE_UUID_STR_2));
        colleague2.setFirstName("first_name_2");
        colleague2.setTags(Map.ofEntries(
                entry("has_objective_approved", "1"),
                entry("has_objective_submitted", "1"),
                entry("has_myr_approved", "1"),
                entry("has_eyr_approved", "1"),
                entry("myr_how_rating", SATISFACTORY.getDescription()),
                entry("myr_what_rating", SATISFACTORY.getDescription()),
                entry("eyr_how_rating", BELOW_EXPECTED.getDescription()),
                entry("eyr_what_rating", BELOW_EXPECTED.getDescription()),
                entry("has_myr_submitted", "1"),
                entry("has_eyr_submitted", "1"),
                entry("must_create_objective", "1"),
                entry("must_create_myr", "1"),
                entry("must_create_eyr", "1"),
                entry("is_new_to_business", "1"),
                entry("has_eyr_approved_1_quarter", "0"),
                entry("has_eyr_approved_2_quarter", "1"),
                entry("has_eyr_approved_3_quarter", "1"),
                entry("has_eyr_approved_4_quarter", "1")));

        return List.of(colleague1, colleague2);
    }

    private Report buildReport() {
        var reportProvider = new ReviewStatsReportProvider();
        var data = new ReviewStatsData();
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
}