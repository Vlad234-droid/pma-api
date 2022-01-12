package com.tesco.pma.colleague.api.effectivity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Effectivity implements Serializable {

    private static final long serialVersionUID = 2399263549373220659L;

    LocalDate from;
    LocalDate to;
}
