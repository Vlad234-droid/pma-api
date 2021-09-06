package com.tesco.pma.colleague.profile.service.rest.model.colleague.contact;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddressListResponse {
    List<String> lines;
    String countryCode;
    String postcode;
    String city;
}
