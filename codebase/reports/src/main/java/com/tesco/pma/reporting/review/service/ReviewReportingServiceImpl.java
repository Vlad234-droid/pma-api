package com.tesco.pma.reporting.review.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.error.ErrorCodeAware;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.reporting.Report;
import com.tesco.pma.reporting.review.dao.ReviewReportingDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;

import static com.tesco.pma.reporting.exception.ErrorCodes.REVIEW_REPORT_NOT_FOUND;
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
    public Report getLinkedObjectivesData(Integer year, List<PMTimelinePointStatus> statuses) {
        var res = reviewReportingDAO.getObjectiveLinkedReviewReport(year, statuses);

        if (isEmpty(res.getObjectives())) {
            throw notFound(REVIEW_REPORT_NOT_FOUND,
                    Map.of(YEAR_PARAMETER_NAME, year,
                           STATUSES_PARAMETER_NAME, statuses));
        }
        return res.getReport();
    }

    private NotFoundException notFound(ErrorCodeAware errorCode, Map<String, ?> params) {
        return new NotFoundException(
                errorCode.getCode(),
                messageSourceAccessor.getMessageForParams(errorCode.getCode(), params),
                null,
                null);
    }
}