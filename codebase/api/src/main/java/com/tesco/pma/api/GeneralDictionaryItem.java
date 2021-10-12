package com.tesco.pma.api;

import lombok.Data;

@Data
public class GeneralDictionaryItem implements DictionaryItem<Integer> {

    private Integer id;
    private String code;
    private String description;

}
