package com.tesco.pma.colleague.profile.service;

import com.tesco.pma.colleague.profile.dao.ProfileDAO;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.colleague.profile.domain.ImportError;
import com.tesco.pma.colleague.profile.domain.ImportReport;
import com.tesco.pma.colleague.profile.parser.model.ParsingResult;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.tesco.pma.colleague.profile.exception.ErrorCodes.COLLEAGUES_MANAGER_NOT_FOUND;
import static com.tesco.pma.colleague.profile.exception.ErrorCodes.INVALID_COLLEAGUE_IDENTIFIER;

@Service
@RequiredArgsConstructor
public class ProcessColleaguesDataServiceImpl implements ProcessColleaguesDataService {

    private static final int BATCH_SIZE = 100;
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
        batchifyColleagues(colleagues).forEach(c ->
                processBatchColleagues(c, requestUuid, importReportBuilder, uuidToManager, colleagueUuids)
        );
        uuidToManager.forEach(profileDAO::updateColleagueManager);
    }

    private void processBatchColleagues(List<ColleagueEntity> colleagues, UUID requestUuid,
                                        ImportReport.ImportReportBuilder importReportBuilder,
                                        Map<UUID, UUID> uuidToManager, Set<UUID> colleagueUuids) {
        var existingManagersUuids = getExistingManagersUuids(colleagues);
        var existingColleaguesUuids = getExistingColleaguesUuids(colleagues);
        colleagues.forEach(c -> {
            var colleagueUuid = c.getUuid();
            var managerUuid = c.getManagerUuid();
            if (managerUuid == null || existingManagersUuids.contains(managerUuid)) {
                upsertColleague(c, existingColleaguesUuids);
                importReportBuilder.importColleague(colleagueUuid);
            } else {
                c.setManagerUuid(null);
                upsertColleague(c, existingColleaguesUuids);
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
    }

    private Set<UUID> getExistingManagersUuids(List<ColleagueEntity> colleagues) {
        var managerUuids = colleagues.stream().map(ColleagueEntity::getManagerUuid).filter(Objects::nonNull).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(managerUuids)) {
            return Collections.emptySet();
        }
        return profileDAO.getAllColleaguesUuids(managerUuids);
    }

    private Set<UUID> getExistingColleaguesUuids(List<ColleagueEntity> colleagues) {
        var colleagueUuids = colleagues.stream().map(ColleagueEntity::getUuid).filter(Objects::nonNull).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(colleagueUuids)) {
            return Collections.emptySet();
        }
        return profileDAO.getAllColleaguesUuids(colleagueUuids);
    }

    private Collection<List<ColleagueEntity>> batchifyColleagues(Collection<ColleagueEntity> colleagues) {
        final var counter = new AtomicInteger(0);

        return colleagues.stream()
                .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / BATCH_SIZE)).values();
    }

    private void upsertColleague(ColleagueEntity entity, Set<UUID> existingColleaguesUuids) {
        if (existingColleaguesUuids.contains(entity.getUuid())) {
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
