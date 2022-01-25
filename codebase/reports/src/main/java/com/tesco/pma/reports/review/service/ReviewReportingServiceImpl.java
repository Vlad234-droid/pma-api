package com.tesco.pma.reports.review.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.error.ErrorCodeAware;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.reporting.Report;
import com.tesco.pma.reports.review.dao.ReviewReportingDAO;
import com.tesco.pma.reports.exception.ErrorCodes;
import com.tesco.pma.reports.review.domain.provider.ObjectiveLinkedReviewReportProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Service for mapping data management
 */
@Service
@Validated
@RequiredArgsConstructor
public class ReviewReportingServiceImpl implements ReviewReportingService {

    private final ReviewReportingDAO reviewReportingDAO;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    private static final String YEAR_PARAMETER_NAME = "year";
    private static final String STATUSES_PARAMETER_NAME = "statuses";

    @Override
    public Report getLinkedObjectivesReport(Integer year, List<PMTimelinePointStatus> statuses) {
        var reportProvider = getLinkedObjectivesReportProvider(year, statuses);

        if (isEmpty(reportProvider.getObjectives())) {
            throw notFound(ErrorCodes.REVIEW_REPORT_NOT_FOUND,
                    Map.of(YEAR_PARAMETER_NAME, year,
                           STATUSES_PARAMETER_NAME, statuses));
        }
        return reportProvider.getReport();
    }

    /**
     * Find Objectives linked with reviews
     *
     * @param year        - time of colleague cycle
     * @param statuses    - statuses of review
     * @return linked Objectives report data
     */
    private ObjectiveLinkedReviewReportProvider getLinkedObjectivesReportProvider(Integer year, List<PMTimelinePointStatus> statuses) {
        var report = new ObjectiveLinkedReviewReportProvider();
        report.setObjectives(reviewReportingDAO.getLinkedObjectivesData(year, statuses));

        return report;
    }

    private NotFoundException notFound(ErrorCodeAware errorCode, Map<String, ?> params) {
        return new NotFoundException(
                errorCode.getCode(),
                messageSourceAccessor.getMessageForParams(errorCode.getCode(), params),
                null,
                null);
    }
}