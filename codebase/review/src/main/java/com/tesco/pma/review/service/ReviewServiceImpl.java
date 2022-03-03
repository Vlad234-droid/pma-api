package com.tesco.pma.review.service;

import com.tesco.pma.api.OrgObjectiveStatus;
import com.tesco.pma.colleague.api.workrelationships.WorkLevel;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.PMCycleStatus;
import com.tesco.pma.cycle.api.PMReviewType;
import com.tesco.pma.cycle.api.PMTimelinePointStatus;
import com.tesco.pma.cycle.service.PMColleagueCycleService;
import com.tesco.pma.error.ErrorCodeAware;
import com.tesco.pma.event.EventSupport;
import com.tesco.pma.event.service.EventSender;
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
import com.tesco.pma.review.domain.ColleagueView;
import com.tesco.pma.review.domain.OrgObjective;
import com.tesco.pma.review.domain.Review;
import com.tesco.pma.review.domain.TimelinePoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.api.ActionType.PUBLISH;
import static com.tesco.pma.api.ActionType.SAVE_AS_DRAFT;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.APPROVED;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.DECLINED;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.DRAFT;
import static com.tesco.pma.cycle.api.PMTimelinePointStatus.WAITING_FOR_APPROVAL;
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
import static java.util.stream.Collectors.toList;

/**
 * Implementation of {@link ReviewService}.
 */
@Slf4j
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
    private final EventSender eventSender;
    private final ProfileService profileService;
    private final ReviewDecisionService reviewDecisionService;
    private final TimelinePointDecisionService timelinePointDecisionService;

    private static final String REVIEW_UUID_PARAMETER_NAME = "reviewUuid";
    private static final String COLLEAGUE_UUID_PARAMETER_NAME = "colleagueUuid";
    private static final String COLLEAGUE_CYCLE_UUID_PARAMETER_NAME = "colleagueCycleUuid";
    private static final String MANAGER_UUID_PARAMETER_NAME = "managerUuid";
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
    private static final String NF_ORGANISATION_OBJECTIVES_EVENT_NAME = "NF_ORGANISATION_OBJECTIVES";
    private static final String NF_OBJECTIVES_APPROVED_FOR_SHARING_EVENT_NAME = "NF_OBJECTIVES_APPROVED_FOR_SHARING";
    private static final String NF_PM_REVIEW_SUBMITTED_EVENT_NAME = "NF_PM_REVIEW_SUBMITTED";
    private static final String NF_PM_REVIEW_APPROVED_EVENT_NAME = "NF_PM_REVIEW_APPROVED";
    private static final String COLLEAGUE_UUID_EVENT_PARAM = "COLLEAGUE_UUID";
    private static final String SENDER_COLLEAGUE_UUID_EVENT_PARAM = "SENDER_COLLEAGUE_UUID";
    private static final String TIMELINE_POINT_UUID_EVENT_PARAM = "TIMELINE_POINT_UUID";
    private static final String PM_CYCLE_STATUS_PARAMETER_NAME = "pmCycleStatus";

    private static final Comparator<OrgObjective> ORG_OBJECTIVE_SEQUENCE_NUMBER_TITLE_COMPARATOR =
            Comparator.comparing(OrgObjective::getNumber)
                    .thenComparing(OrgObjective::getTitle);

    private static final List<PMTimelinePointStatus> MIN_REVIEW_STATUSES = List.of(
            WAITING_FOR_APPROVAL,
            APPROVED,
            DECLINED);

    @Override
    public Review getReview(UUID colleagueUuid, PMReviewType type, Integer number) {
        var timelinePoint = getTimelinePoint(colleagueUuid, type);
        var res = reviewDAO.getByParams(
                timelinePoint.getUuid(),
                type,
                null,
                number);
        if (res == null || 1 != res.size()) {
            throw notFound(REVIEW_NOT_FOUND,
                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid,
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
    public List<Review> getReviews(UUID colleagueUuid, PMReviewType type) {
        var timelinePoint = getTimelinePoint(colleagueUuid, type);

        List<Review> results = reviewDAO.getByParams(
                timelinePoint.getUuid(),
                type,
                null,
                null);

        if (results == null) {
            throw notFound(REVIEW_NOT_FOUND,
                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid,
                            TYPE_PARAMETER_NAME, type));
        }
        return results;
    }

    @Override
    public List<Review> getReviews(UUID colleagueUuid, PMReviewType type, PMTimelinePointStatus status) {
        var timelinePoint = getTimelinePoint(colleagueUuid, type);

        List<Review> results = reviewDAO.getByParams(
                timelinePoint.getUuid(),
                type,
                status,
                null);

        if (results == null) {
            throw notFound(REVIEW_NOT_FOUND,
                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid,
                            TYPE_PARAMETER_NAME, type,
                            STATUS_PARAMETER_NAME, status));
        }
        return results;
    }

    @Override
    public List<Review> getReviewsByColleague(UUID colleagueUuid) {

        List<Review> results = reviewDAO.getReviewsByColleague(colleagueUuid, PMCycleStatus.STARTED);
        if (results == null) {
            throw notFound(REVIEW_NOT_FOUND,
                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid));
        }
        return results;
    }

    @Override
    public List<ColleagueView> getTeamView(UUID managerUuid, Integer depth) {
        List<ColleagueView> results = reviewDAO.getTeamView(managerUuid, depth);
        if (results == null) {
            throw notFound(REVIEW_NOT_FOUND,
                    Map.of(MANAGER_UUID_PARAMETER_NAME, managerUuid));
        } else {
            results.forEach(colleagueReviews ->
                    colleagueReviews.setTimeline(getCycleTimelineByColleague(colleagueReviews.getUuid())));
        }
        return results;
    }

    @Override
    @Transactional
    public Review createReview(Review review, UUID colleagueUuid) {
        var timelinePoint = getTimelinePoint(colleagueUuid, review.getType());
        review.setTlPointUuid(timelinePoint.getUuid());
        var createdReview = createReview(review, timelinePoint);

        checkReviewStateAfterUpdate(timelinePoint);
        updateTLPointStatus(timelinePoint);
        return createdReview;
    }

    private Review createReview(Review review, TimelinePoint timelinePoint) {
        review.setUuid(UUID.randomUUID());
        var maxReviews = getVariable(timelinePoint, PM_REVIEW_MAX, 1);
        if (maxReviews < review.getNumber()) {
            throw createReviewException(
                    MAX_REVIEW_NUMBER_CONSTRAINT_VIOLATION,
                    Map.of(MAX_PARAMETER_NAME, maxReviews,
                            NUMBER_PARAMETER_NAME, review.getNumber())
            );
        }
        try {
            var allowedStatuses = reviewDecisionService.getReviewAllowedStatuses(review.getType(), CREATE_OPERATION_NAME);
            if (allowedStatuses.isEmpty()) {
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

    @Override
    @Transactional
    public Review updateReview(Review review, UUID colleagueUuid, UUID loggedUserUuid) {
        var timelinePoint = getTimelinePoint(colleagueUuid, review.getType());
        review.setTlPointUuid(timelinePoint.getUuid());
        var reviews = reviewDAO.getByParams(
                review.getTlPointUuid(),
                review.getType(),
                null,
                review.getNumber());

        if (reviews == null || reviews.isEmpty()) {
            throw notFound(REVIEW_NOT_FOUND,
                    Map.of(TL_POINT_UUID_PARAMETER_NAME, review.getTlPointUuid(),
                            TYPE_PARAMETER_NAME, review.getType(),
                            NUMBER_PARAMETER_NAME, review.getNumber()));
        }
        var reviewBefore = reviews.get(0);
        review.setUuid(reviewBefore.getUuid());
        review.setNumber(reviewBefore.getNumber());
        updateReview(review);
        checkReviewStateAfterUpdate(timelinePoint);
        updateTLPointStatus(timelinePoint);
        sendEvent(timelinePoint, loggedUserUuid, colleagueUuid);
        return review;
    }

    private Review updateReview(Review review) {
        var allowedStatuses = getAllowedStatusesForUpdate(review.getType(), review.getStatus());
        if (allowedStatuses.isEmpty()) {
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

    @Override
    @Transactional
    public List<Review> updateReviews(UUID colleagueUuid,
                                      PMReviewType type,
                                      List<Review> reviews,
                                      UUID loggedUserUuid) {
        var timelinePoint = getTimelinePoint(colleagueUuid, type);
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
                createReview(review, timelinePoint);
            } else {
                var reviewBefore = rvs.get(0);
                review.setUuid(reviewBefore.getUuid());
                review.setNumber(reviewBefore.getNumber());
                updateReview(review);
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
                deleteReview(timelinePoint, i);
            }
        }

        checkReviewStateAfterUpdate(timelinePoint);
        updateTLPointStatus(timelinePoint);
        sendEvent(timelinePoint, loggedUserUuid, colleagueUuid);
        return results;
    }

    @Override
    @Transactional
    public PMTimelinePointStatus updateReviewsStatus(UUID colleagueUuid,
                                                     PMReviewType type,
                                                     List<Review> reviews,
                                                     PMTimelinePointStatus status,
                                                     String reason,
                                                     UUID loggedUserUuid) {
        var prevStatuses = reviewDecisionService.getReviewAllowedPrevStatuses(type, status);
        if (prevStatuses.isEmpty()) {
            throw notFound(ALLOWED_STATUSES_NOT_FOUND,
                    Map.of(OPERATION_PARAMETER_NAME, CHANGE_STATUS_OPERATION_NAME));
        }
        var timelinePoint = getTimelinePoint(colleagueUuid, type);
        reviews.forEach(review -> {
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
                var actualReview = actualReviews.get(0);
                //feedback presents in MYR
                if (review.getProperties() != null
                        && !review.getProperties().getMapJson().isEmpty()) {
                    actualReview.setProperties(review.getProperties());
                    updateReview(actualReview);
                }
                reviewAuditLogDAO.logReviewUpdating(actualReview, status, reason, loggedUserUuid);
            } else {
                throw notFound(REVIEW_NOT_FOUND,
                        Map.of(STATUS_PARAMETER_NAME, status,
                                COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid,
                                TYPE_PARAMETER_NAME, type,
                                NUMBER_PARAMETER_NAME, review.getNumber(),
                                PREV_STATUSES_PARAMETER_NAME, prevStatuses));
            }
        });

        checkReviewStateAfterUpdate(timelinePoint);
        updateTLPointStatus(timelinePoint);
        sendEvent(timelinePoint, loggedUserUuid, colleagueUuid);
        return status;
    }

    @Override
    @Transactional
    public void deleteReview(UUID colleagueUuid,
                             PMReviewType type,
                             Integer number) {
        var timelinePoint = getTimelinePoint(colleagueUuid, type);
        deleteReview(timelinePoint, number);
        reviewDAO.renumerateReviews(
                timelinePoint.getUuid(),
                type,
                number + 1);
        checkReviewStateAfterUpdate(timelinePoint);
        updateTLPointStatus(timelinePoint);
    }

    private void deleteReview(TimelinePoint timelinePoint, Integer number) {
        var minReviews = getVariable(timelinePoint, PM_REVIEW_MIN, 1);
        var reviewCount = reviewDAO.getReviewStats(timelinePoint.getUuid()).getCountAll();
        if (minReviews.equals(reviewCount)) {
            throw deleteReviewException(CANNOT_DELETE_REVIEW_COUNT_CONSTRAINT,
                    Map.of(MIN_PARAMETER_NAME, minReviews));
        }

        var allowedStatuses = reviewDecisionService.getReviewAllowedStatuses(timelinePoint.getReviewType(), DELETE_OPERATION_NAME);
        if (allowedStatuses.isEmpty()) {
            throw notFound(ALLOWED_STATUSES_NOT_FOUND,
                    Map.of(OPERATION_PARAMETER_NAME, DELETE_OPERATION_NAME));
        }
        if (1 != reviewDAO.deleteByParams(
                timelinePoint.getUuid(),
                null,
                null,
                number,
                allowedStatuses)) {
            throw notFound(REVIEW_NOT_FOUND,
                    Map.of(OPERATION_PARAMETER_NAME, DELETE_OPERATION_NAME,
                            TL_POINT_UUID_PARAMETER_NAME, timelinePoint.getUuid(),
                            NUMBER_PARAMETER_NAME, number,
                            ALLOWED_STATUSES_PARAMETER_NAME, allowedStatuses));
        }
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

    @Override
    public List<TimelinePoint> getCycleTimelineByColleague(UUID colleagueUuid) {
        return timelinePointDAO.getTimeline(colleagueUuid, PMCycleStatus.STARTED);
    }

    @Override
    public List<AuditOrgObjectiveReport> getAuditOrgObjectiveReport(RequestQuery requestQuery) {
        return reviewAuditLogDAO.getAuditOrgObjectiveReport(requestQuery);
    }

    private TimelinePoint calcTlPoint(TimelinePoint timelinePoint) {
        var reviewStats = reviewDAO.getReviewStats(timelinePoint.getUuid());
        if (null == reviewStats || reviewStats.getStatusStats().isEmpty()) {
            return timelinePoint;
        }
        var mapStatusStats = reviewStats.getMapStatusStats();
        if (mapStatusStats.containsKey(DRAFT) && mapStatusStats.get(DRAFT).equals(reviewStats.getCountAll())) {
            timelinePoint.setStatus(DRAFT);
            timelinePoint.setCount(mapStatusStats.get(DRAFT));
        } else {
            var status = reviewStats.getLastUpdatedStatus(DRAFT);
            timelinePoint.setStatus(status);
            timelinePoint.setCount(mapStatusStats.get(status));
        }
        return timelinePoint;
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
        var allowedStatusesForUpdate = reviewDecisionService.getReviewAllowedStatuses(reviewType, UPDATE_OPERATION_NAME);
        var prevStatusesForChangeStatus = reviewDecisionService.getReviewAllowedPrevStatuses(reviewType, newStatus);
        return allowedStatusesForUpdate.stream()
                .filter(prevStatusesForChangeStatus::contains)
                .collect(toList());
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
        sendEvent(NF_ORGANISATION_OBJECTIVES_EVENT_NAME, WorkLevel.WL4, WorkLevel.WL5);
        return getPublishedOrgObjectives();
    }

    private TimelinePoint updateTLPointStatus(TimelinePoint timelinePoint) {
        var calcTlPoint = calcTlPoint(timelinePoint);
        if (1 == timelinePointDAO.update(
                calcTlPoint,
                timelinePointDecisionService.getTlPointAllowedPrevStatuses(calcTlPoint.getStatus()))) {
            return calcTlPoint;
        } else {
            return null;
        }
    }

    private void checkReviewStateAfterUpdate(TimelinePoint timelinePoint) {
        var minReviews = getVariable(timelinePoint, PM_REVIEW_MIN, 1);
        var countReviewWithMinStatus = reviewDAO.getReviewStats(timelinePoint.getUuid())
                .getCount(MIN_REVIEW_STATUSES);

        if (0 < countReviewWithMinStatus && countReviewWithMinStatus < minReviews) {
            throw updateReviewException(MIN_REVIEW_NUMBER_CONSTRAINT_VIOLATION,
                    Map.of(COUNT_PARAMETER_NAME, countReviewWithMinStatus,
                            STATUS_PARAMETER_NAME, MIN_REVIEW_STATUSES,
                            MIN_PARAMETER_NAME, minReviews));
        }
    }

    TimelinePoint getTimelinePoint(UUID colleagueUuid,
                                   PMReviewType type) {
        final var colleagueCycles = pmColleagueCycleService.getByCycleUuid(
                null,
                colleagueUuid,
                PMCycleStatus.STARTED);
        if (colleagueCycles == null || 1 != colleagueCycles.size()) {
            throw notFound(COLLEAGUE_CYCLE_NOT_FOUND,
                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid,
                            PM_CYCLE_STATUS_PARAMETER_NAME, PMCycleStatus.STARTED));
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

    private DatabaseConstraintViolationException databaseConstraintViolation(ErrorCodeAware errorCode,
                                                                             Map<String, ?> params,
                                                                             Throwable cause) {
        return new DatabaseConstraintViolationException(errorCode.getCode(),
                messageSourceAccessor.getMessage(errorCode.getCode(), params), null, cause);
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

    private Integer getVariable(TimelinePoint timelinePoint, String variableName, Integer defaultValue) {
        var variable = timelinePoint.getProperties().getMapJson().get(variableName);
        if (variable != null) {
            try {
                return Integer.valueOf(variable);
            } catch (NumberFormatException e) {
                log.trace("The variable {} is not a number: {}", variableName, variable, e);
            }
        }
        return defaultValue;
    }

    private void sendEvent(String eventName, WorkLevel... wls) {
        for (WorkLevel wl : wls) {
            var rq = new RequestQuery();
            rq.addFilters("work-level_eq", wl.name());

            profileService.getSuggestions(rq)
                    .forEach(colleague -> sendEvent(eventName, colleague.getColleague().getColleagueUUID()));
        }
    }

    private void sendEvent(String eventName, UUID colleagueUuid) {
        var event = EventSupport.create(eventName,
                Map.of(COLLEAGUE_UUID_EVENT_PARAM, colleagueUuid));

        eventSender.sendEvent(event, null, true);
    }

    private void sendEvent(TimelinePoint timelinePoint, UUID loggedUserUUID, UUID colleagueUuid) {
        var eventName = timelinePointDecisionService.getStatusUpdateEventName(timelinePoint.getStatus());

        if (eventName == null) {
            return;
        }

        var recipientUUID = colleagueUuid;

        if (NF_PM_REVIEW_SUBMITTED_EVENT_NAME.equals(eventName)) {
            recipientUUID = getManagerUuid(colleagueUuid);
        }

        if (recipientUUID == null) {
            return;
        }

        var event = EventSupport.create(eventName, Map.of(
                COLLEAGUE_UUID_EVENT_PARAM, recipientUUID,
                SENDER_COLLEAGUE_UUID_EVENT_PARAM, loggedUserUUID,
                TIMELINE_POINT_UUID_EVENT_PARAM, timelinePoint.getUuid()));

        eventSender.sendEvent(event, null, true);

        if (!(NF_PM_REVIEW_APPROVED_EVENT_NAME.equals(eventName)
                && PMReviewType.OBJECTIVE.name().equals(timelinePoint.getCode()))) {
            return;
        }

        var colleague = profileService.findColleagueByColleagueUuid(colleagueUuid);

        if (Boolean.FALSE.equals(colleague.getWorkRelationships().get(0).getIsManager())) {
            return;
        }

        event = EventSupport.create(NF_OBJECTIVES_APPROVED_FOR_SHARING_EVENT_NAME, Map.of(
                COLLEAGUE_UUID_EVENT_PARAM, recipientUUID,
                SENDER_COLLEAGUE_UUID_EVENT_PARAM, loggedUserUUID,
                TIMELINE_POINT_UUID_EVENT_PARAM, timelinePoint.getUuid()));

        eventSender.sendEvent(event, null, true);
    }

    private UUID getManagerUuid(UUID colleagueUuid) {
        var colleague = profileService.findColleagueByColleagueUuid(colleagueUuid);
        var manager = colleague.getWorkRelationships().get(0).getManager();

        if (manager == null) {
            log.info("User {} has no manager", colleagueUuid);
            return null;
        }

        return manager.getColleagueUUID();
    }
}