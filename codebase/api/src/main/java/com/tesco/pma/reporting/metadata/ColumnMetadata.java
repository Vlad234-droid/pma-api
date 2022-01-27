package com.tesco.pma.reporting.metadata;


import com.tesco.pma.api.DictionaryItem;
import com.tesco.pma.api.ValueType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ColumnMetadata implements DictionaryItem<String> {
    String id;
    String name;
    ValueType type;
    String description;

    @Override
    public String getCode() {
        return name;
    }
}
