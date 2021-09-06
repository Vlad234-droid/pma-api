package com.tesco.pma.colleague.profile.service.rest.model.colleague.contact;

import lombok.Data;

@Data
public class Contact {
    private String email;
    private String workPhoneNumber;
    private AddressList addresses;
}
