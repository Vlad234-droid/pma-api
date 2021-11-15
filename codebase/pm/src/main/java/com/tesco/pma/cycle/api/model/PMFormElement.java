package com.tesco.pma.cycle.api.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Vadim Shatokhin <a href="mailto:VShatokhin@luxoft.com">VShatokhin@luxoft.com</a> Date: 19.10.2021 Time: 11:07
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PMFormElement extends PMElement {
    public static final String PM_FORM = "form";
    public static final String PM_FORM_PREFIX = PMElement.PM_PREFIX + PM_FORM + "_";
    public static final String PM_FORM_KEY = PM_FORM_PREFIX + "key";
    public static final String PM_FORM_CODE = PM_FORM_PREFIX + "code";
    public static final String PM_FORM_JSON = PM_FORM_PREFIX + "json";

    private String key;
    private String json;

    public PMFormElement(String key, String code, String json) {
        super(null, code, null, PMElementType.FORM);
        this.key = key;
        this.json = json;
    }

    public static List<String> getPropertyNames() {
        return getPropertyNames(PMFormElement.class, PM_FORM_PREFIX + "(?!prefix)([\\w]+)$");
    }
}