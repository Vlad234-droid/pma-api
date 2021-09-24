package com.tesco.pma.objective.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.error.ErrorCodeAware;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.objective.dao.ObjectiveDAO;
import com.tesco.pma.objective.domain.GroupObjective;
import com.tesco.pma.objective.domain.PersonalObjective;
import com.tesco.pma.objective.domain.WorkingGroupObjective;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.objective.exception.ErrorCodes.GROUP_OBJECTIVES_NOT_FOUND;
import static com.tesco.pma.objective.exception.ErrorCodes.GROUP_OBJECTIVE_ALREADY_EXISTS;
import static com.tesco.pma.objective.exception.ErrorCodes.GROUP_OBJECTIVE_FOREIGN_CONSTRAINT_VIOLATION;
import static com.tesco.pma.objective.exception.ErrorCodes.PERSONAL_OBJECTIVE_ALREADY_EXISTS;
import static com.tesco.pma.objective.exception.ErrorCodes.PERSONAL_OBJECTIVE_NOT_FOUND;
import static java.time.Instant.now;

/**
 * Implementation of {@link ObjectiveService}.
 */
@Service
@Validated
@RequiredArgsConstructor
public class ObjectiveServiceImpl implements ObjectiveService {
    private final ObjectiveDAO objectiveDAO;
    private final NamedMessageSourceAccessor messageSourceAccessor;

    private static final String PERSONAL_OBJECTIVE_UUID_PARAMETER_NAME = "personalObjectiveUuid";
    private static final String COLLEAGUE_UUID_PARAMETER_NAME = "colleagueUuid";
    private static final String PERFORMANCE_CYCLE_UUID_PARAMETER_NAME = "performanceCycleUuid";
    private static final String BUSINESS_UNIT_UUID_PARAMETER_NAME = "businessUnitUuid";
    private static final String SEQUENCE_NUMBER_PARAMETER_NAME = "sequenceNumber";
    private static final String VERSION_PARAMETER_NAME = "version";
    private static final Integer VERSION_1 = 1;

    @Override
    public PersonalObjective getPersonalObjectiveByUuid(UUID personalObjectiveUuid) {
        var res = objectiveDAO.getPersonalObjective(personalObjectiveUuid);
        if (res == null) {
            throw notFound(PERSONAL_OBJECTIVE_NOT_FOUND,
                    Map.of(PERSONAL_OBJECTIVE_UUID_PARAMETER_NAME, personalObjectiveUuid));
        }
        return res;
    }

    @Override
    @Transactional
    public PersonalObjective createPersonalObjective(PersonalObjective personalObjective) {
        try {
            personalObjective.setUuid(UUID.randomUUID());
            objectiveDAO.createPersonalObjective(personalObjective);
            return personalObjective;
        } catch (DuplicateKeyException e) {
            throw databaseConstraintViolation(
                    PERSONAL_OBJECTIVE_ALREADY_EXISTS,
                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, personalObjective.getColleagueUuid(),
                            PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, personalObjective.getPerformanceCycleUuid(),
                            SEQUENCE_NUMBER_PARAMETER_NAME, personalObjective.getSequenceNumber()),
                    e);
        }
    }

    @Override
    @Transactional
    public PersonalObjective updatePersonalObjective(PersonalObjective personalObjective) {
        if (1 != objectiveDAO.updatePersonalObjective(personalObjective)) {
            throw notFound(PERSONAL_OBJECTIVE_NOT_FOUND,
                    Map.of(PERSONAL_OBJECTIVE_UUID_PARAMETER_NAME, personalObjective.getUuid()));
        }
        return personalObjective;
    }

    @Override
    @Transactional
    public void deletePersonalObjective(UUID personalObjectiveUuid) {
        if (1 != objectiveDAO.deletePersonalObjective(personalObjectiveUuid)) {
            throw notFound(PERSONAL_OBJECTIVE_NOT_FOUND,
                    Map.of(PERSONAL_OBJECTIVE_UUID_PARAMETER_NAME, personalObjectiveUuid));
        }
    }

    @Override
    @Transactional
    public List<GroupObjective> createGroupObjectives(UUID businessUnitUuid,
                                                      UUID performanceCycleUuid,
                                                      List<GroupObjective> groupObjectives) {
        List<GroupObjective> results = new ArrayList<>();
        groupObjectives.forEach(groupObjective -> {
            try {
                groupObjective.setUuid(UUID.randomUUID());
                groupObjective.setBusinessUnitUuid(businessUnitUuid);
                groupObjective.setPerformanceCycleUuid(performanceCycleUuid);
                groupObjective.setVersion(VERSION_1);
                if (1 == objectiveDAO.createGroupObjective(groupObjective)) {
                    results.add(groupObjective);
                    objectiveDAO.createWorkingGroupObjective(getWorkingGroupObjective(groupObjective));
                } else {
                    //todo it will work after implementation foreign keys
                    throw notFound(GROUP_OBJECTIVE_FOREIGN_CONSTRAINT_VIOLATION,
                            Map.of(BUSINESS_UNIT_UUID_PARAMETER_NAME, groupObjective.getBusinessUnitUuid(),
                                    PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, groupObjective.getPerformanceCycleUuid()));
                }
            } catch (DuplicateKeyException e) {
                throw databaseConstraintViolation(
                        GROUP_OBJECTIVE_ALREADY_EXISTS,
                        Map.of(BUSINESS_UNIT_UUID_PARAMETER_NAME, groupObjective.getBusinessUnitUuid(),
                                PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, groupObjective.getPerformanceCycleUuid(),
                                SEQUENCE_NUMBER_PARAMETER_NAME, groupObjective.getSequenceNumber(),
                                VERSION_PARAMETER_NAME, groupObjective.getVersion()),
                        e);

            }

        });
        return results;
    }

    @Override
    @Transactional
    public List<GroupObjective> updateGroupObjectives(UUID businessUnitUuid,
                                                      UUID performanceCycleUuid,
                                                      List<GroupObjective> groupObjectives) {
        List<GroupObjective> results = new ArrayList<>();
        groupObjectives.forEach(groupObjective -> {
            try {
                groupObjective.setBusinessUnitUuid(businessUnitUuid);
                groupObjective.setPerformanceCycleUuid(performanceCycleUuid);
                var workingGroupObjective = objectiveDAO.getWorkingGroupObjective(
                        groupObjective.getBusinessUnitUuid(),
                        groupObjective.getPerformanceCycleUuid(),
                        groupObjective.getSequenceNumber());
                if (workingGroupObjective != null) {
                    var oldGroupObjective =
                            objectiveDAO.getGroupObjective(workingGroupObjective.getGroupObjectiveUuid());
                    if (oldGroupObjective.getTitle().equals(groupObjective.getTitle())
                            && oldGroupObjective.getStatus().equals(groupObjective.getStatus())) {
                        groupObjective.setUuid(oldGroupObjective.getUuid());
                        groupObjective.setVersion(oldGroupObjective.getVersion());
                    } else if (oldGroupObjective.getTitle().equals(groupObjective.getTitle())
                            && !oldGroupObjective.getStatus().equals(groupObjective.getStatus())) {
                        groupObjective.setUuid(oldGroupObjective.getUuid());
                        groupObjective.setVersion(oldGroupObjective.getVersion());
                        objectiveDAO.updateGroupObjective(groupObjective);
                    } else {
                        groupObjective.setUuid(UUID.randomUUID());
                        groupObjective.setVersion(oldGroupObjective.getVersion() + 1);
                        objectiveDAO.createGroupObjective(groupObjective);
                        objectiveDAO.updateWorkingGroupObjective(getWorkingGroupObjective(groupObjective));
                    }
                    results.add(groupObjective);
                } else {
                    groupObjective.setUuid(UUID.randomUUID());
                    groupObjective.setVersion(VERSION_1);
                    if (1 == objectiveDAO.createGroupObjective(groupObjective)) {
                        results.add(groupObjective);
                        objectiveDAO.createWorkingGroupObjective(getWorkingGroupObjective(groupObjective));
                    } else {
                        //todo it will work after implementation foreign keys
                        throw notFound(GROUP_OBJECTIVE_FOREIGN_CONSTRAINT_VIOLATION,
                                Map.of(BUSINESS_UNIT_UUID_PARAMETER_NAME, groupObjective.getBusinessUnitUuid(),
                                        PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, groupObjective.getPerformanceCycleUuid()));
                    }
                }
            } catch (DuplicateKeyException e) {
                throw databaseConstraintViolation(
                        GROUP_OBJECTIVE_ALREADY_EXISTS,
                        Map.of(BUSINESS_UNIT_UUID_PARAMETER_NAME, groupObjective.getBusinessUnitUuid(),
                                PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, groupObjective.getPerformanceCycleUuid(),
                                SEQUENCE_NUMBER_PARAMETER_NAME, groupObjective.getSequenceNumber(),
                                VERSION_PARAMETER_NAME, groupObjective.getVersion()),
                        e);

            }

        });
        return results;
    }

    @Override
    public List<GroupObjective> getAllGroupObjectives(UUID businessUnitUuid, UUID performanceCycleUuid) {
        List<GroupObjective> results = objectiveDAO.getAllGroupObjectives(businessUnitUuid, performanceCycleUuid);
        if (results == null) {
            throw notFound(GROUP_OBJECTIVES_NOT_FOUND,
                    Map.of(BUSINESS_UNIT_UUID_PARAMETER_NAME, businessUnitUuid,
                            PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, businessUnitUuid));
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

    private WorkingGroupObjective getWorkingGroupObjective(GroupObjective groupObjective) {
        return WorkingGroupObjective.builder()
                .businessUnitUuid(groupObjective.getBusinessUnitUuid())
                .performanceCycleUuid(groupObjective.getPerformanceCycleUuid())
                .sequenceNumber(groupObjective.getSequenceNumber())
                .version(groupObjective.getVersion())
                .groupObjectiveUuid(groupObjective.getUuid())
                .updateTime(now())
                //todo use real user
                .updaterId("TempUser")
                .build();
    }
}
