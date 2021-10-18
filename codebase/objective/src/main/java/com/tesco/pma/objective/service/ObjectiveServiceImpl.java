package com.tesco.pma.objective.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.error.ErrorCodeAware;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.objective.dao.ObjectiveDAO;
import com.tesco.pma.objective.dao.ReviewAuditLogDAO;
import com.tesco.pma.objective.domain.GroupObjective;
import com.tesco.pma.objective.domain.ObjectiveStatus;
import com.tesco.pma.objective.domain.PersonalObjective;
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

import static com.tesco.pma.objective.domain.ObjectiveStatus.APPROVED;
import static com.tesco.pma.objective.domain.ObjectiveStatus.COMPLETED;
import static com.tesco.pma.objective.domain.ObjectiveStatus.DRAFT;
import static com.tesco.pma.objective.domain.ObjectiveStatus.RETURNED;
import static com.tesco.pma.objective.domain.ObjectiveStatus.WAITING_FOR_APPROVAL;
import static com.tesco.pma.objective.exception.ErrorCodes.BUSINESS_UNIT_NOT_EXISTS;
import static com.tesco.pma.objective.exception.ErrorCodes.GROUP_OBJECTIVES_NOT_FOUND;
import static com.tesco.pma.objective.exception.ErrorCodes.GROUP_OBJECTIVE_ALREADY_EXISTS;
import static com.tesco.pma.objective.exception.ErrorCodes.PERSONAL_OBJECTIVES_NOT_FOUND;
import static com.tesco.pma.objective.exception.ErrorCodes.PERSONAL_OBJECTIVE_ALREADY_EXISTS;
import static com.tesco.pma.objective.exception.ErrorCodes.PERSONAL_OBJECTIVE_NOT_FOUND;
import static com.tesco.pma.objective.exception.ErrorCodes.PERSONAL_OBJECTIVE_NOT_FOUND_BY_UUID;
import static com.tesco.pma.objective.exception.ErrorCodes.PERSONAL_OBJECTIVE_NOT_FOUND_FOR_COLLEAGUE;
import static java.time.Instant.now;

/**
 * Implementation of {@link ObjectiveService}.
 */
@Service
@Validated
@RequiredArgsConstructor
public class ObjectiveServiceImpl implements ObjectiveService {
    private final ObjectiveDAO objectiveDAO;
    private final ReviewAuditLogDAO reviewAuditLogDAO;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    private static final String PERSONAL_OBJECTIVE_UUID_PARAMETER_NAME = "personalObjectiveUuid";
    private static final String COLLEAGUE_UUID_PARAMETER_NAME = "colleagueUuid";
    private static final String PERFORMANCE_CYCLE_UUID_PARAMETER_NAME = "performanceCycleUuid";
    private static final String BUSINESS_UNIT_UUID_PARAMETER_NAME = "businessUnitUuid";
    private static final String NUMBER_PARAMETER_NAME = "number";
    private static final String VERSION_PARAMETER_NAME = "version";
    private static final String STATUS_PARAMETER_NAME = "status";
    private static final String PREV_STATUSES_PARAMETER_NAME = "prevStatuses";
    private static final Comparator<GroupObjective> GROUP_OBJECTIVE_SEQUENCE_NUMBER_TITLE_COMPARATOR =
            Comparator.comparing(GroupObjective::getNumber)
                    .thenComparing(GroupObjective::getTitle);
    private static final Map<ObjectiveStatus, Collection<ObjectiveStatus>> UPDATE_STATUS_RULE_MAP;

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
    public PersonalObjective getPersonalObjectiveByUuid(UUID personalObjectiveUuid) {
        var res = objectiveDAO.getPersonalObjective(personalObjectiveUuid);
        if (res == null) {
            throw notFound(PERSONAL_OBJECTIVE_NOT_FOUND_BY_UUID,
                    Map.of(PERSONAL_OBJECTIVE_UUID_PARAMETER_NAME, personalObjectiveUuid));
        }
        return res;
    }

    @Override
    public PersonalObjective getPersonalObjectiveForColleague(UUID performanceCycleUuid, UUID colleagueUuid, Integer number) {
        var res = objectiveDAO.getPersonalObjectiveForColleague(performanceCycleUuid, colleagueUuid, number);
        if (res == null) {
            throw notFound(PERSONAL_OBJECTIVE_NOT_FOUND_FOR_COLLEAGUE,
                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid,
                            PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, performanceCycleUuid,
                            NUMBER_PARAMETER_NAME, number));
        }
        return res;
    }

    @Override
    public List<PersonalObjective> getPersonalObjectivesForColleague(UUID performanceCycleUuid, UUID colleagueUuid) {
        List<PersonalObjective> results = objectiveDAO.getPersonalObjectivesForColleague(performanceCycleUuid, colleagueUuid);
        if (results == null) {
            throw notFound(PERSONAL_OBJECTIVES_NOT_FOUND,
                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid,
                            PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, performanceCycleUuid));
        }
        return results;
    }

    @Override
    @Transactional
    public PersonalObjective createPersonalObjective(PersonalObjective personalObjective) {
        try {
            personalObjective.setUuid(UUID.randomUUID());
            personalObjective.setStatus(DRAFT);
            objectiveDAO.createPersonalObjective(personalObjective);
            return personalObjective;
        } catch (DuplicateKeyException e) {
            throw databaseConstraintViolation(
                    PERSONAL_OBJECTIVE_ALREADY_EXISTS,
                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, personalObjective.getColleagueUuid(),
                            PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, personalObjective.getPerformanceCycleUuid(),
                            NUMBER_PARAMETER_NAME, personalObjective.getNumber()),
                    e);
        }
    }

    @Override
    @Transactional
    public PersonalObjective updatePersonalObjective(PersonalObjective personalObjective) {
        var personalObjectiveBefore = objectiveDAO.getPersonalObjectiveForColleague(
                personalObjective.getPerformanceCycleUuid(),
                personalObjective.getColleagueUuid(),
                personalObjective.getNumber());
        if (null == personalObjectiveBefore) {
            throw notFound(PERSONAL_OBJECTIVE_NOT_FOUND_FOR_COLLEAGUE,
                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, personalObjective.getColleagueUuid(),
                            PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, personalObjective.getPerformanceCycleUuid(),
                            NUMBER_PARAMETER_NAME, personalObjective.getNumber()));
        } else {
            personalObjective.setUuid(personalObjectiveBefore.getUuid());
            personalObjective.setStatus(personalObjectiveBefore.getStatus());
            objectiveDAO.updatePersonalObjective(personalObjective);
        }

        return personalObjective;
    }

    @Override
    @Transactional
    public ObjectiveStatus updatePersonalObjectiveStatus(UUID performanceCycleUuid,
                                                         UUID colleagueUuid,
                                                         Integer number,
                                                         ObjectiveStatus status,
                                                         String reason,
                                                         String loggedUserName) {
        var actualReview = objectiveDAO.getPersonalObjectiveForColleague(
                performanceCycleUuid,
                colleagueUuid,
                number);
        var prevStatuses = UPDATE_STATUS_RULE_MAP.get(status);
        if (1 == objectiveDAO.updatePersonalObjectiveStatus(
                performanceCycleUuid,
                colleagueUuid,
                number,
                status,
                prevStatuses)) {
            reviewAuditLogDAO.logLogReviewUpdating(actualReview, status, reason, loggedUserName);
            return status;
        } else {
            throw notFound(PERSONAL_OBJECTIVE_NOT_FOUND,
                    Map.of(STATUS_PARAMETER_NAME, status,
                            COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid,
                            PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, performanceCycleUuid,
                            NUMBER_PARAMETER_NAME, number,
                            PREV_STATUSES_PARAMETER_NAME, prevStatuses));
        }
    }

    @Override
    @Transactional
    public void deletePersonalObjective(UUID personalObjectiveUuid) {
        if (1 != objectiveDAO.deletePersonalObjective(personalObjectiveUuid)) {
            throw notFound(PERSONAL_OBJECTIVE_NOT_FOUND_BY_UUID,
                    Map.of(PERSONAL_OBJECTIVE_UUID_PARAMETER_NAME, personalObjectiveUuid));
        }
    }

    @Override
    @Transactional
    public List<GroupObjective> createGroupObjectives(UUID businessUnitUuid,
                                                      List<GroupObjective> groupObjectives) {
        var currentGroupObjectives = objectiveDAO.getGroupObjectivesByBusinessUnitUuid(businessUnitUuid);
        if (listEqualsIgnoreOrder(currentGroupObjectives, groupObjectives, GROUP_OBJECTIVE_SEQUENCE_NUMBER_TITLE_COMPARATOR)) {
            return currentGroupObjectives;
        }
        List<GroupObjective> results = new ArrayList<>();
        final var newVersion = objectiveDAO.getMaxVersionGroupObjective(businessUnitUuid) + 1;
        groupObjectives.forEach(groupObjective -> {
            try {
                groupObjective.setUuid(UUID.randomUUID());
                groupObjective.setBusinessUnitUuid(businessUnitUuid);
                groupObjective.setVersion(newVersion);
                if (1 == objectiveDAO.createGroupObjective(groupObjective)) {
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
        List<GroupObjective> results = objectiveDAO.getGroupObjectivesByBusinessUnitUuid(businessUnitUuid);
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
        final var version = objectiveDAO.getMaxVersionGroupObjective(businessUnitUuid);
        final var workingGroupObjective = getWorkingGroupObjective(businessUnitUuid, version);
        workingGroupObjective.setUpdaterId(loggedUserName);
        if (1 == objectiveDAO.insertOrUpdateWorkingGroupObjective(workingGroupObjective)) {
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
        if (1 != objectiveDAO.deleteWorkingGroupObjective(businessUnitUuid)) {
            throw notFound(BUSINESS_UNIT_NOT_EXISTS,
                    Map.of(BUSINESS_UNIT_UUID_PARAMETER_NAME, businessUnitUuid));
        }
    }

    @Override
    public List<GroupObjective> getPublishedGroupObjectives(UUID businessUnitUuid) {
        List<GroupObjective> results = objectiveDAO.getWorkingGroupObjectivesByBusinessUnitUuid(businessUnitUuid);
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
