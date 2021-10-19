package com.tesco.pma.process.service;

import java.util.Map;
import java.util.UUID;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.DatabaseConstraintViolationException;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.process.api.PMRuntimeProcess;
import com.tesco.pma.process.api.PMProcessErrorCodes;
import com.tesco.pma.process.api.PMProcessStatus;
import com.tesco.pma.process.dao.PMRuntimeProcessDAO;

import lombok.RequiredArgsConstructor;

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
}
