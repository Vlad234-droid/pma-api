package com.tesco.pma.config.service;

import com.tesco.pma.colleague.profile.dao.ProfileDAO;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.config.domain.BatchImportResult;
import com.tesco.pma.config.parser.model.ParsingResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProcessColleaguesDataServiceImpl implements ProcessColleaguesDataService {

    private final ProfileDAO profileDAO;

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

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BatchImportResult processBatchColleagues(List<ColleagueEntity> colleagues) {
        var existingManagersUuids = getExistingManagersUuids(colleagues);
        var existingColleaguesUuids = getExistingColleaguesUuids(colleagues);

        var ir = BatchImportResult.builder();
        colleagues.forEach(c -> {
            var managerUuid = c.getManagerUuid();
            if (managerUuid != null && !existingManagersUuids.contains(managerUuid)) {
                c.setManagerUuid(null);
                ir.withoutManager(c.getUuid(), managerUuid);
            }
            upsertColleague(c, existingColleaguesUuids);
            ir.saved(c.getUuid());
        });
        return ir.build();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateColleagueManagers(Collection<Map.Entry<UUID, UUID>> colleagueToManagerMap) {
        colleagueToManagerMap.forEach(es -> profileDAO.updateColleagueManager(es.getKey(), es.getValue()));
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

    private void upsertColleague(ColleagueEntity entity, Set<UUID> existingColleaguesUuids) {
        if (existingColleaguesUuids.contains(entity.getUuid())) {
            profileDAO.updateColleague(entity);
        } else {
            profileDAO.saveColleague(entity);
        }
    }

}
