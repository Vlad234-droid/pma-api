package com.tesco.pma.colleague.api.service;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceDates implements Serializable {

    private static final long serialVersionUID = 4797138225709404661L;

    LocalDate hireDate;
    LocalDate leavingDate;
}
