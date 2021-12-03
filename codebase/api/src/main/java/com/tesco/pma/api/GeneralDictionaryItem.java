package com.tesco.pma.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneralDictionaryItem implements DictionaryItem<Integer>, Serializable {

    private Integer id;
    private String code;
    private String description;

}
