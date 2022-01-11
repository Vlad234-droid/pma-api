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
    private static final long serialVersionUID = -2034323460586581869L;

    public static final String PM_FORM = "form";
    public static final String PM_FORM_PREFIX = PM_PREFIX + PM_FORM + "_";
    public static final String PM_FORM_KEY = PM_FORM_PREFIX + "key";
    public static final String PM_FORM_CODE = PM_FORM_PREFIX + "code";
    public static final String PM_FORM_JSON = PM_FORM_PREFIX + "json";

    private String key;
    private String json;

    public PMFormElement() {
        setType(PMElementType.FORM);
    }

    public PMFormElement(String id, String key, String code) {
        super(id, code, null, PMElementType.FORM);
        this.key = key;
    }

    public static List<String> getPropertyNames() {
        return getPropertyNames(PMFormElement.class, PM_FORM_PREFIX + "(?!prefix)([\\w]+)$");
    }
}