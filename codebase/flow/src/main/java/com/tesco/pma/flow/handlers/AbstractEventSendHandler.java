package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.process.api.PMProcessErrorCodes;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.Expression;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
public abstract class AbstractEventSendHandler extends CamundaAbstractFlowHandler {

    private Expression eventNameExpression;
    private Expression isErrorSensitiveExpression;

    private final NamedMessageSourceAccessor messageSourceAccessor;


    public void setEventNameExpression(Expression expression) {
        this.eventNameExpression = expression;
    }

    public String getEventNameExpression() {
        Objects.requireNonNull(eventNameExpression, "eventNameExpression must be specified");

        return Optional.ofNullable(eventNameExpression.getExpressionText())
                .orElseThrow(() -> new IllegalStateException(
                        messageSourceAccessor.getMessage(PMProcessErrorCodes.VALUE_MUST_BE_SPECIFIED.getCode(),
                                Map.of("value", "eventNameExpression"))));
    }

    public boolean isErrorSensitiveExpression() {
        Objects.requireNonNull(isErrorSensitiveExpression, "isErrorSensitiveExpression must be specified");

        return Boolean.parseBoolean(Optional.ofNullable(isErrorSensitiveExpression.getExpressionText())
                .orElseThrow(() -> new IllegalStateException(
                        messageSourceAccessor.getMessage(PMProcessErrorCodes.VALUE_MUST_BE_SPECIFIED.getCode(),
                                Map.of("value", "isErrorSensitiveExpression")))));
    }

    public void setIsErrorSensitiveExpression(Expression isErrorSensitiveExpression) {
        this.isErrorSensitiveExpression = isErrorSensitiveExpression;
    }

}
