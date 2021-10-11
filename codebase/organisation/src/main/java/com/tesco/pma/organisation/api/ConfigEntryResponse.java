package com.tesco.pma.organisation.api;

import com.tesco.pma.api.GeneralDictionaryItem;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ConfigEntryResponse {
    private UUID uuid;
    private String name;
    private GeneralDictionaryItem type;
    private int version;
    private boolean root;
    private String compositeKey;

    private List<ConfigEntryResponse> children;
}
