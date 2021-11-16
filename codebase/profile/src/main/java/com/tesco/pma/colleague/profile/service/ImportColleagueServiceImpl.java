package com.tesco.pma.colleague.profile.service;

import com.tesco.pma.colleague.profile.dao.ImportColleaguesDAO;
import com.tesco.pma.colleague.profile.dao.ProfileDAO;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.colleague.profile.domain.ImportError;
import com.tesco.pma.colleague.profile.domain.ImportReport;
import com.tesco.pma.colleague.profile.domain.ImportRequest;
import com.tesco.pma.colleague.profile.domain.ImportRequestStatus;
import com.tesco.pma.colleague.profile.exception.ErrorCodes;
import com.tesco.pma.colleague.profile.parser.XlsxParser;
import com.tesco.pma.colleague.profile.parser.model.ParsingError;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.tesco.pma.colleague.profile.exception.ErrorCodes.COLLEAGUES_MANAGER_NOT_FOUND;

@Service
@Validated
@RequiredArgsConstructor
public class ImportColleagueServiceImpl implements ImportColleagueService {

    private final ProfileDAO profileDAO;
    private final ImportColleaguesDAO importColleaguesDAO;
    private final NamedMessageSourceAccessor messages;

    @Override
    @Transactional
    public ImportReport importColleagues(InputStream inputStream, String fileName) {
        var requestUuid = UUID.randomUUID();
        var request = buildImportRequest(requestUuid, fileName);
        importColleaguesDAO.registerRequest(request);

        var parser = new XlsxParser();
        var result = parser.parse(inputStream);
        var importErrors = mapParsingErrors(requestUuid, result.getErrors());
        saveErrors(importErrors);

        if (!result.isSuccess()) {
            updateRequestStatus(request, ImportRequestStatus.FAILED);
            return ImportReport.builder()
                    .requestUuid(requestUuid)
                    .skipped(importErrors)
                    .build();
        }

        var workLevels = ColleagueEntityMapper.mapWLs(result.getData());
        workLevels.forEach(profileDAO::saveWorkLevel);

        var countries = ColleagueEntityMapper.mapCountries(result.getData());
        countries.forEach(profileDAO::saveCountry);

        var departments = ColleagueEntityMapper.mapDepartments(result.getData());
        departments.forEach(profileDAO::saveDepartment);

        var jobs = ColleagueEntityMapper.mapJobs(result.getData());
        jobs.forEach(profileDAO::saveJob);

        var colleagues = ColleagueEntityMapper.mapColleagues(result.getData(), workLevels, countries, departments, jobs);
        var importReport = saveColleagues(colleagues, requestUuid);

        updateRequestStatus(request, ImportRequestStatus.PROCESSED);
        saveErrors(importReport.getSkipped());

        return importReport;
    }

    @Override
    public ImportRequest getRequest(UUID requestUuid) {
        return Optional.ofNullable(importColleaguesDAO.getRequest(requestUuid))
                .orElseThrow(() -> new NotFoundException(ErrorCodes.IMPORT_REQUEST_NOT_FOUND.getCode(),
                        messages.getMessage(ErrorCodes.IMPORT_REQUEST_NOT_FOUND, Map.of("requestUuid", requestUuid))));
    }

    @Override
    public List<ImportError> getRequestErrors(UUID requestUuid) {
        return importColleaguesDAO.getRequestErrors(requestUuid);
    }

    private List<ImportError> mapParsingErrors(UUID requestUuid, Collection<ParsingError> errors) {
        return  errors.stream()
                .map(e -> buildImportError(requestUuid, null, e.getCode(), e.getProperties()))
                .collect(Collectors.toList());
    }

    private void saveErrors(Collection<ImportError> skipped) {
        skipped.forEach(importColleaguesDAO::saveError);
    }

    private void updateRequestStatus(ImportRequest request, ImportRequestStatus status) {
        request.setStatus(status);
        importColleaguesDAO.updateRequest(request);
    }

    private ImportRequest buildImportRequest(UUID requestUuid, String fileName) {
        var ir = new ImportRequest();
        ir.setUuid(requestUuid);
        ir.setFileName(fileName);
        ir.setStatus(ImportRequestStatus.REGISTERED);
        return ir;
    }

    private ImportReport saveColleagues(List<ColleagueEntity> colleagues, UUID requestUuid) {
        var uuidToManager = new HashMap<UUID, UUID>();
        var colleagueUuids = colleagues.stream().map(ColleagueEntity::getUuid).collect(Collectors.toSet());
        var builder = ImportReport.builder()
                .requestUuid(requestUuid);
        colleagues.forEach(c -> {
            var colleagueUuid = c.getUuid();
            var managerUuid = c.getManagerUuid();
            if (managerUuid == null || profileDAO.isColleagueExists(managerUuid)) {
                profileDAO.saveColleague(c);
                builder.importColleague(colleagueUuid);
            } else if (colleagueUuids.contains(managerUuid)) {
                c.setManagerUuid(null);
                uuidToManager.put(colleagueUuid, managerUuid);
                profileDAO.saveColleague(c);
                builder.importColleague(colleagueUuid);
            } else {
                builder.skipColleague(buildImportError(requestUuid, colleagueUuid, COLLEAGUES_MANAGER_NOT_FOUND.getCode(),
                        Map.of("colleagueUuid", colleagueUuid,
                                "managerUuid", managerUuid)));
            }

        });
        uuidToManager.forEach(profileDAO::updateColleagueManager);
        return builder.build();
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
