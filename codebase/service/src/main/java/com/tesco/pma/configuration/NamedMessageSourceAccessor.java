package com.tesco.pma.configuration;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class NamedMessageSourceAccessor extends MessageSourceAccessor {
    public NamedMessageSourceAccessor(MessageSource messageSource) {
        super(messageSource);
    }

    public NamedMessageSourceAccessor(MessageSource messageSource, Locale defaultLocale) {
        super(messageSource, defaultLocale);
    }

    public <E extends Enum<E>> String getMessage(Enum<E> enumClass) {
        return Optional.ofNullable(enumClass)
                .map(e -> getMessage(e.name()))
                .orElseThrow(() -> new IllegalArgumentException("Enum cannot be null"));
    }

    public <E extends Enum<E>> String getMessage(Enum<E> enumClass, Map<String, ?> params) {
        return Optional.ofNullable(enumClass)
                .map(e -> getMessage(e.name(), params))
                .orElseThrow(() -> new IllegalArgumentException("Enum cannot be null"));
    }

    public String getMessage(String code, Map<String, ?> params) {
        if (StringUtils.isEmpty(code)) {
            return null;
        }
        var message = getMessage(code);
        return StringSubstitutor.replace(message, params, "{", "}");
    }

    public String getMessageForParams(String code, Map<String, ?> params) {
        if (StringUtils.isEmpty(code)) {
            return null;
        }
        var mapAsString = params.keySet().stream().sorted()
                .map(key -> key + "=" + params.get(key))
                .collect(Collectors.joining(", ", "{", "}"));
        var message = getMessage(code);
        return StringSubstitutor.replace(message, Map.of("params", mapAsString), "{", "}");
    }
}
