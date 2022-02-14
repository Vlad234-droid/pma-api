package com.tesco.pma.config.service;

import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.config.parser.model.ParsingResult;
import com.tesco.pma.config.domain.BatchImportResult;
import com.tesco.pma.config.domain.UpdateColleagueManagerResult;

import java.util.List;
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

    BatchImportResult processBatchColleagues(List<ColleagueEntity> colleagues);

    UpdateColleagueManagerResult updateColleagueManagers(Set<UUID> colleagueUuids, List<BatchImportResult> results);
}
