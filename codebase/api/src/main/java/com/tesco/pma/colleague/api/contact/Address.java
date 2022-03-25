package com.tesco.pma.colleague.api.contact;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Address implements Serializable {

    private static final long serialVersionUID = -7976077387623366848L;

    private List<String> lines;
    private String countryCode;
    private String postcode;
    private String city;
}
