package com.tesco.pma.organisation.api;

import lombok.Data;

import java.util.UUID;

@Data
public class WorkingBusinessUnit {
    private String name;
    private BusinessUnitType type;
    private int version;
    private UUID unitUuid;
    private String compositeKey;
}
