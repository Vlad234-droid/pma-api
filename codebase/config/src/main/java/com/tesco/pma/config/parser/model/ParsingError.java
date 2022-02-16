package com.tesco.pma.config.parser.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
public class ParsingError {

    private String code;

    @Singular
    private Map<String, String> properties;

    protected ParsingError(@NonNull String code, Map<String, String> properties) {
        this.code = code;
        this.properties = Objects.requireNonNullElse(properties, Collections.emptyMap());
    }
}
