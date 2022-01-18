package com.tesco.pma.reporting.review.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.reporting.review.LocalServiceTestConfig;
import com.tesco.pma.reporting.review.dao.ReviewReportingDAO;
import com.tesco.pma.reporting.review.domain.ObjectiveLinkedReviewReport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static com.tesco.pma.cycle.api.PMTimelinePointStatus.APPROVED;
import static com.tesco.pma.reporting.exception.ErrorCodes.REVIEW_REPORT_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(classes = {ReviewReportingServiceImpl.class, LocalServiceTestConfig.class})
@ExtendWith(MockitoExtension.class)
class ReviewReportingServiceImplTest {

    private static final String COLLEAGUE_UUID = "10000000-0000-0000-0000-000000000000";
    private static final String LINE_MANAGER_UUID = "10000000-0000-0000-0000-000000000002";
    private static final Instant START_TIME = Instant.parse("2021-11-26T14:18:42.615Z");
    private static final Instant END_TIME = Instant.parse("2021-11-28T14:18:42.615Z");

    private static final String REVIEW_REPORT_NOT_FOUND_MESSAGE = "Review report not found for: " +
            "{endTime=" + END_TIME.toString() + ", startTime=" + START_TIME.toString() + ", status=APPROVED}";

    @Autowired
    private NamedMessageSourceAccessor messageSourceAccessor;

    @MockBean
    private ReviewReportingDAO reviewReportingDAO;

    @Autowired
    private ReviewReportingServiceImpl reviewReportingService;

    @Test
    void getLinkedObjectivesData() {
        var report = buildObjectiveLinkedReviewReport();
        when(reviewReportingDAO.getLinkedObjectivesData(any(), any(), any())).thenReturn(report);

        final var res = reviewReportingService.getLinkedObjectivesData(START_TIME, END_TIME, APPROVED);

        assertEquals(report.getReport(), res);
    }

    @Test
    void getLinkedObjectivesDataNotExists() {
        when(reviewReportingDAO.getLinkedObjectivesData(any(), any(), any())).thenReturn(null);

        final var exception = assertThrows(NotFoundException.class,
                () -> reviewReportingService.getLinkedObjectivesData(START_TIME, END_TIME, APPROVED));

        assertEquals(REVIEW_REPORT_NOT_FOUND.getCode(), exception.getCode());
        assertEquals(REVIEW_REPORT_NOT_FOUND_MESSAGE, exception.getMessage());
    }

    private ObjectiveLinkedReviewReport buildObjectiveLinkedReviewReport() {
        var report = new ObjectiveLinkedReviewReport();
        report.setIamId("UKE12375189");
        report.setColleagueUUID(COLLEAGUE_UUID);
        report.setFirstName("Name");
        report.setLastName("Surname");
        report.setWorkLevel("WL5");
        report.setJobTitle("JobTitle");
        report.setLineManager(LINE_MANAGER_UUID);
        report.setObjectiveNumber(1);
        report.setStatus("APPROVED");
        report.setStrategicPriority("Priority");
        report.setObjectiveTitle("Title");
        report.setHowAchieved("HowAchieved");
        report.setHowOverAchieved("HowOverAchieved");

        return report;
    }
}