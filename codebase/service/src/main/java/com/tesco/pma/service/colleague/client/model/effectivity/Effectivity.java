package com.tesco.pma.service.colleague.client.model.effectivity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Effectivity {
    LocalDate from;
    LocalDate to;
}
