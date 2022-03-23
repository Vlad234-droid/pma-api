package com.tesco.pma.cycle.api;

import com.tesco.pma.cycle.api.model.PMCycleMetadata;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompositePMCycleMetadataResponse implements Serializable {
    private static final long serialVersionUID = -9187871593097610915L;

    PMCycleMetadata metadata;
    private List<PMForm> forms;
}
