package com.tesco.pma.process.service;

import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.cycle.api.model.PMCycleMetadata;
import com.tesco.pma.cycle.model.PMProcessModelParser;
import com.tesco.pma.cycle.model.ResourceProvider;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.process.api.PMProcessErrorCodes;
import com.tesco.pma.process.api.PMProcessStatus;
import com.tesco.pma.process.api.PMRuntimeProcess;
import com.tesco.pma.process.dao.PMRuntimeProcessDAO;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final PMRuntimeProcessDAO dao;
    private final NamedMessageSourceAccessor messageSourceAccessor;
    private final ProcessEngine processEngine;
    private final ResourceProvider resourceProvider;

    @Override
    @Transactional
    public PMRuntimeProcess register(PMRuntimeProcess process) {
        return register(process, PMProcessStatus.REGISTERED);
    }

    @Override
    @Transactional
    public PMRuntimeProcess register(PMRuntimeProcess process, PMProcessStatus status) {
        return intRegister(process, status);
    }

    private PMRuntimeProcess intRegister(PMRuntimeProcess process, PMProcessStatus status) {
        process.setId(UUID.randomUUID());
        process.setStatus(status);
        //todo check not null businessKey, bpmProcessId
        try {
            dao.create(process);
            return process;
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
    public PMCycleMetadata getProcessMetadataByKey(String processKey) {
        var processDefinition = getProcessDefinition(processKey);
        var model = getModel(processDefinition);
        var parser = new PMProcessModelParser(resourceProvider, messageSourceAccessor);
        return parser.parse(model);
    }

    @Override
    public List<PMRuntimeProcess> findByCycleUuidAndStatus(UUID cycleUUID, DictionaryFilter<PMProcessStatus> statusFilter) {
        return dao.findByCycleUuidAndStatus(cycleUUID, statusFilter);
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
