package com.tesco.pma.colleague.api.service;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceDates {
    LocalDate hireDate;
    LocalDate leavingDate;
}
