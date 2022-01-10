package com.tesco.pma.cycle.api;

import com.tesco.pma.api.MapJson;
import com.tesco.pma.cycle.api.model.PMCycleMetadata;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CompositePMCycleMetadataResponse implements Serializable {
    private static final long serialVersionUID = -9187871593097610915L;

    PMCycleMetadata metadata;
    MapJson forms;
}
