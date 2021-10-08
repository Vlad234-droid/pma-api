package com.tesco.pma.flow.handlers;

import java.util.Objects;
import java.util.Optional;

import org.camunda.bpm.engine.delegate.Expression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.tesco.pma.bpm.api.flow.ExecutionContext;
import com.tesco.pma.bpm.api.flow.FlowMessages;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.service.deployment.ProcessMetadataService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 08.10.2021 Time: 12:28
 */
@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class UpdatePerformanceCycleStatus extends CamundaAbstractFlowHandler {
    private Expression statusValue;

    @Autowired
    ProcessMetadataService processMetadataService;

    @Override
    protected void execute(ExecutionContext context) {
        log.info("New status {}", getStatusValue());
        log.info("Processes {}", processMetadataService.getTimelines());
    }

    public void setStatusValue(Expression expression) {
        this.statusValue = expression;
    }

    public String getStatusValue() {
        Objects.requireNonNull(statusValue, "injectedValue must be specified");
        return Optional.ofNullable(statusValue.getExpressionText())
                .orElseThrow(() -> new IllegalStateException(FlowMessages.FLOW_ERROR_RUNTIME
                        .format("Wrong injectedValue: %s", statusValue.getExpressionText())));
    }
}
