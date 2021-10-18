package com.tesco.pma.objective.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.error.ErrorCodeAware;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.objective.dao.ReviewDAO;
import com.tesco.pma.objective.dao.ReviewAuditLogDAO;
import com.tesco.pma.objective.domain.GroupObjective;
import com.tesco.pma.objective.domain.Review;
import com.tesco.pma.objective.domain.ReviewStatus;
import com.tesco.pma.objective.domain.ReviewType;
import com.tesco.pma.objective.domain.WorkingGroupObjective;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.objective.domain.ReviewStatus.APPROVED;
import static com.tesco.pma.objective.domain.ReviewStatus.COMPLETED;
import static com.tesco.pma.objective.domain.ReviewStatus.DRAFT;
import static com.tesco.pma.objective.domain.ReviewStatus.RETURNED;
import static com.tesco.pma.objective.domain.ReviewStatus.WAITING_FOR_APPROVAL;
import static com.tesco.pma.objective.exception.ErrorCodes.BUSINESS_UNIT_NOT_EXISTS;
import static com.tesco.pma.objective.exception.ErrorCodes.GROUP_OBJECTIVES_NOT_FOUND;
import static com.tesco.pma.objective.exception.ErrorCodes.GROUP_OBJECTIVE_ALREADY_EXISTS;
import static com.tesco.pma.objective.exception.ErrorCodes.REVIEWS_NOT_FOUND;
import static com.tesco.pma.objective.exception.ErrorCodes.REVIEW_ALREADY_EXISTS;
import static com.tesco.pma.objective.exception.ErrorCodes.REVIEWS_NOT_FOUND_FOR_STATUS_UPDATE;
import static com.tesco.pma.objective.exception.ErrorCodes.REVIEW_NOT_FOUND_BY_UUID;
import static com.tesco.pma.objective.exception.ErrorCodes.REVIEW_NOT_FOUND_FOR_COLLEAGUE;
import static java.time.Instant.now;

/**
 * Implementation of {@link ReviewService}.
 */
@Service
@Validated
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewDAO reviewDAO;
    private final ReviewAuditLogDAO reviewAuditLogDAO;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    private static final String REVIEW_UUID_PARAMETER_NAME = "reviewUuid";
    private static final String COLLEAGUE_UUID_PARAMETER_NAME = "colleagueUuid";
    private static final String PERFORMANCE_CYCLE_UUID_PARAMETER_NAME = "performanceCycleUuid";
    private static final String TYPE_PARAMETER_NAME = "type";
    private static final String BUSINESS_UNIT_UUID_PARAMETER_NAME = "businessUnitUuid";
    private static final String NUMBER_PARAMETER_NAME = "number";
    private static final String VERSION_PARAMETER_NAME = "version";
    private static final String STATUS_PARAMETER_NAME = "status";
    private static final String PREV_STATUSES_PARAMETER_NAME = "prevStatuses";
    private static final Comparator<GroupObjective> GROUP_OBJECTIVE_SEQUENCE_NUMBER_TITLE_COMPARATOR =
            Comparator.comparing(GroupObjective::getNumber)
                    .thenComparing(GroupObjective::getTitle);
    private static final Map<ReviewStatus, Collection<ReviewStatus>> UPDATE_STATUS_RULE_MAP;

    static {
        UPDATE_STATUS_RULE_MAP = Map.of(
                WAITING_FOR_APPROVAL, List.of(DRAFT),
                APPROVED, List.of(WAITING_FOR_APPROVAL),
                RETURNED, List.of(WAITING_FOR_APPROVAL, APPROVED),
                COMPLETED, List.of(APPROVED),
                DRAFT, List.of(RETURNED)
        );
    }

    @Override
    public Review getReviewByUuid(UUID reviewUuid) {
        var res = reviewDAO.getReviewByUuid(reviewUuid);
        if (res == null) {
            throw notFound(REVIEW_NOT_FOUND_BY_UUID,
                    Map.of(REVIEW_UUID_PARAMETER_NAME, reviewUuid));
        }
        return res;
    }

    @Override
    public Review getReview(UUID performanceCycleUuid, UUID colleagueUuid, ReviewType type, Integer number) {
        var res = reviewDAO.getReview(performanceCycleUuid, colleagueUuid, type, number);
        if (res == null) {
            throw notFound(REVIEW_NOT_FOUND_FOR_COLLEAGUE,
                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid,
                            PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, performanceCycleUuid,
                            TYPE_PARAMETER_NAME, type,
                            NUMBER_PARAMETER_NAME, number));
        }
        return res;
    }

    @Override
    public List<Review> getReviews(UUID performanceCycleUuid, UUID colleagueUuid, ReviewType type) {
        List<Review> results = reviewDAO.getReviews(performanceCycleUuid, colleagueUuid, type);
        if (results == null) {
            throw notFound(REVIEWS_NOT_FOUND,
                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid,
                            PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, performanceCycleUuid,
                            TYPE_PARAMETER_NAME, type));
        }
        return results;
    }

    @Override
    @Transactional
    public Review createReview(Review review) {
        try {
            review.setUuid(UUID.randomUUID());
            review.setStatus(DRAFT);
            reviewDAO.createReview(review);
            return review;
        } catch (DuplicateKeyException e) {
            throw databaseConstraintViolation(
                    REVIEW_ALREADY_EXISTS,
                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, review.getColleagueUuid(),
                            PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, review.getPerformanceCycleUuid(),
                            TYPE_PARAMETER_NAME, review.getType(),
                            NUMBER_PARAMETER_NAME, review.getNumber()),
                    e);
        }
    }

    @Override
    @Transactional
    public Review updateReview(Review review) {
        var reviewBefore = reviewDAO.getReview(
                review.getPerformanceCycleUuid(),
                review.getColleagueUuid(),
                review.getType(),
                review.getNumber());
        if (null == reviewBefore) {
            throw notFound(REVIEW_NOT_FOUND_FOR_COLLEAGUE,
                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, review.getColleagueUuid(),
                            PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, review.getPerformanceCycleUuid(),
                            TYPE_PARAMETER_NAME, review.getType(),
                            NUMBER_PARAMETER_NAME, review.getNumber()));
        } else {
            review.setUuid(reviewBefore.getUuid());
            review.setStatus(reviewBefore.getStatus());
            reviewDAO.updateReview(review);
        }

        return review;
    }

    @Override
    @Transactional
    public ReviewStatus updateReviewStatus(UUID performanceCycleUuid,
                                           UUID colleagueUuid,
                                           ReviewType type,
                                           Integer number,
                                           ReviewStatus status,
                                           String reason,
                                           String loggedUserName) {
        var actualReview = reviewDAO.getReview(
                performanceCycleUuid,
                colleagueUuid,
                type,
                number);
        var prevStatuses = UPDATE_STATUS_RULE_MAP.get(status);
        if (1 == reviewDAO.updateReviewStatus(
                performanceCycleUuid,
                colleagueUuid,
                type,
                number,
                status,
                prevStatuses)) {
            reviewAuditLogDAO.logLogReviewUpdating(actualReview, status, reason, loggedUserName);
            return status;
        } else {
            throw notFound(REVIEWS_NOT_FOUND_FOR_STATUS_UPDATE,
                    Map.of(STATUS_PARAMETER_NAME, status,
                            COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid,
                            PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, performanceCycleUuid,
                            TYPE_PARAMETER_NAME, type,
                            NUMBER_PARAMETER_NAME, number,
                            PREV_STATUSES_PARAMETER_NAME, prevStatuses));
        }
    }

    @Override
    @Transactional
    public void deleteReview(UUID reviewUuid) {
        if (1 != reviewDAO.deleteReview(reviewUuid)) {
            throw notFound(REVIEW_NOT_FOUND_BY_UUID,
                    Map.of(REVIEW_UUID_PARAMETER_NAME, reviewUuid));
        }
    }

    @Override
    @Transactional
    public List<GroupObjective> createGroupObjectives(UUID businessUnitUuid,
                                                      List<GroupObjective> groupObjectives) {
        var currentGroupObjectives = reviewDAO.getGroupObjectivesByBusinessUnitUuid(businessUnitUuid);
        if (listEqualsIgnoreOrder(currentGroupObjectives, groupObjectives, GROUP_OBJECTIVE_SEQUENCE_NUMBER_TITLE_COMPARATOR)) {
            return currentGroupObjectives;
        }
        List<GroupObjective> results = new ArrayList<>();
        final var newVersion = reviewDAO.getMaxVersionGroupObjective(businessUnitUuid) + 1;
        groupObjectives.forEach(groupObjective -> {
            try {
                groupObjective.setUuid(UUID.randomUUID());
                groupObjective.setBusinessUnitUuid(businessUnitUuid);
                groupObjective.setVersion(newVersion);
                if (1 == reviewDAO.createGroupObjective(groupObjective)) {
                    results.add(groupObjective);
                } else {
                    //todo it will work after implementation foreign keys
                    throw notFound(BUSINESS_UNIT_NOT_EXISTS,
                            Map.of(BUSINESS_UNIT_UUID_PARAMETER_NAME, groupObjective.getBusinessUnitUuid()));
                }
            } catch (DuplicateKeyException e) {
                throw databaseConstraintViolation(
                        GROUP_OBJECTIVE_ALREADY_EXISTS,
                        Map.of(BUSINESS_UNIT_UUID_PARAMETER_NAME, groupObjective.getBusinessUnitUuid(),
                                NUMBER_PARAMETER_NAME, groupObjective.getNumber(),
                                VERSION_PARAMETER_NAME, groupObjective.getVersion()),
                        e);

            }

        });
        return results;
    }

    @Override
    public List<GroupObjective> getAllGroupObjectives(UUID businessUnitUuid) {
        List<GroupObjective> results = reviewDAO.getGroupObjectivesByBusinessUnitUuid(businessUnitUuid);
        if (results == null) {
            throw notFound(GROUP_OBJECTIVES_NOT_FOUND,
                    Map.of(BUSINESS_UNIT_UUID_PARAMETER_NAME, businessUnitUuid));
        }
        return results;
    }

    @Override
    @Transactional
    public WorkingGroupObjective publishGroupObjectives(UUID businessUnitUuid,
                                                        String loggedUserName) {
        final var version = reviewDAO.getMaxVersionGroupObjective(businessUnitUuid);
        final var workingGroupObjective = getWorkingGroupObjective(businessUnitUuid, version);
        workingGroupObjective.setUpdaterId(loggedUserName);
        if (1 == reviewDAO.insertOrUpdateWorkingGroupObjective(workingGroupObjective)) {
            return workingGroupObjective;
        } else {
            //todo it will work after implementation foreign keys
            throw notFound(BUSINESS_UNIT_NOT_EXISTS,
                    Map.of(BUSINESS_UNIT_UUID_PARAMETER_NAME, businessUnitUuid));
        }
    }

    @Override
    @Transactional
    public void unpublishGroupObjectives(UUID businessUnitUuid) {
        if (1 != reviewDAO.deleteWorkingGroupObjective(businessUnitUuid)) {
            throw notFound(BUSINESS_UNIT_NOT_EXISTS,
                    Map.of(BUSINESS_UNIT_UUID_PARAMETER_NAME, businessUnitUuid));
        }
    }

    @Override
    public List<GroupObjective> getPublishedGroupObjectives(UUID businessUnitUuid) {
        List<GroupObjective> results = reviewDAO.getWorkingGroupObjectivesByBusinessUnitUuid(businessUnitUuid);
        if (results == null) {
            throw notFound(GROUP_OBJECTIVES_NOT_FOUND,
                    Map.of(BUSINESS_UNIT_UUID_PARAMETER_NAME, businessUnitUuid));
        }
        return results;
    }

    private NotFoundException notFound(ErrorCodeAware errorCode, Map<String, ?> params) {
        return notFound(errorCode, params, null);
    }

    private NotFoundException notFound(ErrorCodeAware errorCode, Map<String, ?> params, Throwable cause) {
        return new NotFoundException(errorCode.getCode(), messageSourceAccessor.getMessage(errorCode.getCode(), params), null, cause);
    }

    private DatabaseConstraintViolationException databaseConstraintViolation(ErrorCodeAware errorCode,
                                                                             Map<String, ?> params,
                                                                             Throwable cause) {
        return new DatabaseConstraintViolationException(errorCode.getCode(),
                messageSourceAccessor.getMessage(errorCode.getCode(), params), null, cause);
    }

    private WorkingGroupObjective getWorkingGroupObjective(UUID businessUnitUuid, int version) {
        return WorkingGroupObjective.builder()
                .businessUnitUuid(businessUnitUuid)
                .version(version)
                .updateTime(now())
                .build();
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
}
