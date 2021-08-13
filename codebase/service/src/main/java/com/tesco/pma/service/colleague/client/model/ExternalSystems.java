package com.tesco.pma.service.colleague.client.model;

import lombok.Data;

@Data
public class ExternalSystems {
    private String sourceSystem;
    private IamSourceSystem iam;
}
