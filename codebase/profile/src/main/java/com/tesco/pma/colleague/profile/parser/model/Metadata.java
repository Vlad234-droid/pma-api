package com.tesco.pma.colleague.profile.parser.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Value
@Builder
public class Metadata {
    @Singular
    List<FieldDescriptor> descriptors;

    public Metadata(List<FieldDescriptor> descriptors) {
        this.descriptors = Objects.requireNonNullElse(descriptors, Collections.emptyList());
    }
}
