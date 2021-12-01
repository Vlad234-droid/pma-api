package com.tesco.pma.review.service;

import com.tesco.pma.api.OrgObjectiveStatus;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.error.ErrorCodeAware;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.exception.ReviewCreationException;
import com.tesco.pma.exception.ReviewDeletionException;
import com.tesco.pma.exception.ReviewUpdateException;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.review.dao.OrgObjectiveDAO;
import com.tesco.pma.review.dao.ReviewAuditLogDAO;
import com.tesco.pma.review.dao.ReviewDAO;
import com.tesco.pma.review.domain.AuditOrgObjectiveReport;
import com.tesco.pma.review.domain.ColleagueTimeline;
import com.tesco.pma.review.domain.OrgObjective;
import com.tesco.pma.review.domain.PMCycleTimelinePoint;
import com.tesco.pma.review.domain.Review;
import com.tesco.pma.review.domain.ReviewStatusCounter;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.tesco.pma.api.ActionType.PUBLISH;
import static com.tesco.pma.api.ActionType.SAVE_AS_DRAFT;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.APPROVED;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.COMPLETED;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.DECLINED;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.DRAFT;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.WAITING_FOR_APPROVAL;
import static com.tesco.pma.cycle.api.PMReviewType.OBJECTIVE;
import static com.tesco.pma.cycle.api.model.PMElementType.REVIEW;
import static com.tesco.pma.review.exception.ErrorCodes.ORG_OBJECTIVES_NOT_FOUND;
import static com.tesco.pma.review.exception.ErrorCodes.ORG_OBJECTIVE_ALREADY_EXISTS;
import static com.tesco.pma.review.exception.ErrorCodes.REVIEW_NOT_FOUND;

/**
 * Implementation of {@link ReviewService}.
 */
@Service
@Validated
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewDAO reviewDAO;
    private final OrgObjectiveDAO orgObjectiveDAO;
    private final ReviewAuditLogDAO reviewAuditLogDAO;
    private final NamedMessageSourceAccessor messageSourceAccessor;
    private final PMCycleService pmCycleService;

    private static final String REVIEW_UUID_PARAMETER_NAME = "reviewUuid";
    private static final String COLLEAGUE_UUID_PARAMETER_NAME = "colleagueUuid";
    private static final String MANAGER_UUID_PARAMETER_NAME = "managerUuid";
    private static final String PERFORMANCE_CYCLE_UUID_PARAMETER_NAME = "performanceCycleUuid";
    private static final String TYPE_PARAMETER_NAME = "type";
    private static final String NUMBER_PARAMETER_NAME = "number";
    private static final String VERSION_PARAMETER_NAME = "version";
    private static final String STATUS_PARAMETER_NAME = "status";
    private static final String PREV_STATUSES_PARAMETER_NAME = "prevStatuses";
    private static final String ALLOWED_STATUSES_PARAMETER_NAME = "allowedStatuses";
    private static final String OPERATION_PARAMETER_NAME = "operation";
    private static final String MAX_PARAMETER_NAME = "max";
    private static final String MIN_PARAMETER_NAME = "min";
    private static final String COUNT_PARAMETER_NAME = "count";
    private static final String CREATE_OPERATION_NAME = "CREATE";
    private static final String DELETE_OPERATION_NAME = "DELETE";
    private static final String UPDATE_OPERATION_NAME = "UPDATE";
    private static final String CHANGE_STATUS_OPERATION_NAME = "CHANGE STATUS";
    private static final Comparator<OrgObjective> ORG_OBJECTIVE_SEQUENCE_NUMBER_TITLE_COMPARATOR =
            Comparator.comparing(OrgObjective::getNumber)
                    .thenComparing(OrgObjective::getTitle);

    @Override
    public Review getReview(UUID performanceCycleUuid, UUID colleagueUuid, PMReviewType type, Integer number) {
//        var res = reviewDAO.getReview(performanceCycleUuid, colleagueUuid, type, number);
//        if (res == null) {
//            throw notFound(REVIEW_NOT_FOUND,
//                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid,
//                            PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, performanceCycleUuid,
//                            TYPE_PARAMETER_NAME, type,
//                            NUMBER_PARAMETER_NAME, number));
//        }
//        return res;
        return null;
    }

    @Override
    public Review getReview(UUID uuid) {
        var res = reviewDAO.read(uuid);
        if (res == null) {
            throw notFound(REVIEW_NOT_FOUND,
                    Map.of(REVIEW_UUID_PARAMETER_NAME, uuid));
        }
        return res;
    }

    @Override
    public List<Review> getReviews(UUID performanceCycleUuid, UUID colleagueUuid, PMReviewType type) {
//        List<Review> results = reviewDAO.getReviews(performanceCycleUuid, colleagueUuid, type);
//        if (results == null) {
//            throw notFound(REVIEW_NOT_FOUND,
//                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid,
//                            PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, performanceCycleUuid,
//                            TYPE_PARAMETER_NAME, type));
//        }
//        return results;
        return null;
    }

    @Override
    public List<Review> getReviewsByColleague(UUID performanceCycleUuid, UUID colleagueUuid) {
//        List<Review> results = reviewDAO.getReviewsByColleague(performanceCycleUuid, colleagueUuid);
//        if (results == null) {
//            throw notFound(REVIEW_NOT_FOUND,
//                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid,
//                            PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, performanceCycleUuid));
//        }
//        return results;
        return null;
    }

    @Override
    public List<ColleagueTimeline> getTeamReviews(UUID managerUuid) {
        List<ColleagueTimeline> results = reviewDAO.getTeamReviews(managerUuid);
        if (results == null) {
            throw notFound(REVIEW_NOT_FOUND,
                    Map.of(MANAGER_UUID_PARAMETER_NAME, managerUuid));
        } else {
            results.forEach(colleagueReviews -> {
                colleagueReviews.setTimeline(getCycleTimelineByColleague(colleagueReviews.getUuid()));
            });
        }
        return results;
    }

    @Override
    @Transactional
    public Review createReview(Review review) {
        return intCreateReview(review);
    }

    @Override
    @Transactional
    public Review updateReview(Review review) {
//        var reviewBefore = reviewDAO.getReview(
//                review.getPerformanceCycleUuid(),
//                review.getColleagueUuid(),
//                review.getType(),
//                review.getNumber());
//        if (null == reviewBefore) {
//            throw notFound(REVIEW_NOT_FOUND,
//                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, review.getColleagueUuid(),
//                            PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, review.getPerformanceCycleUuid(),
//                            TYPE_PARAMETER_NAME, review.getType(),
//                            NUMBER_PARAMETER_NAME, review.getNumber()));
//        }
//        review.setUuid(reviewBefore.getUuid());
//        review.setNumber(reviewBefore.getNumber());
//        return intUpdateReview(review);
        return null;
    }

    @Override
    @Transactional
    public List<Review> updateReviews(UUID performanceCycleUuid,
                                      UUID colleagueUuid,
                                      PMReviewType type,
                                      List<Review> reviews) {
//        List<Review> results = new ArrayList<>();
//        for (int idx = 0; idx < reviews.size(); idx++) {
//            var review = reviews.get(idx);
//            review.setPerformanceCycleUuid(performanceCycleUuid);
//            review.setColleagueUuid(colleagueUuid);
//            review.setType(type);
//            var reviewBefore = reviewDAO.getReview(
//                    review.getPerformanceCycleUuid(),
//                    review.getColleagueUuid(),
//                    review.getType(),
//                    idx + 1);
//            if (null == reviewBefore) {
//                review.setNumber(idx + 1);
//                intCreateReview(review);
//            } else {
//                review.setUuid(reviewBefore.getUuid());
//                review.setNumber(reviewBefore.getNumber());
//                intUpdateReview(review);
//            }
//            results.add(review);
//        }
//        List<Review> prevReviews = reviewDAO.getReviews(performanceCycleUuid, colleagueUuid, type);
//        if (prevReviews.size() > reviews.size()) {
//            for (int i = reviews.size() + 1; i <= prevReviews.size(); i++) {
//                intDeleteReview(performanceCycleUuid, colleagueUuid, type, i);
//            }
//        }
//
//        checkReviewStateAfterUpdate(performanceCycleUuid, colleagueUuid, type);
//
//        return results;
        return null;
    }

    @Override
    @Transactional
    public PMTimelinePointStatus updateReviewsStatus(UUID performanceCycleUuid,
                                                     UUID colleagueUuid,
                                                     PMReviewType type,
                                                     List<Review> reviews,
                                                     PMTimelinePointStatus status,
                                                     String reason,
                                                     UUID loggedUserUuid) {
//        reviews.forEach(review -> {
//            var prevStatuses = getPrevStatusesForChangeStatus(status);
//            if (0 == prevStatuses.size()) {
//                throw notFound(ALLOWED_STATUSES_NOT_FOUND,
//                        Map.of(OPERATION_PARAMETER_NAME, CHANGE_STATUS_OPERATION_NAME));
//            }
//            if (1 == reviewDAO.updateReviewStatus(
//                    performanceCycleUuid,
//                    colleagueUuid,
//                    type,
//                    review.getNumber(),
//                    status,
//                    prevStatuses)) {
//                var actualReview = reviewDAO.getReview(
//                        performanceCycleUuid,
//                        colleagueUuid,
//                        type,
//                        review.getNumber());
//                reviewAuditLogDAO.logReviewUpdating(actualReview, status, reason, loggedUserUuid);
//            } else {
//                throw notFound(REVIEW_NOT_FOUND,
//                        Map.of(STATUS_PARAMETER_NAME, status,
//                                COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid,
//                                PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, performanceCycleUuid,
//                                TYPE_PARAMETER_NAME, type,
//                                NUMBER_PARAMETER_NAME, review.getNumber(),
//                                PREV_STATUSES_PARAMETER_NAME, prevStatuses));
//            }
//        });
//
//        return status;
        return null;
    }

    @Override
    @Transactional
    public void deleteReview(UUID performanceCycleUuid,
                             UUID colleagueUuid,
                             PMReviewType type,
                             Integer number) {
        intDeleteReview(
                performanceCycleUuid,
                colleagueUuid,
                type,
                number);
//        reviewDAO.renumerateReviews(
//                performanceCycleUuid,
//                colleagueUuid,
//                type,
//                number + 1);
    }

    @Override
    @Transactional
    public List<OrgObjective> createOrgObjectives(List<OrgObjective> orgObjectives, UUID loggedUserUuid) {
        return intCreateOrgObjectives(orgObjectives, loggedUserUuid);
    }

    @Override
    public List<OrgObjective> getAllOrgObjectives() {
        List<OrgObjective> results = orgObjectiveDAO.getAll();
        if (results == null) {
            throw notFound(ORG_OBJECTIVES_NOT_FOUND, Map.of());
        }
        return results;
    }

    @Override
    @Transactional
    public List<OrgObjective> publishOrgObjectives(UUID loggedUserUuid) {
        return intPublishOrgObjectives(loggedUserUuid);
    }

    @Override
    @Transactional
    public List<OrgObjective> createAndPublishOrgObjectives(List<OrgObjective> orgObjectives, UUID loggedUserUuid) {
        intCreateOrgObjectives(orgObjectives, loggedUserUuid);
        return intPublishOrgObjectives(loggedUserUuid);
    }

    @Override
    public List<OrgObjective> getPublishedOrgObjectives() {
        List<OrgObjective> results = orgObjectiveDAO.getAllPublished();
        if (results == null) {
            throw notFound(ORG_OBJECTIVES_NOT_FOUND, Map.of());
        }
        return results;
    }

    private DatabaseConstraintViolationException databaseConstraintViolation(ErrorCodeAware errorCode,
                                                                             Map<String, ?> params,
                                                                             Throwable cause) {
        return new DatabaseConstraintViolationException(errorCode.getCode(),
                messageSourceAccessor.getMessage(errorCode.getCode(), params), null, cause);
    }

    @Override
    public List<PMCycleTimelinePoint> getCycleTimelineByColleague(UUID colleagueUuid) {
        var currentCycleUuid = pmCycleService.getCurrentByColleague(colleagueUuid).getUuid();
        var cycleTimeline = reviewDAO.getTimeline(currentCycleUuid);

        for (PMCycleTimelinePoint timelinePoint :
                cycleTimeline) {
            if (REVIEW == timelinePoint.getType()) {
                var reviewStatusCounter = getTimelineReviewStatusCounter(
                        currentCycleUuid,
                        colleagueUuid,
                        timelinePoint.getReviewType());
                if (null != reviewStatusCounter) {
                    timelinePoint.setStatus(reviewStatusCounter.getStatus());
                    timelinePoint.setCount(reviewStatusCounter.getCount());
                }
            }
        }
        return cycleTimeline;
    }

    @Override
    public List<AuditOrgObjectiveReport> getAuditOrgObjectiveReport(RequestQuery requestQuery) {
        return reviewAuditLogDAO.getAuditOrgObjectiveReport(requestQuery);
    }

    private ReviewStatusCounter getTimelineReviewStatusCounter(UUID cycleUuid, UUID colleagueUuid, PMReviewType reviewType) {
//        var reviewStats = reviewDAO.getReviewStats(cycleUuid, colleagueUuid, reviewType);
//        if (null == reviewStats || 0 == reviewStats.getStatusStats().size()) {
//            return null;
//        }
//        if (OBJECTIVE == reviewType) {
//            var mapStatusStats = reviewStats.getMapStatusStats();
//            var minObjectives = reviewDAO.getPMCycleReviewTypeProperties(cycleUuid, reviewType).getMin();
//            if (mapStatusStats.containsKey(APPROVED) && mapStatusStats.get(APPROVED) >= minObjectives) {
//                return new ReviewStatusCounter(APPROVED, mapStatusStats.get(APPROVED));
//            } else if (mapStatusStats.containsKey(WAITING_FOR_APPROVAL) && mapStatusStats.get(WAITING_FOR_APPROVAL) >= minObjectives) {
//                return new ReviewStatusCounter(WAITING_FOR_APPROVAL, mapStatusStats.get(WAITING_FOR_APPROVAL));
//            } else if (mapStatusStats.containsKey(DECLINED)) {
//                return new ReviewStatusCounter(DECLINED, mapStatusStats.get(DECLINED));
//            } else if (mapStatusStats.containsKey(DRAFT)) {
//                return new ReviewStatusCounter(DRAFT, mapStatusStats.get(DRAFT));
//            } else {
//                return null;
//            }
//        } else {
//            return reviewStats.getStatusStats().get(0);
//        }
        return null;
    }

    private static <T> boolean listEqualsIgnoreOrder(List<T> list1, List<T> list2, Comparator<? super T> comparator) {

        if (list1.size() != list2.size()) {
            return false;
        }

        List<T> copy1 = new ArrayList<>(list1);
        List<T> copy2 = new ArrayList<>(list2);

        copy1.sort(comparator);
        copy2.sort(comparator);

        Iterator<T> it1 = copy1.iterator();
        Iterator<T> it2 = copy2.iterator();
        while (it1.hasNext()) {
            T t1 = it1.next();
            T t2 = it2.next();
            if (comparator.compare(t1, t2) != 0) {
                return false;
            }
        }
        return true;
    }

    private List<PMTimelinePointStatus> getAllowedStatusesForUpdate(PMReviewType reviewType, PMTimelinePointStatus newStatus) {
        var allowedStatusesForUpdate = getStatusesForUpdate(reviewType);
        var prevStatusesForChangeStatus = getPrevStatusesForChangeStatus(newStatus);
        return allowedStatusesForUpdate.stream()
                .filter(prevStatusesForChangeStatus::contains)
                .collect(Collectors.toList());
    }

    public Review intCreateReview(Review review) {
//        review.setUuid(UUID.randomUUID());
//        var reviewTypeProperties = reviewDAO.getPMCycleReviewTypeProperties(review.getPerformanceCycleUuid(), review.getType());
//        if (reviewTypeProperties.getMax() < review.getNumber()) {
//            throw createReviewException(
//                    MAX_REVIEW_NUMBER_CONSTRAINT_VIOLATION,
//                    Map.of(MAX_PARAMETER_NAME, reviewTypeProperties.getMax(),
//                            NUMBER_PARAMETER_NAME, review.getNumber())
//            );
//        }
//        try {
//            var allowedStatuses = getStatusesForCreate();
//            if (0 == allowedStatuses.size()) {
//                throw notFound(ALLOWED_STATUSES_NOT_FOUND,
//                        Map.of(OPERATION_PARAMETER_NAME, CREATE_OPERATION_NAME));
//            }
//            if (allowedStatuses.contains(review.getStatus())) {
//                reviewDAO.create(review);
//                return review;
//            } else {
//                throw createReviewException(
//                        REVIEW_STATUS_NOT_ALLOWED,
//                        Map.of(STATUS_PARAMETER_NAME, review.getStatus(),
//                                OPERATION_PARAMETER_NAME, CREATE_OPERATION_NAME)
//                );
//            }
//        } catch (DuplicateKeyException e) {
//            throw databaseConstraintViolation(
//                    REVIEW_ALREADY_EXISTS,
//                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, review.getColleagueUuid(),
//                            PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, review.getPerformanceCycleUuid(),
//                            TYPE_PARAMETER_NAME, review.getType(),
//                            NUMBER_PARAMETER_NAME, review.getNumber()),
//                    e);
//        }
        return null;
    }

    private Review intUpdateReview(Review review) {
//        var allowedStatuses = getAllowedStatusesForUpdate(review.getType(), review.getStatus());
//        if (0 == allowedStatuses.size()) {
//            throw notFound(ALLOWED_STATUSES_NOT_FOUND,
//                    Map.of(OPERATION_PARAMETER_NAME, UPDATE_OPERATION_NAME));
//        }
//
//        if (1 == reviewDAO.update(review, allowedStatuses)) {
//            return review;
//        } else {
//            throw notFound(REVIEW_NOT_FOUND,
//                    Map.of(OPERATION_PARAMETER_NAME, UPDATE_OPERATION_NAME,
//                            COLLEAGUE_UUID_PARAMETER_NAME, review.getColleagueUuid(),
//                            PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, review.getPerformanceCycleUuid(),
//                            TYPE_PARAMETER_NAME, review.getType(),
//                            NUMBER_PARAMETER_NAME, review.getNumber(),
//                            ALLOWED_STATUSES_PARAMETER_NAME, allowedStatuses));
//        }
        return null;
    }

    private void intDeleteReview(UUID performanceCycleUuid,
                                 UUID colleagueUuid,
                                 PMReviewType type,
                                 Integer number) {
//        var reviewTypeProperties = reviewDAO.getPMCycleReviewTypeProperties(performanceCycleUuid, type);
//        var reviewCount = reviewDAO.getReviewStats(performanceCycleUuid, colleagueUuid, type)
//                .getStatusStats()
//                .stream()
//                .mapToInt(ReviewStatusCounter::getCount)
//                .sum();
//        if (reviewCount == reviewTypeProperties.getMin()) {
//            throw deleteReviewException(CANNOT_DELETE_REVIEW_COUNT_CONSTRAINT,
//                    Map.of(MIN_PARAMETER_NAME, reviewTypeProperties.getMin()));
//        }
//
//        var allowedStatuses = getStatusesForDelete(type);
//        if (0 == allowedStatuses.size()) {
//            throw notFound(ALLOWED_STATUSES_NOT_FOUND,
//                    Map.of(OPERATION_PARAMETER_NAME, DELETE_OPERATION_NAME));
//        }
//        if (1 != reviewDAO.deleteReview(
//                performanceCycleUuid,
//                colleagueUuid,
//                type,
//                number,
//                allowedStatuses)) {
//            throw notFound(REVIEW_NOT_FOUND,
//                    Map.of(OPERATION_PARAMETER_NAME, DELETE_OPERATION_NAME,
//                            COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid,
//                            PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, performanceCycleUuid,
//                            TYPE_PARAMETER_NAME, type,
//                            NUMBER_PARAMETER_NAME, number,
//                            ALLOWED_STATUSES_PARAMETER_NAME, allowedStatuses));
//        }
    }

    private List<OrgObjective> intCreateOrgObjectives(List<OrgObjective> orgObjectives, UUID loggedUserUuid) {
        var currentOrgObjectives = orgObjectiveDAO.getAll();
        if (listEqualsIgnoreOrder(currentOrgObjectives, orgObjectives, ORG_OBJECTIVE_SEQUENCE_NUMBER_TITLE_COMPARATOR)) {
            return currentOrgObjectives;
        }
        List<OrgObjective> results = new ArrayList<>();
        final var newVersion = orgObjectiveDAO.getMaxVersion() + 1;
        orgObjectives.forEach(orgObjective -> {
            try {
                orgObjective.setUuid(UUID.randomUUID());
                orgObjective.setStatus(OrgObjectiveStatus.DRAFT);
                orgObjective.setVersion(newVersion);
                orgObjectiveDAO.create(orgObjective);
                results.add(orgObjective);
            } catch (DuplicateKeyException e) {
                throw databaseConstraintViolation(
                        ORG_OBJECTIVE_ALREADY_EXISTS,
                        Map.of(NUMBER_PARAMETER_NAME, orgObjective.getNumber(),
                                VERSION_PARAMETER_NAME, orgObjective.getVersion()),
                        e);

            }

        });
        reviewAuditLogDAO.logOrgObjectiveAction(SAVE_AS_DRAFT, loggedUserUuid);
        return results;
    }

    private List<OrgObjective> intPublishOrgObjectives(UUID loggedUserUuid) {
        orgObjectiveDAO.unpublish();
        if (0 == orgObjectiveDAO.publish()) {
            throw notFound(ORG_OBJECTIVES_NOT_FOUND, Map.of());
        }
        reviewAuditLogDAO.logOrgObjectiveAction(PUBLISH, loggedUserUuid);
        return getPublishedOrgObjectives();
    }

    private List<PMTimelinePointStatus> getStatusesForCreate() {
        return List.of(DRAFT, WAITING_FOR_APPROVAL);
    }

    private List<PMTimelinePointStatus> getStatusesForUpdate(PMReviewType reviewType) {
        if (reviewType.equals(OBJECTIVE)) {
            return List.of(DRAFT, DECLINED, APPROVED);
        } else {
            return List.of(DRAFT, DECLINED);
        }
    }

    private List<PMTimelinePointStatus> getStatusesForDelete(PMReviewType reviewType) {
        if (reviewType.equals(OBJECTIVE)) {
            return List.of(DRAFT, DECLINED, APPROVED);
        } else {
            return Collections.emptyList();
        }
    }

    private List<PMTimelinePointStatus> getPrevStatusesForChangeStatus(PMTimelinePointStatus newStatus) {
        switch (newStatus) {
            case DRAFT:
                return List.of(DRAFT);
            case WAITING_FOR_APPROVAL:
                return List.of(DRAFT, WAITING_FOR_APPROVAL, DECLINED);
            case APPROVED:
                return List.of(WAITING_FOR_APPROVAL, APPROVED);
            case DECLINED:
                return List.of(WAITING_FOR_APPROVAL, DECLINED);
            case COMPLETED:
                return List.of(APPROVED, COMPLETED);
            default:
                return Collections.emptyList();
        }
    }

    private void checkReviewStateAfterUpdate(UUID performanceCycleUuid,
                                             UUID colleagueUuid,
                                             PMReviewType type) {
//        if (OBJECTIVE == type) {
//
//            var reviewTypeProperties = reviewDAO.getPMCycleReviewTypeProperties(performanceCycleUuid, type);
//            var mapStatusStats = reviewDAO.getReviewStats(performanceCycleUuid, colleagueUuid, type).getMapStatusStats();
//
//            if (mapStatusStats.containsKey(WAITING_FOR_APPROVAL)
//                    && mapStatusStats.get(WAITING_FOR_APPROVAL) < reviewTypeProperties.getMin()) {
//                throw updateReviewException(MIN_REVIEW_NUMBER_CONSTRAINT_VIOLATION,
//                        Map.of(COUNT_PARAMETER_NAME, mapStatusStats.get(WAITING_FOR_APPROVAL),
//                                STATUS_PARAMETER_NAME, WAITING_FOR_APPROVAL,
//                                MIN_PARAMETER_NAME, reviewTypeProperties.getMin()));
//            }
//        }
    }

    private NotFoundException notFound(ErrorCodeAware errorCode, Map<String, ?> params) {
        return new NotFoundException(
                errorCode.getCode(),
                messageSourceAccessor.getMessageForParams(errorCode.getCode(), params),
                null,
                null);
    }

    private ReviewCreationException createReviewException(ErrorCodeAware errorCode, Map<String, ?> params) {
        return new ReviewCreationException(errorCode.getCode(), messageSourceAccessor.getMessage(errorCode.getCode(), params), null, null);
    }

    private ReviewUpdateException updateReviewException(ErrorCodeAware errorCode, Map<String, ?> params) {
        return new ReviewUpdateException(errorCode.getCode(), messageSourceAccessor.getMessage(errorCode.getCode(), params), null, null);
    }

    private ReviewDeletionException deleteReviewException(ErrorCodeAware errorCode, Map<String, ?> params) {
        return new ReviewDeletionException(errorCode.getCode(), messageSourceAccessor.getMessage(errorCode.getCode(), params), null, null);
    }
}