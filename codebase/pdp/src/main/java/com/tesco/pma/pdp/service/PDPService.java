package com.tesco.pma.pdp.service;

import com.tesco.pma.pdp.domain.PDPGoal;


import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

/**
 * Personal Development Plan service.
 * Implementation classes must be annotated with @org.springframework.validation.annotation.Validated.
 */
public interface PDPService {

    /**
     * Create a PDP with its Goals
     *
     * @param colleagueUuid is colleague identifier
     * @param goals         are non-empty list of PDP goals
     * @return list of created PDP goals
     */
    List<PDPGoal> createGoals(@NotNull UUID colleagueUuid, @NotEmpty List<PDPGoal> goals);

    /**
     * Update a PDP with its Goals
     *
     * @param colleagueUuid is colleague identifier
     * @param goals         are non-empty list of PDP goals
     * @return list of updated PDP goals
     */
    List<PDPGoal> updateGoals(@NotNull UUID colleagueUuid, @NotEmpty List<PDPGoal> goals);

    /**
     * Delete PDP Goal from Plan by its uuid
     *
     * @param colleagueUuid is colleague identifier
     * @param goalUuids     are goal identifiers
     */
    void deleteGoals(@NotNull UUID colleagueUuid, @NotEmpty List<UUID> goalUuids);

    /**
     * Get a PDP Goal by its colleague and number
     *
     * @param colleagueUuid is colleague identifier
     * @param number        is goal's number
     * @return PDP Goal by its colleagueUuid and number
     */
    PDPGoal getGoal(@NotNull UUID colleagueUuid, @NotNull Integer number);

    /**
     * Get a PDP Goal by its uuid
     *
     * @param colleagueUuid is colleague identifier
     * @param goalUuid is goal identifier
     * @return PDP Goal by its uuid
     */
    PDPGoal getGoal(@NotNull UUID colleagueUuid, @NotNull UUID goalUuid);

    /**
     * Get a list of PDP Goals by its colleague ordered by its number
     *
     * @param colleagueUuid is colleague identifier
     * @return list of PDP Goals by its colleagueUuid
     */
    List<PDPGoal> getGoals(@NotNull UUID colleagueUuid);
}