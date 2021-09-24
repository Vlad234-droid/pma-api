package com.tesco.pma.objective.service;

import com.tesco.pma.objective.domain.GroupObjective;
import com.tesco.pma.objective.domain.PersonalObjective;

import javax.validation.constraints.NotNull;
import java.util.List;
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

    /**
     * Create group's objectives
     *
     * @param businessUnitUuid     business unit an identifier, not null
     * @param performanceCycleUuid performance cycle an identifier, not null
     * @param groupObjectives
     * @return Created group's objectives
     * @throws com.tesco.pma.exception.NotFoundException                    if business unit or performance cycle doesn't exist.
     * @throws com.tesco.pma.exception.DatabaseConstraintViolationException group objective already exist.
     */
    List<GroupObjective> createGroupObjectives(@NotNull UUID businessUnitUuid,
                                               @NotNull UUID performanceCycleUuid,
                                               List<GroupObjective> groupObjectives);

    /**
     * Update group's objectives
     *
     * @param businessUnitUuid     business unit an identifier, not null
     * @param performanceCycleUuid performance cycle an identifier, not null
     * @param groupObjectives
     * @return Updated group's objectives
     */
    List<GroupObjective> updateGroupObjectives(@NotNull UUID businessUnitUuid,
                                               @NotNull UUID performanceCycleUuid,
                                               List<GroupObjective> groupObjectives);

    /**
     * Get all group's objectives
     *
     * @param businessUnitUuid     business unit an identifier, not null
     * @param performanceCycleUuid performance cycle an identifier, not null
     * @return a list of all group's objectives
     */
    List<GroupObjective> getAllGroupObjectives(@NotNull UUID businessUnitUuid, @NotNull UUID performanceCycleUuid);
}
