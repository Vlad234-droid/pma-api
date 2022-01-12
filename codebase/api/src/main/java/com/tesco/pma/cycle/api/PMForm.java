package com.tesco.pma.cycle.api;

import com.tesco.pma.cycle.api.model.PMFormElement;
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
