package com.tesco.pma.cycle.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PMForm extends PMFormElement {
    private String json;

    public PMForm(String id, String key, String code, String json) {
        super(id, key, code);
        this.json = json;
    }
}
