package com.tesco.pma.config.parser.model;

import com.tesco.pma.api.Identified;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@Builder
public class FieldDescriptor implements Identified<String> {
    @EqualsAndHashCode.Include
    String id;

    String name;
}