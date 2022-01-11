package com.tesco.pma.colleague.api;

import lombok.Data;

import java.io.Serializable;

@Data
public class ExternalSystems implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sourceSystem;
    private IamSourceSystem iam;
}
