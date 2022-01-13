package com.tesco.pma.colleague.api.workrelationships;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ContractType implements Serializable {

    private static final long serialVersionUID = -6523526160893462462L;

    String sourceCode;
    String sourceName;
    LocalDate endDate;
}
