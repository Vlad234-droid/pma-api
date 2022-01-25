package com.tesco.pma.reports.review.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.reports.review.LocalServiceTestConfig;
import com.tesco.pma.reports.review.dao.ReviewReportingDAO;
import com.tesco.pma.reports.review.domain.ObjectiveLinkedReviewData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.Collectors;

import static com.tesco.pma.cycle.api.PMTimelinePointStatus.APPROVED;
import static com.tesco.pma.reports.exception.ErrorCodes.REVIEW_REPORT_NOT_FOUND;

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
    private static final Integer YEAR = 2021;

    private static final String REVIEW_REPORT_NOT_FOUND_MESSAGE = "Review report not found for: " +
            "{statuses=[" + APPROVED + "], year=" + YEAR + "}";

    @Autowired
    private NamedMessageSourceAccessor messageSourceAccessor;

    @MockBean
    private ReviewReportingDAO reviewReportingDAO;

    @Autowired
    private ReviewReportingServiceImpl reviewReportingService;

    @Test
    void getLinkedObjectivesData() {
        var reportData = List.of(buildObjectiveLinkedReviewData(1),
                                                            buildObjectiveLinkedReviewData(2));
        when(reviewReportingDAO.getLinkedObjectivesData(any(), any())).thenReturn(reportData);

        final var res = reviewReportingService.getLinkedObjectivesReport(YEAR, List.of(APPROVED));

        assertEquals(getExpectedReportData(reportData), res.getData());
    }

    @Test
    void getLinkedObjectivesDataNotExists() {
        when(reviewReportingDAO.getLinkedObjectivesData(any(), any())).thenReturn(null);

        final var exception = assertThrows(NotFoundException.class,
                () -> reviewReportingService.getLinkedObjectivesReport(YEAR, List.of(APPROVED)));

        assertEquals(REVIEW_REPORT_NOT_FOUND.getCode(), exception.getCode());
        assertEquals(REVIEW_REPORT_NOT_FOUND_MESSAGE, exception.getMessage());
    }

    private List<List<Object>> getExpectedReportData(List<ObjectiveLinkedReviewData> reportData) {
        return reportData.stream()
                .map(ObjectiveLinkedReviewData::toList)
                .collect(Collectors.toList());
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