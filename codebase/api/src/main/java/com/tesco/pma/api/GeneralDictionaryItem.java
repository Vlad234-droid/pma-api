package com.tesco.pma.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneralDictionaryItem implements DictionaryItem<Integer> {

    private Integer id;
    private String code;
    private String description;

}
