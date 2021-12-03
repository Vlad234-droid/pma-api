package com.tesco.pma.organisation.api;

import com.tesco.pma.api.GeneralDictionaryItem;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class ConfigEntry implements Serializable {
    private static final long serialVersionUID = -6723195649989620889L;
    private UUID uuid;
    private String name;
    private GeneralDictionaryItem type;
    private int version;
    private UUID parentUuid;
    private String compositeKey;
}
