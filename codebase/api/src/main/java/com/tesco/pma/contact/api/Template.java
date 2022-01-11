package com.tesco.pma.contact.api;

import lombok.Data;

import java.io.Serializable;

/**
 * Contact API
 * /messaging
 * Description of the template for the message
 */

@Data
public class Template implements Serializable {

    private String version;
    private String language;

}
