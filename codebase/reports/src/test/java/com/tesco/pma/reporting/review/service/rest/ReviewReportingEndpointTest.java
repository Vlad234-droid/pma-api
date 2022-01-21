package com.tesco.pma.reporting.review.service.rest;

import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.pagination.Condition;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.reporting.Report;
import com.tesco.pma.reporting.review.LocalTestConfig;
import com.tesco.pma.reporting.review.service.ReviewReportingService;
import com.tesco.pma.rest.AbstractEndpointTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.List;

import static com.tesco.pma.cycle.api.PMTimelinePointStatus.APPROVED;
import static com.tesco.pma.pagination.Condition.Operand.EQUALS;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.COLLEAGUE_UUID;
import static com.tesco.pma.reporting.metadata.ColumnMetadataEnum.IAM_ID;
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
import static com.tesco.pma.reporting.review.service.rest.ReviewReportingEndpoint.APPLICATION_FORCE_DOWNLOAD_VALUE;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
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
    private static final Integer YEAR = 2021;
    private static final String LINKED_OBJECTIVE_REVIEW_REPORT_URL = "/linked-objective-report";

    @MockBean
    private ReviewReportingService service;

    @Test
    void getLinkedObjectivesData() throws Exception {
        when(service.getLinkedObjectivesData(any(), any())).thenReturn(buildReport());

        var result = performGetWith(admin(), status().isCreated(),
                APPLICATION_FORCE_DOWNLOAD_VALUE, LINKED_OBJECTIVE_REVIEW_REPORT_URL, buildRequestQuery());

        assertThat(result.getResponse().getContentAsByteArray()).hasSizeGreaterThan(0);
    }

    @Test
    void cannotGetLinkedObjectivesDataIfUnauthorized() throws Exception {
        mvc.perform(get(LINKED_OBJECTIVE_REVIEW_REPORT_URL, new RequestQuery())
                        .with(anonymous())
                        .accept(APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    @Test
    void getLinkedObjectivesDataIfNotFound() throws Exception {
        when(service.getLinkedObjectivesData(any(), any())).thenThrow(NotFoundException.class);

        performGetWith(talentAdmin(), status().isNotFound(),
                LINKED_OBJECTIVE_REVIEW_REPORT_URL, new RequestQuery());
    }

    private RequestQuery buildRequestQuery() {
        var requestQuery = new RequestQuery();
        requestQuery.setFilters(asList(
                new Condition("year", EQUALS, YEAR),
                new Condition("statuses", EQUALS, Arrays.asList(APPROVED.getCode()))));

        return requestQuery;
    }

    private Report buildReport() {
        var report = new Report();
        report.setData(List.of(
                List.of("UKE12375189", COLLEAGUE_UUID_STR, "Name", "Surname", "WL5", "JobTitle", "UKE12375188", 1,
                        "APPROVED", "Priority", "Title", "How Achieved", "How Over-Achieved")));
        report.setMetadata(List.of(
                IAM_ID.getColumnMetadata(),
                COLLEAGUE_UUID.getColumnMetadata(),
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