package com.tesco.pma.colleague.api;

import lombok.Data;

@Data
public class ExternalSystems {
    private String sourceSystem;
    private IamSourceSystem iam;
}
