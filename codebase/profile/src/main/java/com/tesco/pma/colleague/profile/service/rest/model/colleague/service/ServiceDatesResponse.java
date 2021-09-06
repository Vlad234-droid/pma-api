package com.tesco.pma.colleague.profile.service.rest.model.colleague.service;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceDatesResponse {
    LocalDate hireDate;
    LocalDate leavingDate;
}
