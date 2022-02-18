package com.tesco.pma.reporting.review.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.error.ErrorCodeAware;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.reporting.Report;
import com.tesco.pma.reporting.ReportingConstants;
import com.tesco.pma.reporting.exception.ErrorCodes;
import com.tesco.pma.reporting.review.dao.ReviewReportingDAO;
import com.tesco.pma.reporting.review.domain.provider.ObjectiveLinkedReviewReportProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Service for review reporting data
 */
@Service
@Validated
@RequiredArgsConstructor
public class ReviewReportingServiceImpl implements ReviewReportingService {
    private final ReviewReportingDAO reviewReportingDAO;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Override
    public Report getLinkedObjectivesReport(RequestQuery requestQuery) {
        var reportProvider = new ObjectiveLinkedReviewReportProvider();
        reportProvider.setObjectives(reviewReportingDAO.getLinkedObjectivesData(requestQuery));

        if (isEmpty(reportProvider.getObjectives())) {
            throw notFound(ErrorCodes.REPORT_NOT_FOUND, Map.of(ReportingConstants.QUERY_PARAMS, requestQuery));
        }
        return reportProvider.getReport();
    }

    private NotFoundException notFound(ErrorCodeAware errorCode, Map<String, ?> params) {
        return new NotFoundException(
                errorCode.getCode(),
                messageSourceAccessor.getMessageForParams(errorCode.getCode(), params),
                null,
                null);
    }
}