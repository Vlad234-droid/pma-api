package com.tesco.pma.objective.service;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.error.ErrorCodeAware;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.objective.dao.ObjectiveDAO;
import com.tesco.pma.objective.domain.PersonalObjective;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Map;
import java.util.UUID;

import static com.tesco.pma.objective.exception.ErrorCodes.PERSONAL_OBJECTIVE_ALREADY_EXISTS;
import static com.tesco.pma.objective.exception.ErrorCodes.PERSONAL_OBJECTIVE_NOT_FOUND;

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
    private static final String SEQUENCE_NUMBER_PARAMETER_NAME = "sequenceNumber";

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
}
