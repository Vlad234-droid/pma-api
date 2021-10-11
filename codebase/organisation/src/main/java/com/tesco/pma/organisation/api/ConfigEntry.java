package com.tesco.pma.organisation.api;

import com.tesco.pma.api.GeneralDictionaryItem;
import lombok.Data;

import java.util.UUID;

@Data
public class ConfigEntry {
    private UUID uuid;
    private String name;
    private GeneralDictionaryItem type;
    private int version;
    private UUID parentUuid;
    private String compositeKey;
}
