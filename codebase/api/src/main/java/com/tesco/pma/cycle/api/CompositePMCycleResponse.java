package com.tesco.pma.cycle.api;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class CompositePMCycleResponse implements Serializable {
    private PMCycle cycle;
    private List<PMForm> forms;
}
