package com.tesco.pma.organisation.api;

import lombok.Data;

import java.util.UUID;

@Data
public class BusinessUnit {
    private UUID uuid;
    private String name;
    private BusinessUnitType type;
    private int version;
    private UUID parentUuid;
}
