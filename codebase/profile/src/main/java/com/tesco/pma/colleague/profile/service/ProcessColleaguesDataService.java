package com.tesco.pma.colleague.profile.service;

import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.colleague.profile.domain.ImportReport;
import com.tesco.pma.colleague.profile.parser.model.ParsingResult;

import java.util.Set;
import java.util.UUID;

public interface ProcessColleaguesDataService {

    /**
     * Performs validation, mapping and store colleagues into db
     *
     * @param requestUuid - request uuid
     * @param result      - parsing result
     * @param workLevels  - parsed work levels
     * @param countries   - parsed countries
     * @param departments - parsed departments
     * @param jobs        - parsed jobs
     * @return import report
     */
    ImportReport processColleagues(UUID requestUuid, ParsingResult result, Set<ColleagueEntity.WorkLevel> workLevels,
                                   Set<ColleagueEntity.Country> countries, Set<ColleagueEntity.Department> departments,
                                   Set<ColleagueEntity.Job> jobs);

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
}
