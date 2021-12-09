package com.tesco.pma.contact.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DestinationType {
    EMAIL_TO("emailTo"), EMAIL_CC("emailCc"), EMAIL_BCC("emailBcc");

    private final String value;
}
