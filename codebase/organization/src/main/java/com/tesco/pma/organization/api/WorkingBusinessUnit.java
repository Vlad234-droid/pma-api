package com.tesco.pma.organization.api;

import lombok.Data;

import java.util.UUID;

@Data
public class WorkingBusinessUnit {
    private String name;
    private BusinessUnitType type;
    private int version;
    private UUID rootUuid;
}
