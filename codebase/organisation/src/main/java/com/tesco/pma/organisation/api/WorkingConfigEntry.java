package com.tesco.pma.organisation.api;

import com.tesco.pma.api.GeneralDictionaryItem;
import lombok.Data;

import java.util.UUID;

@Data
public class WorkingConfigEntry {
    private String name;
    private GeneralDictionaryItem type;
    private int version;
    private UUID configEntryUuid;
    private String compositeKey;
}
