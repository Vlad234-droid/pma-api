package com.tesco.pma.review.service;

import com.tesco.pma.api.OrgObjectiveStatus;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.cycle.service.PMColleagueCycleService;
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
import com.tesco.pma.review.dao.TimelinePointDAO;
import com.tesco.pma.review.domain.AuditOrgObjectiveReport;
import com.tesco.pma.review.domain.ColleagueTimeline;
import com.tesco.pma.review.domain.OrgObjective;
import com.tesco.pma.review.domain.Review;
import com.tesco.pma.review.domain.ReviewStatusCounter;
import com.tesco.pma.review.domain.TimelinePoint;
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
import static com.tesco.pma.cycle.api.PMReviewType.OBJECTIVE;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.APPROVED;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.COMPLETED;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.DECLINED;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.DRAFT;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.WAITING_FOR_APPROVAL;
import static com.tesco.pma.cycle.api.model.PMElementType.REVIEW;
import static com.tesco.pma.cycle.api.model.PMReviewElement.PM_REVIEW_MAX;
import static com.tesco.pma.cycle.api.model.PMReviewElement.PM_REVIEW_MIN;
import static com.tesco.pma.review.exception.ErrorCodes.ALLOWED_STATUSES_NOT_FOUND;
import static com.tesco.pma.review.exception.ErrorCodes.CANNOT_DELETE_REVIEW_COUNT_CONSTRAINT;
import static com.tesco.pma.review.exception.ErrorCodes.COLLEAGUE_CYCLE_NOT_FOUND;
import static com.tesco.pma.review.exception.ErrorCodes.MAX_REVIEW_NUMBER_CONSTRAINT_VIOLATION;
import static com.tesco.pma.review.exception.ErrorCodes.MIN_REVIEW_NUMBER_CONSTRAINT_VIOLATION;
import static com.tesco.pma.review.exception.ErrorCodes.ORG_OBJECTIVES_NOT_FOUND;
import static com.tesco.pma.review.exception.ErrorCodes.ORG_OBJECTIVE_ALREADY_EXISTS;
import static com.tesco.pma.review.exception.ErrorCodes.REVIEW_ALREADY_EXISTS;
import static com.tesco.pma.review.exception.ErrorCodes.REVIEW_NOT_FOUND;
import static com.tesco.pma.review.exception.ErrorCodes.REVIEW_STATUS_NOT_ALLOWED;
import static com.tesco.pma.review.exception.ErrorCodes.TIMELINE_POINT_NOT_FOUND;

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
    private final PMColleagueCycleService pmColleagueCycleService;
    private final TimelinePointDAO timelinePointDAO;
    private final NamedMessageSourceAccessor messageSourceAccessor;
    private final PMCycleService pmCycleService;

    private static final String REVIEW_UUID_PARAMETER_NAME = "reviewUuid";
    private static final String COLLEAGUE_UUID_PARAMETER_NAME = "colleagueUuid";
    private static final String COLLEAGUE_CYCLE_UUID_PARAMETER_NAME = "colleagueCycleUuid";
    private static final String MANAGER_UUID_PARAMETER_NAME = "managerUuid";
    private static final String PERFORMANCE_CYCLE_UUID_PARAMETER_NAME = "performanceCycleUuid";
    private static final String TL_POINT_UUID_PARAMETER_NAME = "tlPointUuid";
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
        var timelinePoint = getTimelinePoint(performanceCycleUuid, colleagueUuid, type);
        var res = reviewDAO.getByParams(
                timelinePoint.getUuid(),
                type,
                null,
                number);
        if (res == null || 1 != res.size()) {
            throw notFound(REVIEW_NOT_FOUND,
                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid,
                            PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, performanceCycleUuid,
                            TYPE_PARAMETER_NAME, type,
                            NUMBER_PARAMETER_NAME, number));
        }
        return res.get(0);
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
        var timelinePoint = getTimelinePoint(performanceCycleUuid, colleagueUuid, type);

        List<Review> results = reviewDAO.getByParams(
                timelinePoint.getUuid(),
                type,
                null,
                null);

        if (results == null) {
            throw notFound(REVIEW_NOT_FOUND,
                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid,
                            PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, performanceCycleUuid,
                            TYPE_PARAMETER_NAME, type));
        }
        return results;
    }

    @Override
    public List<Review> getReviews(UUID performanceCycleUuid, UUID colleagueUuid, PMReviewType type, PMTimelinePointStatus status) {
        var timelinePoint = getTimelinePoint(performanceCycleUuid, colleagueUuid, type);

        List<Review> results = reviewDAO.getByParams(
                timelinePoint.getUuid(),
                type,
                status,
                null);

        if (results == null) {
            throw notFound(REVIEW_NOT_FOUND,
                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid,
                            PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, performanceCycleUuid,
                            TYPE_PARAMETER_NAME, type,
                            STATUS_PARAMETER_NAME, status));
        }
        return results;
    }

    @Override
    public List<Review> getReviewsByColleague(UUID performanceCycleUuid, UUID colleagueUuid) {

        List<Review> results = reviewDAO.getReviewsByColleague(performanceCycleUuid, colleagueUuid);
        if (results == null) {
            throw notFound(REVIEW_NOT_FOUND,
                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid,
                            PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, performanceCycleUuid));
        }
        return results;
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
    public Review createReview(Review review, UUID performanceCycleUuid, UUID colleagueUuid) {
        var timelinePoint = getTimelinePoint(performanceCycleUuid, colleagueUuid, review.getType());
        review.setTlPointUuid(timelinePoint.getUuid());
        return intCreateReview(review, performanceCycleUuid);
    }

    @Override
    @Transactional
    public Review updateReview(Review review, UUID performanceCycleUuid, UUID colleagueUuid) {
        var timelinePoint = getTimelinePoint(performanceCycleUuid, colleagueUuid, review.getType());
        review.setTlPointUuid(timelinePoint.getUuid());
        var reviews = reviewDAO.getByParams(
                review.getTlPointUuid(),
                review.getType(),
                null,
                review.getNumber());

        if (reviews == null || 0 == reviews.size()) {
            throw notFound(REVIEW_NOT_FOUND,
                    Map.of(TL_POINT_UUID_PARAMETER_NAME, review.getTlPointUuid(),
                            TYPE_PARAMETER_NAME, review.getType(),
                            NUMBER_PARAMETER_NAME, review.getNumber()));
        }
        var reviewBefore = reviews.get(0);
        review.setUuid(reviewBefore.getUuid());
        review.setNumber(reviewBefore.getNumber());
        return intUpdateReview(review);
    }

    @Override
    @Transactional
    public List<Review> updateReviews(UUID performanceCycleUuid,
                                      UUID colleagueUuid,
                                      PMReviewType type,
                                      List<Review> reviews) {
        var timelinePoint = getTimelinePoint(performanceCycleUuid, colleagueUuid, type);
        List<Review> results = new ArrayList<>();

        for (int idx = 0; idx < reviews.size(); idx++) {
            var review = reviews.get(idx);
            review.setTlPointUuid(timelinePoint.getUuid());
            review.setType(type);
            var rvs = reviewDAO.getByParams(
                    review.getTlPointUuid(),
                    review.getType(),
                    null,
                    idx + 1);
            if (rvs == null || 1 != rvs.size()) {
                review.setNumber(idx + 1);
                intCreateReview(review, performanceCycleUuid);
            } else {
                var reviewBefore = rvs.get(0);
                review.setUuid(reviewBefore.getUuid());
                review.setNumber(reviewBefore.getNumber());
                intUpdateReview(review);
            }
            results.add(review);
        }
        List<Review> prevReviews = reviewDAO.getByParams(
                timelinePoint.getUuid(),
                type,
                null,
                null);
        if (prevReviews.size() > reviews.size()) {
            for (int i = reviews.size() + 1; i <= prevReviews.size(); i++) {
                intDeleteReview(performanceCycleUuid, colleagueUuid, type, i);
            }
        }

        checkReviewStateAfterUpdate(performanceCycleUuid, colleagueUuid, type);

        return results;
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
        var timelinePoint = getTimelinePoint(performanceCycleUuid, colleagueUuid, type);
        reviews.forEach(review -> {
            var prevStatuses = getPrevStatusesForChangeStatus(status);
            if (0 == prevStatuses.size()) {
                throw notFound(ALLOWED_STATUSES_NOT_FOUND,
                        Map.of(OPERATION_PARAMETER_NAME, CHANGE_STATUS_OPERATION_NAME));
            }
            if (1 == reviewDAO.updateStatusByParams(
                    timelinePoint.getUuid(),
                    type,
                    review.getNumber(),
                    status,
                    prevStatuses)) {
                var actualReviews = reviewDAO.getByParams(
                        timelinePoint.getUuid(),
                        type,
                        null,
                        review.getNumber());
                reviewAuditLogDAO.logReviewUpdating(actualReviews.get(0), status, reason, loggedUserUuid);
            } else {
                throw notFound(REVIEW_NOT_FOUND,
                        Map.of(STATUS_PARAMETER_NAME, status,
                                COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid,
                                PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, performanceCycleUuid,
                                TYPE_PARAMETER_NAME, type,
                                NUMBER_PARAMETER_NAME, review.getNumber(),
                                PREV_STATUSES_PARAMETER_NAME, prevStatuses));
            }
        });

        return status;
    }

    @Override
    @Transactional
    public void deleteReview(UUID performanceCycleUuid,
                             UUID colleagueUuid,
                             PMReviewType type,
                             Integer number) {
        var timelinePoint = getTimelinePoint(performanceCycleUuid, colleagueUuid, type);
        intDeleteReview(
                performanceCycleUuid,
                colleagueUuid,
                type,
                number);
        reviewDAO.renumerateReviews(
                timelinePoint.getUuid(),
                type,
                number + 1);
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
    public List<TimelinePoint> getCycleTimelineByColleague(UUID colleagueUuid) {
        var currentCycleUuid = pmCycleService.getCurrentByColleague(colleagueUuid).getUuid();
        var cycleTimeline = timelinePointDAO.getTimeline(currentCycleUuid, colleagueUuid);

        for (TimelinePoint timelinePoint :
                cycleTimeline) {
            if (REVIEW == timelinePoint.getType()) {
                timelinePoint.setReviewType(PMReviewType.getByCode(timelinePoint.getCode()));
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
        var timelinePoint = getTimelinePoint(cycleUuid, colleagueUuid, reviewType);
        var reviewStats = reviewDAO.getReviewStats(timelinePoint.getUuid(), reviewType);
        if (null == reviewStats || 0 == reviewStats.getStatusStats().size()) {
            return null;
        }
        if (OBJECTIVE == reviewType) {
            var mapStatusStats = reviewStats.getMapStatusStats();
            var minObjectives = Integer.valueOf(timelinePoint.getProperties().getMapJson().get(PM_REVIEW_MIN));
            if (mapStatusStats.containsKey(APPROVED) && mapStatusStats.get(APPROVED) >= minObjectives) {
                return new ReviewStatusCounter(APPROVED, mapStatusStats.get(APPROVED));
            } else if (mapStatusStats.containsKey(WAITING_FOR_APPROVAL) && mapStatusStats.get(WAITING_FOR_APPROVAL) >= minObjectives) {
                return new ReviewStatusCounter(WAITING_FOR_APPROVAL, mapStatusStats.get(WAITING_FOR_APPROVAL));
            } else if (mapStatusStats.containsKey(DECLINED)) {
                return new ReviewStatusCounter(DECLINED, mapStatusStats.get(DECLINED));
            } else if (mapStatusStats.containsKey(DRAFT)) {
                return new ReviewStatusCounter(DRAFT, mapStatusStats.get(DRAFT));
            } else {
                return null;
            }
        } else {
            return reviewStats.getStatusStats().get(0);
        }
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

    public Review intCreateReview(Review review, UUID cycleUuid) {
        review.setUuid(UUID.randomUUID());
        var timelinePoint = timelinePointDAO.read(review.getTlPointUuid());
        var maxReviews = Integer.valueOf(timelinePoint.getProperties().getMapJson().get(PM_REVIEW_MAX));
        if (maxReviews < review.getNumber()) {
            throw createReviewException(
                    MAX_REVIEW_NUMBER_CONSTRAINT_VIOLATION,
                    Map.of(MAX_PARAMETER_NAME, maxReviews,
                            NUMBER_PARAMETER_NAME, review.getNumber())
            );
        }
        try {
            var allowedStatuses = getStatusesForCreate();
            if (0 == allowedStatuses.size()) {
                throw notFound(ALLOWED_STATUSES_NOT_FOUND,
                        Map.of(OPERATION_PARAMETER_NAME, CREATE_OPERATION_NAME));
            }
            if (allowedStatuses.contains(review.getStatus())) {
                reviewDAO.create(review);
                return review;
            } else {
                throw createReviewException(
                        REVIEW_STATUS_NOT_ALLOWED,
                        Map.of(STATUS_PARAMETER_NAME, review.getStatus(),
                                OPERATION_PARAMETER_NAME, CREATE_OPERATION_NAME)
                );
            }
        } catch (DuplicateKeyException e) {
            throw databaseConstraintViolation(
                    REVIEW_ALREADY_EXISTS,
                    Map.of(TL_POINT_UUID_PARAMETER_NAME, review.getTlPointUuid(),
                            TYPE_PARAMETER_NAME, review.getType(),
                            NUMBER_PARAMETER_NAME, review.getNumber()),
                    e);
        }
    }

    private Review intUpdateReview(Review review) {
        var allowedStatuses = getAllowedStatusesForUpdate(review.getType(), review.getStatus());
        if (0 == allowedStatuses.size()) {
            throw notFound(ALLOWED_STATUSES_NOT_FOUND,
                    Map.of(OPERATION_PARAMETER_NAME, UPDATE_OPERATION_NAME));
        }

        if (1 == reviewDAO.update(review, allowedStatuses)) {
            return review;
        } else {
            throw notFound(REVIEW_NOT_FOUND,
                    Map.of(OPERATION_PARAMETER_NAME, UPDATE_OPERATION_NAME,
                            TL_POINT_UUID_PARAMETER_NAME, review.getTlPointUuid(),
                            TYPE_PARAMETER_NAME, review.getType(),
                            NUMBER_PARAMETER_NAME, review.getNumber(),
                            ALLOWED_STATUSES_PARAMETER_NAME, allowedStatuses));
        }
    }

    private void intDeleteReview(UUID performanceCycleUuid,
                                 UUID colleagueUuid,
                                 PMReviewType type,
                                 Integer number) {
        var timelinePoint = getTimelinePoint(performanceCycleUuid, colleagueUuid, type);
        var minReviews = Integer.valueOf(timelinePoint.getProperties().getMapJson().get(PM_REVIEW_MIN));
        var reviewCount = reviewDAO.getReviewStats(timelinePoint.getUuid(), type)
                .getStatusStats()
                .stream()
                .mapToInt(ReviewStatusCounter::getCount)
                .sum();
        if (reviewCount == minReviews) {
            throw deleteReviewException(CANNOT_DELETE_REVIEW_COUNT_CONSTRAINT,
                    Map.of(MIN_PARAMETER_NAME, minReviews));
        }

        var allowedStatuses = getStatusesForDelete(type);
        if (0 == allowedStatuses.size()) {
            throw notFound(ALLOWED_STATUSES_NOT_FOUND,
                    Map.of(OPERATION_PARAMETER_NAME, DELETE_OPERATION_NAME));
        }
        if (1 != reviewDAO.deleteByParams(
                timelinePoint.getUuid(),
                type,
                null,
                number,
                allowedStatuses)) {
            throw notFound(REVIEW_NOT_FOUND,
                    Map.of(OPERATION_PARAMETER_NAME, DELETE_OPERATION_NAME,
                            COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid,
                            PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, performanceCycleUuid,
                            TYPE_PARAMETER_NAME, type,
                            NUMBER_PARAMETER_NAME, number,
                            ALLOWED_STATUSES_PARAMETER_NAME, allowedStatuses));
        }
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
        if (OBJECTIVE == type) {
            var timelinePoint = getTimelinePoint(performanceCycleUuid, colleagueUuid, type);
            var minReviews = Integer.valueOf(timelinePoint.getProperties().getMapJson().get(PM_REVIEW_MIN));
            var mapStatusStats = reviewDAO.getReviewStats(timelinePoint.getUuid(), type).getMapStatusStats();

            if (mapStatusStats.containsKey(WAITING_FOR_APPROVAL)
                    && mapStatusStats.get(WAITING_FOR_APPROVAL) < minReviews) {
                throw updateReviewException(MIN_REVIEW_NUMBER_CONSTRAINT_VIOLATION,
                        Map.of(COUNT_PARAMETER_NAME, mapStatusStats.get(WAITING_FOR_APPROVAL),
                                STATUS_PARAMETER_NAME, WAITING_FOR_APPROVAL,
                                MIN_PARAMETER_NAME, minReviews));
            }
        }
    }

    TimelinePoint getTimelinePoint(UUID performanceCycleUuid,
                                   UUID colleagueUuid,
                                   PMReviewType type) {
        final var colleagueCycles = pmColleagueCycleService.getByCycleUuid(
                performanceCycleUuid,
                colleagueUuid,
                null);
        if (colleagueCycles == null || 1 != colleagueCycles.size()) {
            throw notFound(COLLEAGUE_CYCLE_NOT_FOUND,
                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid,
                            PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, performanceCycleUuid));
        }
        final var colleagueCycle = colleagueCycles.get(0);

        final var timelinePoints = timelinePointDAO.getByParams(
                colleagueCycle.getUuid(),
                type.getCode(),
                null);
        if (timelinePoints == null || 1 != timelinePoints.size()) {
            throw notFound(TIMELINE_POINT_NOT_FOUND,
                    Map.of(COLLEAGUE_CYCLE_UUID_PARAMETER_NAME, colleagueCycle.getUuid(),
                            TYPE_PARAMETER_NAME, type.getCode()));
        }

        return timelinePoints.get(0);
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