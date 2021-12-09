package com.tesco.pma.contact.api;

import lombok.Data;

import java.util.List;

@Data
public class Message {

    private List<Recipient> recipients;
    private Template template;
}
