package com.tesco.pma.process.service;

import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.process.api.PMProcessErrorCodes;
import com.tesco.pma.process.api.PMProcessMetadata;
import com.tesco.pma.process.api.PMProcessStatus;
import com.tesco.pma.process.api.PMRuntimeProcess;
import com.tesco.pma.process.api.TimelineResponse;
import com.tesco.pma.process.api.model.PMCycle;
import com.tesco.pma.process.dao.PMRuntimeProcessDAO;
import com.tesco.pma.process.dao.PMRuntimeProcessMetadataDAO;
import com.tesco.pma.process.model.PMProcessModelParser;
import com.tesco.pma.process.model.ResourceProvider;

import lombok.RequiredArgsConstructor;

import org.apache.commons.io.IOUtils;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 14.10.2021 Time: 19:15
 */
@Service
@RequiredArgsConstructor
public class PMProcessServiceImpl implements PMProcessService {
    private static final String ID = "id";
    private static final String STATUS = "status";
    private static final String STATUS_FILTER = "status_filter";
    private static final String FORMS_PATH = "/com/tesco/pma/flow/forms/";

    private final PMRuntimeProcessDAO dao;
    private final PMRuntimeProcessMetadataDAO metadataDAO;
    private final NamedMessageSourceAccessor messageSourceAccessor;
    private final ProcessEngine processEngine;

    private final ResourceProvider resourceProvider = new FormsResourceProvider();

    private static class FormsResourceProvider implements ResourceProvider {
        @Override
        public InputStream read(String resourceName) throws IOException {
            return getClass().getResourceAsStream(FORMS_PATH + resourceName);
        }

        @Override
        public String resourceToString(final String resourceName) throws IOException {
            try (InputStream is = getClass().getResourceAsStream(FORMS_PATH + resourceName)) {
                return IOUtils.toString(is);
            }
        }
    }

    @Override
    @Transactional
    public void register(PMRuntimeProcess process) {
        process.setId(UUID.randomUUID());
        process.setStatus(PMProcessStatus.REGISTERED);
        //todo check not null colleagueUuid, bpmProcessName, bpmProcessId
        try {
            dao.create(process);
        } catch (DuplicateKeyException ex) {
            throw new DatabaseConstraintViolationException(PMProcessErrorCodes.PROCESS_ALREADY_EXISTS.getCode(),
                    messageSourceAccessor.getMessage(PMProcessErrorCodes.PROCESS_ALREADY_EXISTS), null, ex);
        }
    }

    @Override
    public PMRuntimeProcess getProcess(UUID uuid) {
        var process = dao.read(uuid);
        if (process == null) {
            throw new NotFoundException(PMProcessErrorCodes.PROCESS_NOT_FOUND.getCode(),
                    messageSourceAccessor.getMessage(PMProcessErrorCodes.PROCESS_NOT_FOUND, Map.of(ID, uuid)));
        }
        return process;
    }

    @Override
    @Transactional
    public void updateStatus(UUID uuid, PMProcessStatus status, DictionaryFilter<PMProcessStatus> statusFilter) {
        if (0 == dao.updateStatus(uuid, status, statusFilter)) {
            throw new NotFoundException(PMProcessErrorCodes.PROCESS_NOT_FOUND.getCode(),
                    messageSourceAccessor.getMessage(PMProcessErrorCodes.PROCESS_NOT_FOUND,
                            Map.of(ID, uuid, STATUS, status, STATUS_FILTER, statusFilter)));
        }
    }

    // todo remove after
    @Override
    public PMProcessMetadata getProcessMetadataByKey(String processKey) {
        var processDefinition = getProcessDefinition(processKey);
        var model = getModel(processDefinition);

        var metadata = new PMProcessMetadata();
        var cycle = new PMCycle();
        cycle.setCode(processDefinition.getName());
        metadata.setCycle(cycle);

        var parser = new PMProcessModelParser(resourceProvider);
        var tasks = model.getModelElementsByType(Task.class);
        parser.parse(cycle, tasks);

        return metadata;
    }

    @Override
    public List<TimelineResponse> getProcessMetadata(UUID uuid) {
        var metadata = metadataDAO.readMetadata(uuid);
        if (metadata == null) {
            throw new NotFoundException(PMProcessErrorCodes.PROCESS_METADATA_NOT_FOUND.getCode(),
                    messageSourceAccessor.getMessage(PMProcessErrorCodes.PROCESS_METADATA_NOT_FOUND, Map.of(ID, uuid)));
        }
        return metadata;
    }

    @Override
    public String getFullMetadata(UUID uuid) {
        var metadata = metadataDAO.getFullMetadata(uuid);
        return metadata;
    }

    @Override
    public void saveProcessMetadata(UUID processUuid, String metadata) {
        try {
            metadataDAO.saveProcessMetadata(processUuid, metadata);
        } catch (DuplicateKeyException ex) {
            throw new DatabaseConstraintViolationException(PMProcessErrorCodes.PROCESS_METADATA_ALREADY_EXISTS.getCode(),
                    messageSourceAccessor.getMessage(PMProcessErrorCodes.PROCESS_METADATA_ALREADY_EXISTS,
                            Map.of(ID, processUuid)), null, ex);
        }
    }

    private BpmnModelInstance getModel(ProcessDefinition processDefinition) {
        return processEngine.getRepositoryService().getBpmnModelInstance(processDefinition.getId());
    }

    private ProcessDefinition getProcessDefinition(String processKey) {
        var processDefinitionQuery = processEngine.getRepositoryService().createProcessDefinitionQuery();
        processDefinitionQuery.processDefinitionKey(processKey);
        return processDefinitionQuery.latestVersion().singleResult();
    }
}
