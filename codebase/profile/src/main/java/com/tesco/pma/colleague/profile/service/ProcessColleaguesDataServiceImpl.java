package com.tesco.pma.colleague.profile.service;

import com.tesco.pma.colleague.profile.dao.ProfileDAO;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.colleague.profile.domain.ImportError;
import com.tesco.pma.colleague.profile.domain.ImportReport;
import com.tesco.pma.colleague.profile.parser.model.ParsingResult;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.tesco.pma.colleague.profile.exception.ErrorCodes.COLLEAGUES_MANAGER_NOT_FOUND;
import static com.tesco.pma.colleague.profile.exception.ErrorCodes.INVALID_COLLEAGUE_IDENTIFIER;

@Service
@RequiredArgsConstructor
public class ProcessColleaguesDataServiceImpl implements ProcessColleaguesDataService {

    private final ProfileDAO profileDAO;
    private final NamedMessageSourceAccessor messages;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ImportReport processColleagues(UUID requestUuid, ParsingResult result, Set<ColleagueEntity.WorkLevel> workLevels,
                                          Set<ColleagueEntity.Country> countries, Set<ColleagueEntity.Department> departments,
                                          Set<ColleagueEntity.Job> jobs) {
        var validationErrors = ColleagueEntityValidator.validateColleague(result.getData());
        var colleagues = ColleagueEntityMapper.mapColleagues(result.getData(), workLevels, countries, departments, jobs, validationErrors);
        var importReportBuilder = ImportReport.builder();
        saveColleagues(colleagues, requestUuid, importReportBuilder);
        validationErrors.forEach(ve -> importReportBuilder.skipColleague(
                buildImportError(requestUuid, null, INVALID_COLLEAGUE_IDENTIFIER.getCode(),
                        Map.of("id", StringUtils.defaultString(ve, "null")))));
        return importReportBuilder.build();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Set<ColleagueEntity.Job> saveJobs(ParsingResult result) {
        var jobs = ColleagueEntityMapper.mapJobs(result.getData());
        jobs.forEach(profileDAO::updateJob);
        return jobs;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Set<ColleagueEntity.Department> saveDepartments(ParsingResult result) {
        var departments = ColleagueEntityMapper.mapDepartments(result.getData());
        departments.forEach(profileDAO::updateDepartment);
        return departments;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Set<ColleagueEntity.Country> saveCountries(ParsingResult result) {
        var countries = ColleagueEntityMapper.mapCountries(result.getData());
        countries.forEach(profileDAO::updateCountry);
        return countries;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Set<ColleagueEntity.WorkLevel> saveWorkLevels(ParsingResult result) {
        var workLevels = ColleagueEntityMapper.mapWLs(result.getData());
        workLevels.forEach(profileDAO::updateWorkLevel);
        return workLevels;
    }

    private void saveColleagues(List<ColleagueEntity> colleagues, UUID requestUuid, ImportReport.ImportReportBuilder importReportBuilder) {
        var uuidToManager = new HashMap<UUID, UUID>();
        var colleagueUuids = colleagues.stream().map(ColleagueEntity::getUuid).collect(Collectors.toSet());
        importReportBuilder.requestUuid(requestUuid);
        var existingColleaguesUuids = profileDAO.getAllColleaguesUuids();
        colleagues.forEach(c -> {
            var colleagueUuid = c.getUuid();
            var managerUuid = c.getManagerUuid();
            if (managerUuid == null || existingColleaguesUuids.contains(managerUuid)) {
                upsertColleague(c);
                importReportBuilder.importColleague(colleagueUuid);
            } else {
                c.setManagerUuid(null);
                upsertColleague(c);
                importReportBuilder.importColleague(colleagueUuid);
                if (colleagueUuids.contains(managerUuid)) {
                    uuidToManager.put(colleagueUuid, managerUuid);
                } else {
                    importReportBuilder.warnColleague(buildImportError(requestUuid, colleagueUuid, COLLEAGUES_MANAGER_NOT_FOUND.getCode(),
                            Map.of("colleagueUuid", colleagueUuid,
                                    "managerUuid", managerUuid)));
                }
            }

        });
        uuidToManager.forEach(profileDAO::updateColleagueManager);
    }

    private void upsertColleague(ColleagueEntity entity) {
        if (profileDAO.isColleagueExists(entity.getUuid())) {
            profileDAO.updateColleague(entity);
        } else {
            profileDAO.saveColleague(entity);
        }
    }

    private ImportError buildImportError(UUID requestUuid, UUID colleagueUuid, String code, Map<String, ?> params) {
        var ie = new ImportError();
        ie.setRequestUuid(requestUuid);
        ie.setColleagueUuid(colleagueUuid);
        ie.setCode(code);
        ie.setMessage(messages.getMessage(code, params));
        return ie;
    }

}
