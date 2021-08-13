package com.tesco.pma.service.colleague.client.model.service;

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
