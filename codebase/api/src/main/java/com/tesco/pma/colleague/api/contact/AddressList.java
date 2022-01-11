package com.tesco.pma.colleague.api.contact;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressList implements Serializable {

    private static final long serialVersionUID = 1L;

    List<String> lines;
    String countryCode;
    String postcode;
    String city;
}
