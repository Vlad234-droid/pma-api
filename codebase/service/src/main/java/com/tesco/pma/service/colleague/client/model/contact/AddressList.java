package com.tesco.pma.service.colleague.client.model.contact;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressList {
    List<String> lines;
    String countryCode;
    String postcode;
    String city;
}
