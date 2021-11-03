package com.tesco.pma.colleague.profile.parser.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
public class ProcessingError {

    private UUID id;

    private String code;

    @Singular
    private Map<String, String> properties;

    protected ProcessingError(UUID id, @NonNull String code, Map<String, String> properties) {
        this.id = Objects.requireNonNullElse(id, UUID.randomUUID());
        this.code = code;
        this.properties = Objects.requireNonNullElse(properties, Collections.emptyMap());
    }
}
