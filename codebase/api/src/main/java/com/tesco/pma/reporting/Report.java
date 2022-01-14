package com.tesco.pma.reporting;

import com.tesco.pma.reporting.metadata.ColumnMetadata;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Report {
    List<List<Object>> data;
    List<ColumnMetadata> metadata;
}
