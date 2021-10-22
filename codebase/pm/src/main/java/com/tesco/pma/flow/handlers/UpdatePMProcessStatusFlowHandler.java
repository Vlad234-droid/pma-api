package com.tesco.pma.flow.handlers;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.tesco.pma.api.DictionaryFilter;
import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.flow.PMProcessFlowVariables;
import com.tesco.pma.process.api.PMProcessErrorCodes;
import com.tesco.pma.process.api.PMProcessStatus;
import com.tesco.pma.process.service.PMProcessService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 08.10.2021 Time: 12:28
 */
@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UpdatePMProcessStatusFlowHandler extends AbstractUpdateStatusFlowHandler {

    @Autowired
    PMProcessService pmProcessService;

    @Override
    protected void execute(ExecutionContext context) {
        log.info("New status {}; old statuses {}", getStatusValue(), getOldStatusValues());

        pmProcessService.updateStatus(context.getVariable(PMProcessFlowVariables.PM_PROCESS_ID),
                getStatus(), DictionaryFilter.includeFilter(getOldStatuses()));
    }

    private PMProcessStatus getStatus() {
        return getStatus(statusValue.getExpressionText());
    }

    private PMProcessStatus getStatus(String status) {
        return Optional.ofNullable(PMProcessStatus.getByCode(status))
                .orElseThrow(() -> new IllegalStateException(
                        messageSourceAccessor.getMessage(PMProcessErrorCodes.WRONG_VALUE, Map.of(STATUS_VALUE, status))));
    }

    private Set<PMProcessStatus> getOldStatuses() {
        return getOldStatusValues().stream().map(this::getStatus)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(PMProcessStatus.class)));
    }
}
