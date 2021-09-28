package com.tesco.pma.objective.service;

import com.tesco.pma.objective.domain.GroupObjective;
import com.tesco.pma.objective.domain.PersonalObjective;
import com.tesco.pma.objective.domain.WorkingGroupObjective;

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
     * Finds personal objective by colleagueUuid, performanceCycleUuid, sequenceNumber.
     *
     * @param colleagueUuid        an identifier of colleague
     * @param performanceCycleUuid an identifier of performance cycle
     * @param sequenceNumber       a sequence number of personal objective
     * @return personal objective
     * @throws com.tesco.pma.exception.NotFoundException if personal objective doesn't exist.
     */
    PersonalObjective getPersonalObjectiveForColleague(@NotNull UUID colleagueUuid,
                                                       @NotNull UUID performanceCycleUuid,
                                                       @NotNull Integer sequenceNumber);

    /**
     * Finds personal objectives by colleagueUuid, performanceCycleUuid.
     *
     * @param colleagueUuid        an identifier of colleague
     * @param performanceCycleUuid an identifier of performance cycle
     * @return a list of personal objectives
     * @throws com.tesco.pma.exception.NotFoundException if personal objectives don't exist.
     */
    List<PersonalObjective> getPersonalObjectivesForColleague(@NotNull UUID colleagueUuid,
                                                              @NotNull UUID performanceCycleUuid);

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
     * @param businessUnitUuid business unit an identifier, not null
     * @param groupObjectives  a list of group's objectives
     * @return Created group's objectives
     * @throws com.tesco.pma.exception.NotFoundException                    if business unit or performance cycle doesn't exist.
     * @throws com.tesco.pma.exception.DatabaseConstraintViolationException group objective already exist.
     */
    List<GroupObjective> createGroupObjectives(@NotNull UUID businessUnitUuid,
                                               List<GroupObjective> groupObjectives);

    /**
     * Get all group's objectives
     *
     * @param businessUnitUuid business unit an identifier, not null
     * @return a list of all group's objectives
     */
    List<GroupObjective> getAllGroupObjectives(@NotNull UUID businessUnitUuid);

    /**
     * Publish the last version of group objectives
     *
     * @param businessUnitUuid business unit an identifier, not null
     * @return a working group objective
     */
    WorkingGroupObjective publishGroupObjectives(@NotNull UUID businessUnitUuid);

    /**
     * Un-publish group objectives
     *
     * @param businessUnitUuid business unit an identifier, not null
     */
    void unpublishGroupObjectives(@NotNull UUID businessUnitUuid);

    /**
     * Get published group's objectives
     *
     * @param businessUnitUuid business unit an identifier, not null
     * @return a list of published group's objectives
     */
    List<GroupObjective> getPublishedGroupObjectives(@NotNull UUID businessUnitUuid);
}
