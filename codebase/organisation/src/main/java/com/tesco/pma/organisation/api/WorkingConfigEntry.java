package com.tesco.pma.organisation.api;

import lombok.Data;

import java.util.UUID;

@Data
public class WorkingConfigEntry {
    private String name;
    private ConfigEntryType type;
    private int version;
    private UUID configEntryUuid;
    private String compositeKey;
}
