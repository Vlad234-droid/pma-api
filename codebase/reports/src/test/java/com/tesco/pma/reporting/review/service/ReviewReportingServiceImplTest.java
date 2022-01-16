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

import java.util.UUID;

import static com.tesco.pma.cycle.api.PMTimelinePointStatus.APPROVED;
import static com.tesco.pma.reporting.exception.ErrorCodes.REVIEW_REPORT_NOT_FOUND;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(classes = {ReviewReportingServiceImpl.class, LocalServiceTestConfig.class})
@ExtendWith(MockitoExtension.class)
class ReviewReportingServiceImplTest {

    private static final String COLLEAGUE_UUID = "10000000-0000-0000-0000-000000000000";
    private static final UUID TIMELINE_POINT_UUID = UUID.fromString("10000000-0000-0000-2000-000000000000");

    private static final String REVIEW_REPORT_NOT_FOUND_MESSAGE =
            "Review report not found for: {status=APPROVED, tlPointUuid=10000000-0000-0000-2000-000000000000}";

    @Autowired
    private NamedMessageSourceAccessor messageSourceAccessor;

    @MockBean
    private ReviewReportingDAO reviewReportingDAO;

    @Autowired
    private ReviewReportingServiceImpl reviewReportingService;

    @Test
    void getLinkedObjectivesData() {
        var report = new ObjectiveLinkedReviewReport();
        report.setEmployeeUUID(COLLEAGUE_UUID);
        report.setWorkLevel("WL5");
        when(reviewReportingDAO.getLinkedObjectivesData(any(), any())).thenReturn(report);

        final var res = reviewReportingService.getLinkedObjectivesData(TIMELINE_POINT_UUID, APPROVED);

        assertThat(res).isSameAs(report);
    }

    @Test
    void getLinkedObjectivesDataNotExists() {
        when(reviewReportingDAO.getLinkedObjectivesData(any(), any())).thenReturn(null);

        final var exception = assertThrows(NotFoundException.class,
                () -> reviewReportingService.getLinkedObjectivesData(TIMELINE_POINT_UUID, APPROVED));

        assertEquals(REVIEW_REPORT_NOT_FOUND.getCode(), exception.getCode());
        assertEquals(REVIEW_REPORT_NOT_FOUND_MESSAGE, exception.getMessage());
    }
}