package com.tesco.pma.flow.handlers;

import com.tesco.pma.bpm.camunda.flow.handlers.CamundaAbstractFlowHandler;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.process.api.PMProcessErrorCodes;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.Expression;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 14.10.2021 Time: 20:59
 */
@RequiredArgsConstructor
public abstract class AbstractUpdateStatusHandler extends CamundaAbstractFlowHandler {
    protected static final String STATUS_VALUE = "statusValue";
    protected static final String VALUE = "value";
    protected static final String STATUS_SEPARATOR = ",";

    protected NamedMessageSourceAccessor messageSourceAccessor;

    protected Expression statusValue;
    protected Expression oldStatusValues;

    public void setStatusValue(Expression expression) {
        this.statusValue = expression;
    }

    public void setOldStatusValues(Expression oldStatusValues) {
        this.oldStatusValues = oldStatusValues;
    }

    protected String getStatusValue() {
        Objects.requireNonNull(statusValue, messageSourceAccessor.getMessage(PMProcessErrorCodes.VALUE_MUST_BE_SPECIFIED,
                Map.of(VALUE, STATUS_VALUE)));
        return statusValue.getExpressionText();
    }

    protected Set<String> getOldStatusValues() {
        if (oldStatusValues == null || StringUtils.isBlank(oldStatusValues.getExpressionText())) {
            return Collections.emptySet();
        }
        return Set.of(oldStatusValues.getExpressionText().split(STATUS_SEPARATOR));
    }
}
