package com.tesco.pma.colleague.api.contact;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Address implements Serializable {

    private static final long serialVersionUID = -7976077387623366848L;

    List<String> lines;
    String countryCode;
    String postcode;
    String city;
}
