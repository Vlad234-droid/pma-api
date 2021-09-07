package com.tesco.pma.service.colleague.client.model.workrelationships;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContractType {
    String sourceCode;
    String sourceName;
    LocalDate endDate;
}
