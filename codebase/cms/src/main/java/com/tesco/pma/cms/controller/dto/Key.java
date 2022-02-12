package com.tesco.pma.cms.controller.dto;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class Key {

    private String type;
    private String countryCode;
    private String iam;
    private String role;
    private String content;

    @Override
    public String toString() {
        return createKey(type, countryCode, role, iam, content);
    }


    public static String createKey(String... elements) {
        var sb = new StringBuilder();
        var divisor = "";

        for (String el : elements) {

            if(el == null) {
                continue;
            }

            sb.append(divisor);
            divisor = "/";
            sb.append(el);
        }

        return StringUtils.trim(sb.toString());
    }
}
