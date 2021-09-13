package com.tesco.pma.colleague.api;

import com.tesco.pma.colleague.api.contact.AddressList;
import lombok.Data;

@Data
public class Contact {
    private String email;
    private String workPhoneNumber;
    private AddressList addresses;
}
