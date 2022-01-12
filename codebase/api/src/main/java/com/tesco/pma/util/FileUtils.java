package com.tesco.pma.util;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public class FileUtils {

    private static final Pattern FORM_NAME_PATTERN = Pattern.compile("(([\\w\\-_/]+)\\.(form|json))$");

    /**
     * Return form name by key using pattern
     * @param key - form key
     * @return form name
     */
    public String getFormName(String key) {
        var matcher = FORM_NAME_PATTERN.matcher(key);
        return matcher.find() ? matcher.group(1) : null;
    }
}