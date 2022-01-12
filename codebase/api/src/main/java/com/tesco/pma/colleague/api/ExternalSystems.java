package com.tesco.pma.colleague.api;

import lombok.Data;

import java.io.Serializable;

@Data
public class ExternalSystems implements Serializable {

    private static final long serialVersionUID = 1967049566448106364L;

    private String sourceSystem;
    private IamSourceSystem iam;
}
