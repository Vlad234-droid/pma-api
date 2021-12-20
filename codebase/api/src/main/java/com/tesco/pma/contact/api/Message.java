package com.tesco.pma.contact.api;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Contact API
 * /messaging
 * Message for the given template id
 */

@Data
public class Message {

    private List<Recipient> recipients;
    private Template template;

    /**
     * Placeholders for the template
     * Format: {
     *     placeholderName: "value"
     * }
     */
    private Map<String, String> data;
}
