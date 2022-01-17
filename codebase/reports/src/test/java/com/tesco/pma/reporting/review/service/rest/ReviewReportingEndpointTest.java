package com.tesco.pma.reporting.review.service.rest;

import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.reporting.Report;
import com.tesco.pma.reporting.review.LocalTestConfig;
import com.tesco.pma.reporting.review.service.ReviewReportingService;
import com.tesco.pma.rest.AbstractEndpointTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.UUID;

import static com.tesco.pma.cycle.api.PMTimelinePointStatus.APPROVED;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.EMPLOYEE_NO;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.EMPLOYEE_UUID;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.FIRST_NAME;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.HOW_ACHIEVED;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.HOW_OVER_ACHIEVED;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.JOB_TITLE;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.LAST_NAME;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.LINE_MANAGER;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.OBJECTIVE_NUMBER;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.OBJECTIVE_TITLE;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.STATUS;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.STRATEGIC_PRIORITY;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.WORKING_LEVEL;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReviewReportingEndpoint.class)
@ContextConfiguration(classes = {LocalTestConfig.class, ReviewReportingEndpoint.class})
class ReviewReportingEndpointTest extends AbstractEndpointTest {

    private static final String COLLEAGUE_UUID = "10000000-0000-0000-0000-000000000000";
    private static final UUID TIMELINE_POINT_UUID = UUID.fromString("10000000-0000-0000-2000-000000000000");
    private static final String LINKED_OBJECTIVE_REVIEW_REPORT_URL =
            "/review-reports/pm-linked-objective-report/timeline-points/{tlPointUuid}/statuses/{status}";
    private static final String LINKED_OBJECTIVES_REPORT_GET_RESPONSE_JSON_FILE_NAME = "linked_objectives_report_get_ok_response.json";

    @MockBean
    private ReviewReportingService service;

    @Test
    void getLinkedObjectivesData() throws Exception {
        var report = buildReport();
        when(service.getLinkedObjectivesData(TIMELINE_POINT_UUID, APPROVED)).thenReturn(report);

        var result = performGetWith(admin(), status().isOk(),
                LINKED_OBJECTIVE_REVIEW_REPORT_URL, TIMELINE_POINT_UUID, APPROVED.getCode());

        assertResponseContent(result.getResponse(), LINKED_OBJECTIVES_REPORT_GET_RESPONSE_JSON_FILE_NAME);
    }

    @Test
    void cannotGetLinkedObjectivesDataIfUnauthorized() throws Exception {
        mvc.perform(get(LINKED_OBJECTIVE_REVIEW_REPORT_URL, TIMELINE_POINT_UUID, APPROVED.getCode())
                        .with(anonymous())
                        .accept(APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    @Test
    void getLinkedObjectivesDataIfNotFound() throws Exception {
        when(service.getLinkedObjectivesData(TIMELINE_POINT_UUID, APPROVED)).thenThrow(NotFoundException.class);

        performGetWith(admin(), status().isNotFound(),
                LINKED_OBJECTIVE_REVIEW_REPORT_URL, TIMELINE_POINT_UUID, APPROVED.getCode());
    }

    private Report buildReport() {
        var report = new Report();
        report.setData(List.of(
                List.of("UKE12375189", COLLEAGUE_UUID, "Name", "Surname", "WL5", "JobTitle", "UKE12375188", 1,
                        "APPROVED", "Priority", "Title", "How Achieved", "How Over-Achieved")));
        report.setMetadata(List.of(
                EMPLOYEE_NO.getColumnMetadata(),
                EMPLOYEE_UUID.getColumnMetadata(),
                FIRST_NAME.getColumnMetadata(),
                LAST_NAME.getColumnMetadata(),
                WORKING_LEVEL.getColumnMetadata(),
                JOB_TITLE.getColumnMetadata(),
                LINE_MANAGER.getColumnMetadata(),
                OBJECTIVE_NUMBER.getColumnMetadata(),
                STATUS.getColumnMetadata(),
                STRATEGIC_PRIORITY.getColumnMetadata(),
                OBJECTIVE_TITLE.getColumnMetadata(),
                HOW_ACHIEVED.getColumnMetadata(),
                HOW_OVER_ACHIEVED.getColumnMetadata()
        ));

        return report;
    }
}