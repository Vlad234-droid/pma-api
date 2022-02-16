package com.tesco.pma.config.service;

import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.colleague.profile.exception.ErrorCodes;
import com.tesco.pma.config.dao.ImportColleaguesDAO;
import com.tesco.pma.config.domain.BatchImportResult;
import com.tesco.pma.config.domain.ImportError;
import com.tesco.pma.config.domain.ImportReport;
import com.tesco.pma.config.domain.ImportRequest;
import com.tesco.pma.config.domain.ImportRequestStatus;
import com.tesco.pma.config.domain.UpdateColleagueManagerResult;
import com.tesco.pma.config.parser.XlsxParser;
import com.tesco.pma.config.parser.model.ParsingError;
import com.tesco.pma.config.parser.model.ParsingResult;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.event.EventNames;
import com.tesco.pma.event.EventParams;
import com.tesco.pma.event.EventSupport;
import com.tesco.pma.event.service.EventSender;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.logging.TraceUtils;
import com.tesco.pma.service.BatchService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.tesco.pma.colleague.profile.exception.ErrorCodes.COLLEAGUES_MANAGER_NOT_FOUND;
import static com.tesco.pma.colleague.profile.exception.ErrorCodes.INVALID_COLLEAGUE_IDENTIFIER;

@Service
@Validated
@RequiredArgsConstructor
public class ImportColleagueServiceImpl implements ImportColleagueService {

    private final ImportColleaguesDAO importColleaguesDAO;
    private final NamedMessageSourceAccessor messages;
    private final EventSender eventSender;
    private final ProcessColleaguesDataService processColleaguesService;
    private final DefaultAttributesService defaultAttributesService;
    private final BatchService batchService;

    @Override
    public ImportReport importColleagues(InputStream inputStream, String fileName) {
        var request = createImportRequest(fileName);
        var requestUuid = request.getUuid();

        var result = parseXlsx(inputStream);
        var importErrors = mapParsingErrors(requestUuid, result.getErrors());
        saveErrors(importErrors);

        if (!result.isSuccess()) {
            updateRequestStatus(request, ImportRequestStatus.FAILED);
            return ImportReport.builder()
                    .requestUuid(requestUuid)
                    .skipped(importErrors)
                    .build();
        }

        var workLevels = processColleaguesService.saveWorkLevels(result);
        var countries = processColleaguesService.saveCountries(result);
        var departments = processColleaguesService.saveDepartments(result);
        var jobs = processColleaguesService.saveJobs(result);

        var importReport = processColleagues(requestUuid, result, workLevels, countries, departments, jobs);

        updateRequestStatus(request, ImportRequestStatus.PROCESSED);
        saveErrors(importReport.getSkipped());
        saveErrors(importReport.getWarn());

        sendEvents(importReport);

        createDefaultAttributes(importReport);

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

    @Transactional
    public ImportRequest createImportRequest(String fileName) {
        var request = buildImportRequest(fileName);
        importColleaguesDAO.registerRequest(request);
        return request;
    }

    @Transactional
    public void saveErrors(Collection<ImportError> skipped) {
        skipped.forEach(importColleaguesDAO::saveError);
    }

    @Transactional
    public void updateRequestStatus(ImportRequest request, ImportRequestStatus status) {
        request.setStatus(status);
        importColleaguesDAO.updateRequest(request);
    }

    private ImportReport processColleagues(UUID requestUuid, ParsingResult result, Set<ColleagueEntity.WorkLevel> workLevels,
                                           Set<ColleagueEntity.Country> countries, Set<ColleagueEntity.Department> departments,
                                           Set<ColleagueEntity.Job> jobs) {
        var validationErrors = ColleagueEntityValidator.validateColleague(result.getData());
        var colleagues = ColleagueEntityMapper.mapColleagues(result.getData(), workLevels, countries, departments, jobs, validationErrors);
        var importReportBuilder = ImportReport.builder();
        saveColleagues(colleagues, requestUuid, importReportBuilder);
        validationErrors.forEach(ve -> importReportBuilder.skipColleague(
                buildError(requestUuid, null, INVALID_COLLEAGUE_IDENTIFIER.getCode(),
                        Map.of("id", StringUtils.defaultString(ve, "null")))));
        return importReportBuilder.build();
    }

    private void saveColleagues(List<ColleagueEntity> colleagues, UUID requestUuid, ImportReport.ImportReportBuilder importReportBuilder) {
        var colleagueUuids = colleagues.stream().map(ColleagueEntity::getUuid).collect(Collectors.toSet());

        importReportBuilder.requestUuid(requestUuid);
        var results = batchService.batchifyCollection(colleagues).stream()
                .map(processColleaguesService::processBatchColleagues).collect(Collectors.toList());

        results.stream().flatMap(r -> r.getSaved().stream()).forEach(importReportBuilder::importColleague);

        var managerResult = updateColleagueManagers(colleagueUuids, results);

        managerResult.getManagerIsNotPresent().forEach((k, v) ->
                importReportBuilder.warnColleague(buildError(requestUuid, k, COLLEAGUES_MANAGER_NOT_FOUND.getCode(),
                        Map.of("colleagueUuid", k, "managerUuid", v))));
    }

    private UpdateColleagueManagerResult updateColleagueManagers(Set<UUID> colleagueUuids, List<BatchImportResult> results) {
        var managerToUpdate = results.stream()
                .flatMap(r -> r.getWithoutManagers().entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


        var managerIsPresent = managerToUpdate.entrySet().stream()
                .filter(es -> colleagueUuids.contains(es.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        batchService.batchifyMap(managerIsPresent)
                .forEach(processColleaguesService::updateColleagueManagers);

        return new UpdateColleagueManagerResult(managerIsPresent,
                managerToUpdate.entrySet().stream()
                        .filter(es -> !managerIsPresent.containsKey(es.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    private void createDefaultAttributes(ImportReport importReport) {
        var traceId = TraceUtils.getTraceId();
        CompletableFuture.runAsync(() -> {
            TraceUtils.setTraceId(traceId);
            importReport.getImported()
                    .forEach(defaultAttributesService::updateDefaultAttributes);
        });
    }

    private ParsingResult parseXlsx(InputStream inputStream) {
        var parser = new XlsxParser();
        return parser.parse(inputStream);
    }

    private void sendEvents(ImportReport importReport) {
        var events = importReport.getImported().stream().map(uuid -> {
            var event = new EventSupport(EventNames.IMPORT_NEW_COLLEAGUE);
            event.setEventProperties(Map.of(EventParams.COLLEAGUE_UUID.name(), uuid));
            return event;
        }).collect(Collectors.toList());
        eventSender.sendEvents(events);

        // Send events to User Management Service on creation new accounts
        events = importReport.getImported().stream().map(uuid -> {
            var event = new EventSupport(EventNames.POST_IMPORT_NEW_COLLEAGUE);
            event.setEventProperties(Map.of(EventParams.COLLEAGUE_UUID.name(), uuid));
            return event;
        }).collect(Collectors.toList());
        eventSender.sendEvents(events);
    }

    private List<ImportError> mapParsingErrors(UUID requestUuid, Collection<ParsingError> errors) {
        return errors.stream()
                .map(e -> buildError(requestUuid, null, e.getCode(), e.getProperties()))
                .collect(Collectors.toList());
    }

    private ImportRequest buildImportRequest(String fileName) {
        var ir = new ImportRequest();
        ir.setUuid(UUID.randomUUID());
        ir.setFileName(fileName);
        ir.setStatus(ImportRequestStatus.REGISTERED);
        return ir;
    }

    private ImportError buildError(UUID requestUuid, UUID colleagueUuid, String code, Map<String, ?> params) {
        var ie = new ImportError();
        ie.setRequestUuid(requestUuid);
        ie.setCode(code);
        ie.setColleagueUuid(colleagueUuid);
        ie.setMessage(messages.getMessage(code, params));
        return ie;
    }

}
