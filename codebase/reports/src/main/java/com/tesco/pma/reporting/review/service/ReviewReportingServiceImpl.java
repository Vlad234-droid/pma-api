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

import java.time.Instant;
import java.util.Map;

import static com.tesco.pma.reporting.exception.ErrorCodes.REVIEW_REPORT_NOT_FOUND;

/**
 * Service for mapping data management
 */
@Service
@Validated
@RequiredArgsConstructor
public class ReviewReportingServiceImpl implements ReviewReportingService {

    private final ReviewReportingDAO reviewReportingDAO;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    private static final String START_TIME_PARAMETER_NAME = "startTime";
    private static final String END_TIME_PARAMETER_NAME = "endTime";
    private static final String STATUS_PARAMETER_NAME = "status";

    @Override
    public Report getLinkedObjectivesData(Instant startTime, Instant endTime, PMTimelinePointStatus status) {
        var res = reviewReportingDAO.getLinkedObjectivesData(startTime, endTime, status);

        if (res == null) {
            throw notFound(REVIEW_REPORT_NOT_FOUND,
                    Map.of(START_TIME_PARAMETER_NAME, startTime,
                            END_TIME_PARAMETER_NAME, endTime,
                            STATUS_PARAMETER_NAME, status));
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