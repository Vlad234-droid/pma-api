package com.tesco.pma.organisation.api;

import lombok.Data;

import java.util.UUID;

@Data
public class ConfigEntry {
    private UUID uuid;
    private String name;
    private ConfigEntryType type;
    private int version;
    private UUID parentUuid;
}
