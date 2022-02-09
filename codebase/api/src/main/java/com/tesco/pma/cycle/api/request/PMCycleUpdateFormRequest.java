package com.tesco.pma.cycle.api.request;

import com.tesco.pma.cycle.api.PMForm;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PMCycleUpdateFormRequest {
    PMForm changeFrom;
    PMForm changeTo;
}
