package com.tesco.pma.colleague.profile.parser.model;

import com.tesco.pma.api.Identified;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@lombok.Value
@Builder
public class FieldSet implements Identified<String> {
    @NonNull String id;

    @Singular
    Map<String, Value> values;

    public FieldSet(@NonNull String id, Map<String, Value> values) {
        this.id = id;
        if (values != null && !values.isEmpty()) {
            values.forEach((valueId, value) -> {
                value.setFieldSet(this);
                value.setId(valueId);
            });
        }
        this.values = Objects.requireNonNullElse(values, Collections.emptyMap());
    }

    public static FieldSetBuilder builderForId(@NonNull Object id) {
        return builder().id(id.toString());
    }
}
