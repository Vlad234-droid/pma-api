package com.tesco.pma.review.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.error.ErrorCodeAware;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.review.dao.ReviewAuditLogDAO;
import com.tesco.pma.review.dao.ReviewDAO;
import com.tesco.pma.review.domain.GroupObjective;
import com.tesco.pma.review.domain.Review;
import com.tesco.pma.review.domain.ReviewStatus;
import com.tesco.pma.review.domain.ReviewType;
import com.tesco.pma.review.domain.WorkingGroupObjective;
import lombok.RequiredArgsConstructor;
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
import java.util.stream.Collectors;

import static com.tesco.pma.review.exception.ErrorCodes.ALLOWED_STATUSES_NOT_FOUND;
import static com.tesco.pma.review.exception.ErrorCodes.BUSINESS_UNIT_NOT_EXISTS;
import static com.tesco.pma.review.exception.ErrorCodes.GROUP_OBJECTIVES_NOT_FOUND;
import static com.tesco.pma.review.exception.ErrorCodes.GROUP_OBJECTIVE_ALREADY_EXISTS;
import static com.tesco.pma.review.exception.ErrorCodes.REVIEWS_NOT_FOUND;
import static com.tesco.pma.review.exception.ErrorCodes.REVIEWS_NOT_FOUND_FOR_STATUS_UPDATE;
import static com.tesco.pma.review.exception.ErrorCodes.REVIEW_ALREADY_EXISTS;
import static com.tesco.pma.review.exception.ErrorCodes.REVIEW_NOT_FOUND_FOR_COLLEAGUE;
import static com.tesco.pma.review.exception.ErrorCodes.REVIEW_NOT_FOUND_FOR_DELETE;
import static com.tesco.pma.review.exception.ErrorCodes.REVIEW_NOT_FOUND_FOR_UPDATE;
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
    private static final String ALLOWED_STATUSES_PARAMETER_NAME = "allowedStatuses";
    private static final String OPERATION_PARAMETER_NAME = "operation";
    private static final String DELETE_OPERATION_NAME = "DELETE";
    private static final String UPDATE_OPERATION_NAME = "UPDATE";
    private static final String CHANGE_STATUS_OPERATION_NAME = "CHANGE STATUS";
    private static final Comparator<GroupObjective> GROUP_OBJECTIVE_SEQUENCE_NUMBER_TITLE_COMPARATOR =
            Comparator.comparing(GroupObjective::getNumber)
                    .thenComparing(GroupObjective::getTitle);

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
        review.setUuid(UUID.randomUUID());
        try {
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
        }
        review.setUuid(reviewBefore.getUuid());
        review.setNumber(reviewBefore.getNumber());
        return intUpdateReview(review);
    }

    @Override
    @Transactional
    public List<Review> updateReviews(UUID performanceCycleUuid,
                                      UUID colleagueUuid,
                                      ReviewType type,
                                      List<Review> reviews) {
        List<Review> results = new ArrayList<>();
        for (int idx = 0; idx < reviews.size(); idx++) {
            var review = reviews.get(idx);
            review.setPerformanceCycleUuid(performanceCycleUuid);
            review.setColleagueUuid(colleagueUuid);
            review.setType(type);
            var reviewBefore = reviewDAO.getReview(
                    review.getPerformanceCycleUuid(),
                    review.getColleagueUuid(),
                    review.getType(),
                    idx + 1);
            if (null == reviewBefore) {
                review.setUuid(UUID.randomUUID());
                review.setNumber(idx + 1);
                try {
                    reviewDAO.createReview(review);
                } catch (DuplicateKeyException e) {
                    throw databaseConstraintViolation(
                            REVIEW_ALREADY_EXISTS,
                            Map.of(COLLEAGUE_UUID_PARAMETER_NAME, review.getColleagueUuid(),
                                    PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, review.getPerformanceCycleUuid(),
                                    TYPE_PARAMETER_NAME, review.getType(),
                                    NUMBER_PARAMETER_NAME, review.getNumber()),
                            e);

                }
            } else {
                review.setUuid(reviewBefore.getUuid());
                review.setNumber(reviewBefore.getNumber());
                intUpdateReview(review);
            }
            results.add(review);
        }
        List<Review> prevReviews = reviewDAO.getReviews(performanceCycleUuid, colleagueUuid, type);
        if (prevReviews.size() > reviews.size()) {
            for (int i = reviews.size() + 1; i <= prevReviews.size(); i++) {
                deleteReviewWithoutRenumeration(performanceCycleUuid, colleagueUuid, type, i);
            }
        }
        return results;
    }

    @Override
    @Transactional
    public ReviewStatus updateReviewsStatus(UUID performanceCycleUuid,
                                            UUID colleagueUuid,
                                            ReviewType type,
                                            List<Review> reviews,
                                            ReviewStatus status,
                                            String reason,
                                            String loggedUserName) {
        reviews.forEach(review -> {
            var prevStatuses = type.getPrevStatusesForChangeStatus(status);
            if (0 == prevStatuses.size()) {
                throw notFound(ALLOWED_STATUSES_NOT_FOUND,
                        Map.of(OPERATION_PARAMETER_NAME, CHANGE_STATUS_OPERATION_NAME));
            }
            if (1 == reviewDAO.updateReviewStatus(
                    performanceCycleUuid,
                    colleagueUuid,
                    type,
                    review.getNumber(),
                    status,
                    prevStatuses)) {
                var actualReview = reviewDAO.getReview(
                        performanceCycleUuid,
                        colleagueUuid,
                        type,
                        review.getNumber());
                reviewAuditLogDAO.logReviewUpdating(actualReview, status, reason, loggedUserName);
            } else {
                throw notFound(REVIEWS_NOT_FOUND_FOR_STATUS_UPDATE,
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
                             ReviewType type,
                             Integer number) {
        deleteReviewWithoutRenumeration(
                performanceCycleUuid,
                colleagueUuid,
                type,
                number);
        reviewDAO.renumerateReviews(
                performanceCycleUuid,
                colleagueUuid,
                type,
                number + 1);
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

    private List<ReviewStatus> getAllowedStatusesForUpdate(ReviewType reviewType, ReviewStatus newStatus) {
        var allowedStatusesForUpdate = reviewType.getStatusesForUpdate();
        var prevStatusesForChangeStatus = reviewType.getPrevStatusesForChangeStatus(newStatus);
        return allowedStatusesForUpdate.stream()
                .filter(prevStatusesForChangeStatus::contains)
                .collect(Collectors.toList());
    }

    private void deleteReviewWithoutRenumeration(UUID performanceCycleUuid,
                                                 UUID colleagueUuid,
                                                 ReviewType type,
                                                 Integer number) {
        var allowedStatuses = type.getStatusesForDelete();
        if (0 == allowedStatuses.size()) {
            throw notFound(ALLOWED_STATUSES_NOT_FOUND,
                    Map.of(OPERATION_PARAMETER_NAME, DELETE_OPERATION_NAME));
        }
        if (1 != reviewDAO.deleteReview(
                performanceCycleUuid,
                colleagueUuid,
                type,
                number,
                allowedStatuses)) {
            throw notFound(REVIEW_NOT_FOUND_FOR_DELETE,
                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid,
                            PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, performanceCycleUuid,
                            TYPE_PARAMETER_NAME, type,
                            NUMBER_PARAMETER_NAME, number,
                            ALLOWED_STATUSES_PARAMETER_NAME, allowedStatuses));
        }
    }

    private Review intUpdateReview(Review review) {
        var allowedStatuses = getAllowedStatusesForUpdate(review.getType(), review.getStatus());
        if (0 == allowedStatuses.size()) {
            throw notFound(ALLOWED_STATUSES_NOT_FOUND,
                    Map.of(OPERATION_PARAMETER_NAME, UPDATE_OPERATION_NAME));
        }

        if (1 == reviewDAO.updateReview(review, allowedStatuses)) {
            return review;
        } else {
            throw notFound(REVIEW_NOT_FOUND_FOR_UPDATE,
                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, review.getColleagueUuid(),
                            PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, review.getPerformanceCycleUuid(),
                            TYPE_PARAMETER_NAME, review.getType(),
                            NUMBER_PARAMETER_NAME, review.getNumber(),
                            ALLOWED_STATUSES_PARAMETER_NAME, allowedStatuses));
        }
    }
}