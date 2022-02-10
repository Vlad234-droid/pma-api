package com.tesco.pma.reports.review.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.pagination.Condition;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.reporting.Report;
import com.tesco.pma.reports.domain.ColleagueReportTargeting;
import com.tesco.pma.reports.rating.service.RatingService;
import com.tesco.pma.reports.review.LocalServiceTestConfig;
import com.tesco.pma.reports.review.dao.ReviewReportingDAO;
import com.tesco.pma.reports.review.domain.ObjectiveLinkedReviewData;
import com.tesco.pma.reports.review.domain.ReviewStatsData;
import com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider;
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
import static com.tesco.pma.reports.exception.ErrorCodes.REVIEW_REPORT_NOT_FOUND;

import static com.tesco.pma.reports.review.domain.RatingStatsData.OverallRating.GREAT;
import static com.tesco.pma.reports.review.domain.RatingStatsData.OverallRating.OUTSTANDING;
import static com.tesco.pma.reports.review.domain.RatingStatsData.OverallRating.SATISFACTORY;
import static com.tesco.pma.reports.review.domain.RatingStatsData.OverallRating.BELOW_EXPECTED;
import static com.tesco.pma.reports.review.service.ReviewReportingServiceImpl.QUERY_PARAMS;
import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(classes = {ReviewReportingServiceImpl.class, LocalServiceTestConfig.class})
@ExtendWith(MockitoExtension.class)
class ReviewReportingServiceImplTest {

    private static final String COLLEAGUE_UUID = "10000000-0000-0000-0000-000000000000";
    private static final String COLLEAGUE_UUID_2 = "20000000-0000-0000-0000-000000000000";
    private static final String LINE_MANAGER_UUID = "10000000-0000-0000-0000-000000000002";
    private static final Integer YEAR = 2021;

    private static final String REVIEW_REPORT_NOT_FOUND_MESSAGE = "Review report not found for: {" +
            QUERY_PARAMS + "=RequestQuery(offset=null, limit=null, sort=[], filters=[" +
            "Condition(property=year, operand=" + EQUALS + ", value=" + YEAR + "), " +
            "Condition(property=statuses, operand=" + IN + ", value=[" + APPROVED + "])], search=null)}";

    private static final String REVIEW_REPORT_NOT_FOUND_MESSAGE_EMPTY_QUERY = "Review report not found for: {" +
            QUERY_PARAMS + "=RequestQuery(offset=null, limit=null, sort=[], filters=[], search=null)}";

    @Autowired
    private NamedMessageSourceAccessor messageSourceAccessor;

    @MockBean
    private ReviewReportingDAO reviewReportingDAO;

    @MockBean
    private RatingService ratingService;

    @Autowired
    private ReviewReportingServiceImpl reviewReportingService;

    @Test
    void getLinkedObjectivesData() {
        var reportData = List.of(buildObjectiveLinkedReviewData(1),
                                                            buildObjectiveLinkedReviewData(2));
        final var requestQuery = new RequestQuery();
        requestQuery.setFilters(new ArrayList<>(Arrays.asList(new Condition("year", EQUALS, YEAR),
                                                              new Condition("statuses", IN, List.of(APPROVED.getCode())))));

        when(reviewReportingDAO.getLinkedObjectivesData(requestQuery)).thenReturn(reportData);

        final var res = reviewReportingService.getLinkedObjectivesReport(requestQuery);

        assertEquals(getExpectedReportData(reportData), res.getData());
    }

    @Test
    void getLinkedObjectivesDataNotExists() {
        final var requestQuery = new RequestQuery();
        requestQuery.setFilters(new ArrayList<>(Arrays.asList(new Condition("year", EQUALS, YEAR),
                                                              new Condition("statuses", IN, List.of(APPROVED.getCode())))));
        when(reviewReportingDAO.getLinkedObjectivesData(requestQuery)).thenReturn(null);

        final var exception = assertThrows(NotFoundException.class,
                () -> reviewReportingService.getLinkedObjectivesReport(requestQuery));

        assertEquals(REVIEW_REPORT_NOT_FOUND.getCode(), exception.getCode());
        assertEquals(REVIEW_REPORT_NOT_FOUND_MESSAGE, exception.getMessage());
    }

    @Test
    void getReviewReportColleaguesData() {
        final var requestQuery = new RequestQuery();
        var colleagues = buildColleagueTargeting();
        when(reviewReportingDAO.getColleagueTargeting(requestQuery)).thenReturn(colleagues);

        final var res = reviewReportingService.getReviewReportColleagues(requestQuery);

        assertEquals(colleagues, res);
    }

    @Test
    void getReviewReportColleaguesDataNotExists() {
        final var requestQuery = new RequestQuery();
        when(reviewReportingDAO.getColleagueTargeting(requestQuery)).thenReturn(null);

        final var exception = assertThrows(NotFoundException.class,
                () -> reviewReportingService.getReviewReportColleagues(requestQuery));

        assertEquals(REVIEW_REPORT_NOT_FOUND.getCode(), exception.getCode());
        assertEquals(REVIEW_REPORT_NOT_FOUND_MESSAGE_EMPTY_QUERY, exception.getMessage());
    }

    @Test
    void getReviewStatsReport() {
        final var requestQuery = new RequestQuery();
        when(reviewReportingDAO.getColleagueTargeting(requestQuery)).thenReturn(buildColleagueTargeting());
        when(reviewReportingDAO.getColleagueTargetingAnniversary(requestQuery)).thenReturn(buildColleagueTargetingAnniversary());
        when(ratingService.getOverallRating(GREAT.getDescription(), GREAT.getDescription()))
                .thenReturn(GREAT.getDescription());
        when(ratingService.getOverallRating(OUTSTANDING.getDescription(), OUTSTANDING.getDescription()))
                .thenReturn(OUTSTANDING.getDescription());
        when(ratingService.getOverallRating(SATISFACTORY.getDescription(), SATISFACTORY.getDescription()))
                .thenReturn(SATISFACTORY.getDescription());
        when(ratingService.getOverallRating(BELOW_EXPECTED.getDescription(), BELOW_EXPECTED.getDescription()))
                .thenReturn(BELOW_EXPECTED.getDescription());

        final var res = reviewReportingService.getReviewStatsReport(requestQuery);

        assertEquals(getReport(), res);
    }

    @Test
    void getReviewStatsReportNotExists() {
        final var requestQuery = new RequestQuery();
        when(reviewReportingDAO.getColleagueTargeting(requestQuery)).thenReturn(null);

        final var exception = assertThrows(NotFoundException.class,
                () -> reviewReportingService.getReviewStatsReport(requestQuery));

        assertEquals(REVIEW_REPORT_NOT_FOUND.getCode(), exception.getCode());
        assertEquals(REVIEW_REPORT_NOT_FOUND_MESSAGE_EMPTY_QUERY, exception.getMessage());
    }

    private List<List<Object>> getExpectedReportData(List<ObjectiveLinkedReviewData> reportData) {
        return reportData.stream()
                .map(ObjectiveLinkedReviewData::toList)
                .collect(Collectors.toList());
    }

    private Report getReport() {
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

    private List<ColleagueReportTargeting> buildColleagueTargeting() {
        var colleague1 = new ColleagueReportTargeting();
        colleague1.setUuid(UUID.fromString(COLLEAGUE_UUID));
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
                entry("is_new_to_business", "1")));

        var colleague2 = new ColleagueReportTargeting();
        colleague2.setUuid(UUID.fromString(LINE_MANAGER_UUID));
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
                entry("is_new_to_business", "1")));

        return List.of(colleague1, colleague2);
    }

    private List<ColleagueReportTargeting> buildColleagueTargetingAnniversary() {
        var colleague1 = new ColleagueReportTargeting();
        colleague1.setUuid(UUID.fromString(COLLEAGUE_UUID));
        colleague1.setFirstName("first_name");
        colleague1.setTags(Map.ofEntries(
                entry("has_eyr_approved_1_quarter", "0"),
                entry("has_eyr_approved_2_quarter", "1"),
                entry("has_eyr_approved_3_quarter", "1"),
                entry("has_eyr_approved_4_quarter", "0")));

        var colleague2 = new ColleagueReportTargeting();
        colleague2.setUuid(UUID.fromString(COLLEAGUE_UUID_2));
        colleague2.setFirstName("first_name_2");
        colleague2.setTags(Map.ofEntries(
                entry("has_eyr_approved_1_quarter", "0"),
                entry("has_eyr_approved_2_quarter", "1"),
                entry("has_eyr_approved_3_quarter", "1"),
                entry("has_eyr_approved_4_quarter", "0")));

        var colleague3 = new ColleagueReportTargeting();
        colleague3.setUuid(UUID.fromString(LINE_MANAGER_UUID));
        colleague3.setFirstName("first_name_3");
        colleague3.setTags(Map.ofEntries(
                entry("has_eyr_approved_1_quarter", "1"),
                entry("has_eyr_approved_2_quarter", "0"),
                entry("has_eyr_approved_3_quarter", "1"),
                entry("has_eyr_approved_4_quarter", "1")));

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