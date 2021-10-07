package com.tesco.pma.organisation.api;

import com.tesco.pma.api.DictionaryItem;
import lombok.Data;

@Data
public class ConfigEntryType implements DictionaryItem<Integer> {

    private Integer id;
    private String code;
    private String description;

}
