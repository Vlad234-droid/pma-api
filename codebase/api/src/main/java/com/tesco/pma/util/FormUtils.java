package com.tesco.pma.util;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public class FormUtils {

    private static final Pattern FORM_NAME_PATTERN = Pattern.compile("(([\\w\\-_/]+)\\.(form|json))$");

    public String getFormName(String key) {
        var matcher = FORM_NAME_PATTERN.matcher(key);
        return matcher.find() ? matcher.group(1) : null;
    }
}