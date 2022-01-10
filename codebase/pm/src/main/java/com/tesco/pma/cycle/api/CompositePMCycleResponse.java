package com.tesco.pma.cycle.api;

import com.tesco.pma.api.MapJson;
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
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompositePMCycleResponse implements Serializable {
    PMCycle cycle;
    MapJson forms;
}
