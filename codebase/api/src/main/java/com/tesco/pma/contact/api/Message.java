package com.tesco.pma.contact.api;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Message {

    private List<Recipient> recipients;
    private Template template;
    private Map<String, String> data;
}
