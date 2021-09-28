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

import static com.tesco.pma.objective.exception.ErrorCodes.BUSINESS_UNIT_NOT_EXISTS;
import static com.tesco.pma.objective.exception.ErrorCodes.GROUP_OBJECTIVES_NOT_FOUND;
import static com.tesco.pma.objective.exception.ErrorCodes.GROUP_OBJECTIVE_ALREADY_EXISTS;
import static com.tesco.pma.objective.exception.ErrorCodes.PERSONAL_OBJECTIVES_NOT_FOUND;
import static com.tesco.pma.objective.exception.ErrorCodes.PERSONAL_OBJECTIVE_ALREADY_EXISTS;
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
    private final NamedMessageSourceAccessor messageSourceAccessor;

    private static final String PERSONAL_OBJECTIVE_UUID_PARAMETER_NAME = "personalObjectiveUuid";
    private static final String COLLEAGUE_UUID_PARAMETER_NAME = "colleagueUuid";
    private static final String PERFORMANCE_CYCLE_UUID_PARAMETER_NAME = "performanceCycleUuid";
    private static final String BUSINESS_UNIT_UUID_PARAMETER_NAME = "businessUnitUuid";
    private static final String SEQUENCE_NUMBER_PARAMETER_NAME = "sequenceNumber";
    private static final String VERSION_PARAMETER_NAME = "version";

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
    public PersonalObjective getPersonalObjectiveForColleague(UUID colleagueUuid, UUID performanceCycleUuid, Integer sequenceNumber) {
        var res = objectiveDAO.getPersonalObjectiveForColleague(colleagueUuid, performanceCycleUuid, sequenceNumber);
        if (res == null) {
            throw notFound(PERSONAL_OBJECTIVE_NOT_FOUND_FOR_COLLEAGUE,
                    Map.of(COLLEAGUE_UUID_PARAMETER_NAME, colleagueUuid,
                            PERFORMANCE_CYCLE_UUID_PARAMETER_NAME, performanceCycleUuid,
                            SEQUENCE_NUMBER_PARAMETER_NAME, sequenceNumber));
        }
        return res;
    }

    @Override
    public List<PersonalObjective> getPersonalObjectivesForColleague(UUID colleagueUuid, UUID performanceCycleUuid) {
        List<PersonalObjective> results = objectiveDAO.getPersonalObjectivesForColleague(colleagueUuid, performanceCycleUuid);
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
            throw notFound(PERSONAL_OBJECTIVE_NOT_FOUND_BY_UUID,
                    Map.of(PERSONAL_OBJECTIVE_UUID_PARAMETER_NAME, personalObjective.getUuid()));
        }
        return personalObjective;
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
                                SEQUENCE_NUMBER_PARAMETER_NAME, groupObjective.getSequenceNumber(),
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
    public WorkingGroupObjective publishGroupObjectives(UUID businessUnitUuid) {
        final var version = objectiveDAO.getMaxVersionGroupObjective(businessUnitUuid);
        final var workingGroupObjective = getWorkingGroupObjective(businessUnitUuid, version);
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
                //todo use real user
                .updaterId("TempUser")
                .build();
    }
}
