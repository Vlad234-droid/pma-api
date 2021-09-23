package com.tesco.pma.organisation.api;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BusinessUnitResponse {
    private UUID uuid;
    private String name;
    private BusinessUnitType type;
    private int version;
    private boolean root;

    private List<BusinessUnitResponse> children;
}
