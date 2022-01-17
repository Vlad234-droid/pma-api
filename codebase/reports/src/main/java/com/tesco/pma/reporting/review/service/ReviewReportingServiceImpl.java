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

import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.cycle.api.PMTimelinePointStatus.APPROVED;
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

    private static final String TL_POINT_UUID_PARAMETER_NAME = "tlPointUuid";
    private static final String STATUS_PARAMETER_NAME = "status";

    @Override
    public Report getLinkedObjectivesData(UUID tlPointUuid, PMTimelinePointStatus status) {
        var res = reviewReportingDAO.getLinkedObjectivesData(tlPointUuid,
                (status == null) ? APPROVED : status);

        if (res == null) {
            throw notFound(REVIEW_REPORT_NOT_FOUND,
                    Map.of(TL_POINT_UUID_PARAMETER_NAME, tlPointUuid,
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