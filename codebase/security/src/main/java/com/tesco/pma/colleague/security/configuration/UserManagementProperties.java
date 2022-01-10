package com.tesco.pma.colleague.security.configuration;

import lombok.Data;

@Data
public class UserManagementProperties {

    private Page page;
    private String subject;

    @Data
    public static class Page {
        private int size;
    }

}
