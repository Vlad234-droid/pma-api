package com.tesco.pma.colleague.api.effectivity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Effectivity implements Serializable {

    private static final long serialVersionUID = 1L;

    LocalDate from;
    LocalDate to;
}
