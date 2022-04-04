package com.tesco.pma.flow.handlers;

import lombok.RequiredArgsConstructor;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.tesco.pma.process.api.PMProcessErrorCodes.WRONG_VALUE;
import static org.apache.commons.lang3.EnumUtils.getEnumIgnoreCase;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2021-12-23 12:41
 */
@RequiredArgsConstructor
public abstract class AbstractUpdateEnumStatusHandler<E extends Enum<E>> extends AbstractUpdateStatusHandler {

    protected static final String PARAM_NAME = "paramName";
    protected static final String PARAM_VALUE = "paramValue";

    protected E getStatus() {
        return getStatus(statusValue.getExpressionText());
    }

    protected E getStatus(String status) {
        return Optional.ofNullable(getEnumIgnoreCase(getEnumClass(), status))
                .orElseThrow(() -> new IllegalStateException(
                        messageSourceAccessor.getMessage(WRONG_VALUE, Map.of(PARAM_NAME, STATUS_VALUE, PARAM_VALUE, status))));
    }

    protected Set<E> getOldStatuses() {
        return getOldStatusValues().stream().map(this::getStatus).collect(Collectors.toCollection(() -> EnumSet.noneOf(getEnumClass())));
    }

    protected abstract Class<E> getEnumClass();
}
