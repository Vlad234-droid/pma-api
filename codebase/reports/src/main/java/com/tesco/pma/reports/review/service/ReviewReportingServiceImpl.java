package com.tesco.pma.reports.review.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.error.ErrorCodeAware;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.reporting.Report;
import com.tesco.pma.reports.domain.ColleagueReportTargeting;
import com.tesco.pma.reports.review.dao.ReviewReportingDAO;
import com.tesco.pma.reports.exception.ErrorCodes;
import com.tesco.pma.reports.review.domain.ReviewStatsData;
import com.tesco.pma.reports.review.domain.provider.ObjectiveLinkedReviewReportProvider;
import com.tesco.pma.reports.review.domain.provider.ReviewStatsReportProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Service for mapping data management
 */
@Service
@Validated
@RequiredArgsConstructor
public class ReviewReportingServiceImpl implements ReviewReportingService {
    static final String QUERY_PARAMS = "queryParams";

    private final ReviewReportingDAO reviewReportingDAO;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    @Override
    public Report getLinkedObjectivesReport(RequestQuery requestQuery) {
        var reportProvider = new ObjectiveLinkedReviewReportProvider();
        reportProvider.setObjectives(reviewReportingDAO.getLinkedObjectivesData(requestQuery));

        if (isEmpty(reportProvider.getObjectives())) {
            throw notFound(ErrorCodes.REVIEW_REPORT_NOT_FOUND, Map.of(QUERY_PARAMS, requestQuery));
        }
        return reportProvider.getReport();
    }

    @Override
    public List<ColleagueReportTargeting> getReviewReportColleagues(RequestQuery requestQuery) {
        return Optional.ofNullable(reviewReportingDAO.getColleagueTargeting(requestQuery))
                .orElseThrow(() -> notFound(ErrorCodes.REVIEW_REPORT_NOT_FOUND, Map.of(QUERY_PARAMS, requestQuery)));
    }

    @Override
    public Report getReviewStatsReport(RequestQuery requestQuery) {
        var targetingColleagues = getReviewReportColleagues(requestQuery);
        var statsReportProvider = new ReviewStatsReportProvider();
        statsReportProvider.setData(findReviewStatsData(targetingColleagues));

        return new ReviewStatsReportProvider().getReport();
    }

    private List<ReviewStatsData> findReviewStatsData(List<ColleagueReportTargeting> targetingColleagues) {
        // TODO: implement logic with filter, count, division here to get needed statistics in percentage
        return null;
    }

    private NotFoundException notFound(ErrorCodeAware errorCode, Map<String, ?> params) {
        return new NotFoundException(
                errorCode.getCode(),
                messageSourceAccessor.getMessageForParams(errorCode.getCode(), params),
                null,
                null);
    }
}