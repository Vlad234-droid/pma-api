package com.tesco.pma.service.colleague.client.model;

import com.tesco.pma.service.colleague.client.model.contact.AddressList;
import lombok.Data;

@Data
public class Contact {
    private String email;
    private String workPhoneNumber;
    private AddressList addresses;
}
