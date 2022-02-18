package com.tesco.pma.cycle.api.request;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PMCycleUpdateFormRequest {
    PMFormRequest changeFrom;
    PMFormRequest changeTo;
}
