package com.tesco.pma.cycle.api;

import com.tesco.pma.cycle.api.model.PMCycleMetadata;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class CompositePMCycleMetadataResponse implements Serializable {
    private static final long serialVersionUID = -9187871593097610915L;

    private PMCycleMetadata metadata;
    private List<PMForm> forms;
}
