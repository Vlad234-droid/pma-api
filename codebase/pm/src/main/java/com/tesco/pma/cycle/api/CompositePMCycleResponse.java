package com.tesco.pma.cycle.api;

import com.tesco.pma.cycle.api.model.PMFormElement;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompositePMCycleResponse implements Serializable {
    PMCycle cycle;
    List<PMFormElement> forms;
}
