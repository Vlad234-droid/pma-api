package com.tesco.pma.colleague.profile.rest.model.colleague.contact;

import lombok.Data;

@Data
public class ContactResponse {
    private String email;
    private String workPhoneNumber;
    private AddressListResponse addresses;
}
