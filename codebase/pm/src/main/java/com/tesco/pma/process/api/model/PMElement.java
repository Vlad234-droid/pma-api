package com.tesco.pma.process.api.model;

import java.util.Map;

import org.codehaus.groovy.util.ListHashMap;

import com.tesco.pma.api.DictionaryItem;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 16.10.2021 Time: 21:13
 */
@Data
@NoArgsConstructor
public class PMElement implements DictionaryItem<String> {
    private String id;
    private String code;
    private String description;
    private DictionaryItem<Integer> type;
    private Map<String, String> properties = new ListHashMap<>();

    public PMElement(String id, String code, String description, DictionaryItem<Integer> type) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.type = type;
    }
}
