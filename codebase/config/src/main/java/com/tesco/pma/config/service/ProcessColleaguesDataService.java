package com.tesco.pma.config.service;

import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.config.domain.BatchImportResult;
import com.tesco.pma.config.parser.model.ParsingResult;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface ProcessColleaguesDataService {

    /**
     * Performs mapping and stores jobs into db
     *
     * @param result - parsing result
     * @return set of stored jobs
     */
    Set<ColleagueEntity.Job> saveJobs(ParsingResult result);

    /**
     * Performs mapping and stores departments into db
     *
     * @param result - parsing result
     * @return set of stored departments
     */
    Set<ColleagueEntity.Department> saveDepartments(ParsingResult result);

    /**
     * Performs mapping and stores countries into db
     *
     * @param result - parsing result
     * @return set of stored countries
     */
    Set<ColleagueEntity.Country> saveCountries(ParsingResult result);

    /**
     * Performs mapping and stores work levels into db
     *
     * @param result - parsing result
     * @return set of stored work levels
     */
    Set<ColleagueEntity.WorkLevel> saveWorkLevels(ParsingResult result);

    /**
     * Validates colleague manager and saves colleagues into db
     *
     * @param colleagues - list of colleague
     * @return result of storage
     */
    BatchImportResult processBatchColleagues(List<ColleagueEntity> colleagues);

    /**
     * Updates colleague manager
     *
     * @param colleagueToManagerMap set of colleague identifier and manager identifier
     */
    void updateColleagueManagers(Collection<Map.Entry<UUID, UUID>> colleagueToManagerMap);
}
