package com.tesco.pma.colleague.profile.service;

import com.tesco.pma.colleague.profile.dao.ImportColleaguesDAO;
import com.tesco.pma.colleague.profile.domain.ImportError;
import com.tesco.pma.colleague.profile.domain.ImportReport;
import com.tesco.pma.colleague.profile.domain.ImportRequest;
import com.tesco.pma.colleague.profile.domain.ImportRequestStatus;
import com.tesco.pma.colleague.profile.exception.ErrorCodes;
import com.tesco.pma.colleague.profile.parser.XlsxParser;
import com.tesco.pma.colleague.profile.parser.model.ParsingError;
import com.tesco.pma.colleague.profile.parser.model.ParsingResult;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.event.EventNames;
import com.tesco.pma.event.EventParams;
import com.tesco.pma.event.EventSupport;
import com.tesco.pma.event.service.EventSender;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.logging.TraceUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Validated
@RequiredArgsConstructor
public class ImportColleagueServiceImpl implements ImportColleagueService {

    private final ImportColleaguesDAO importColleaguesDAO;
    private final NamedMessageSourceAccessor messages;
    private final EventSender eventSender;
    private final ProcessColleaguesDataService processColleaguesService;
    private final RestTemplate restTemplate;

    @Value("${tesco.application.endpoints.default-attributes}")
    private String defaultAttributesUrl;

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

        var importReport = processColleaguesService.processColleagues(requestUuid, result, workLevels, countries, departments, jobs);

        updateRequestStatus(request, ImportRequestStatus.PROCESSED);
        saveErrors(importReport.getSkipped());
        sendEvents(importReport);

        createDefaultAttributes(importReport);

        return importReport;
    }

    private void createDefaultAttributes(ImportReport importReport) {
        var traceId = TraceUtils.getTraceId();
        CompletableFuture.runAsync(() -> {
            TraceUtils.setTraceId(traceId);
            importReport.getImported()
                    .forEach(uuid -> restTemplate.put(defaultAttributesUrl, null, uuid));
        });
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
                .map(e -> buildParsingError(requestUuid, e.getCode(), e.getProperties()))
                .collect(Collectors.toList());
    }

    private ImportRequest buildImportRequest(String fileName) {
        var ir = new ImportRequest();
        ir.setUuid(UUID.randomUUID());
        ir.setFileName(fileName);
        ir.setStatus(ImportRequestStatus.REGISTERED);
        return ir;
    }

    private ImportError buildParsingError(UUID requestUuid, String code, Map<String, ?> params) {
        var ie = new ImportError();
        ie.setRequestUuid(requestUuid);
        ie.setCode(code);
        ie.setMessage(messages.getMessage(code, params));
        return ie;
    }
}
