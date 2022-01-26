package com.tesco.pma.reports.review.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.error.ErrorCodeAware;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.pagination.Condition;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.reporting.Report;
import com.tesco.pma.reports.review.dao.ReviewReportingDAO;
import com.tesco.pma.reports.exception.ErrorCodes;
import com.tesco.pma.reports.review.domain.provider.ObjectiveLinkedReviewReportProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

import static com.tesco.pma.cycle.api.PMTimelinePointStatus.APPROVED;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Service for mapping data management
 */
@Service
@Validated
@RequiredArgsConstructor
public class ReviewReportingServiceImpl implements ReviewReportingService {

    static final String STATUSES_PARAMETER_NAME = "statuses";
    static final PMTimelinePointStatus DEFAULT_STATUS = APPROVED;
    static final String QUERY_PARAMS = "queryParams";

    private final ReviewReportingDAO reviewReportingDAO;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Override
    public Report getLinkedObjectivesReport(RequestQuery requestQuery) {
        setStatuses(requestQuery);

        var reportProvider = new ObjectiveLinkedReviewReportProvider();
        reportProvider.setObjectives(reviewReportingDAO.getLinkedObjectivesData(requestQuery));

        if (isEmpty(reportProvider.getObjectives())) {
            throw notFound(ErrorCodes.REVIEW_REPORT_NOT_FOUND, Map.of(QUERY_PARAMS, requestQuery));
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

    private Object getProperty(RequestQuery requestQuery, String propertyName) {
        var dateTime = requestQuery.getFilters().stream()
                .filter(cond -> propertyName.equalsIgnoreCase(cond.getProperty()))
                .findFirst();
        return dateTime.map(Condition::getValue).orElse(null);
    }

    private void setStatuses(RequestQuery requestQuery) {
        if (getProperty(requestQuery, STATUSES_PARAMETER_NAME) == null) {
            requestQuery.addFilters(STATUSES_PARAMETER_NAME, DEFAULT_STATUS.getId());
        }
    }
}