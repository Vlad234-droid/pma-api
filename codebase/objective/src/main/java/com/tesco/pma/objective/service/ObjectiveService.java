package com.tesco.pma.objective.service;

import com.tesco.pma.objective.domain.PersonalObjective;

import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Objective service
 */
public interface ObjectiveService {

    /**
     * Finds personal objective by uuid.
     *
     * @param personalObjectiveUuid an identifier
     * @return personal objective
     * @throws com.tesco.pma.exception.NotFoundException if personal objective doesn't exist.
     */
    PersonalObjective getPersonalObjectiveByUuid(@NotNull UUID personalObjectiveUuid);

    /**
     * Creates personal objective.
     *
     * @param personalObjective personal objective.
     * @return created personal objective.
     * @throws com.tesco.pma.exception.DatabaseConstraintViolationException personal objective already exist.
     */
    PersonalObjective createPersonalObjective(@NotNull PersonalObjective personalObjective);

    /**
     * Updates existing personal objective.
     *
     * @param personalObjective personal objective.
     * @return updated personal objective.
     * @throws com.tesco.pma.exception.NotFoundException if personal objective doesn't exist.
     */
    PersonalObjective updatePersonalObjective(@NotNull PersonalObjective personalObjective);

    /**
     * Deletes personal objective.
     *
     * @param personalObjectiveUuid an identifier.
     * @throws com.tesco.pma.exception.NotFoundException if personal objective doesn't exist.
     */
    void deletePersonalObjective(@NotNull UUID personalObjectiveUuid);
}
