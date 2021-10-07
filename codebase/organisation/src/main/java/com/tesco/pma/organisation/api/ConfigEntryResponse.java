package com.tesco.pma.organisation.api;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ConfigEntryResponse {
    private UUID uuid;
    private String name;
    private ConfigEntryType type;
    private int version;
    private boolean root;

    private List<ConfigEntryResponse> children;
}
