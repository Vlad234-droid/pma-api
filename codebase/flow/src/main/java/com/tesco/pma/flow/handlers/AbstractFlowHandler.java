package com.tesco.pma.flow.handlers;

import java.util.Objects;
import java.util.Optional;

import org.camunda.bpm.engine.delegate.Expression;

import com.tesco.pma.bpm.api.flow.FlowMessages;
import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 14.06.2021 Time: 17:54
 */
@Slf4j
public abstract class AbstractFlowHandler extends CamundaAbstractFlowHandler {
    public enum Params {
        IS_DIRECT
    }

    private Expression injectedValue;

    public void setInjectedValue(Expression expression) {
        this.injectedValue = expression;
    }

    public String getInjectedValue() {
        Objects.requireNonNull(injectedValue, "injectedValue must be specified");
        return Optional.ofNullable(injectedValue.getExpressionText())
                .orElseThrow(() -> new IllegalStateException(FlowMessages.FLOW_ERROR_RUNTIME
                        .format("Wrong injectedValue: %s", injectedValue.getExpressionText())));
    }
}
