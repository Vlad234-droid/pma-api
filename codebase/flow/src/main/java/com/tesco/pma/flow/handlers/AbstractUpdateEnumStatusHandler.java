package com.tesco.pma.flow.handlers;

import com.tesco.pma.process.api.PMProcessErrorCodes;
import lombok.RequiredArgsConstructor;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.EnumUtils.getEnumIgnoreCase;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2021-12-23 12:41
 */
@RequiredArgsConstructor
public abstract class AbstractUpdateEnumStatusHandler<E extends Enum<E>> extends AbstractUpdateStatusHandler {

    protected E getStatus() {
        return getStatus(statusValue.getExpressionText());
    }

    protected E getStatus(String status) {
        return Optional.ofNullable(getEnumIgnoreCase(getEnumClass(), status))
                .orElseThrow(() -> new IllegalStateException(
                        messageSourceAccessor.getMessage(PMProcessErrorCodes.WRONG_VALUE, Map.of(STATUS_VALUE, status))));
    }

    protected Set<E> getOldStatuses() {
        return getOldStatusValues().stream().map(this::getStatus).collect(Collectors.toCollection(() -> EnumSet.noneOf(getEnumClass())));
    }

    protected abstract Class<E> getEnumClass();
}
