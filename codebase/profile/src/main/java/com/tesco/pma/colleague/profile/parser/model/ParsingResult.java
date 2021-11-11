package com.tesco.pma.colleague.profile.parser.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Value
@Builder
public class ParsingResult {

    Metadata metadata;

    @Singular("datum")
    List<FieldSet> data;

    @Singular
    Set<ParsingError> errors;

    boolean success;

    public ParsingResult(Metadata metadata, List<FieldSet> data, Set<ParsingError> errors, boolean success) {
        this.metadata = metadata;
        this.data = Objects.requireNonNullElse(data, Collections.emptyList());
        this.errors = Objects.requireNonNullElse(errors, Collections.emptySet());
        this.success = success;
    }

}
