package com.tesco.pma.colleague.api;

import com.tesco.pma.colleague.api.contact.AddressList;
import lombok.Data;

import java.io.Serializable;

@Data
public class Contact implements Serializable {

    private static final long serialVersionUID = 3803787587233119188L;

    private String email;
    private String workPhoneNumber;
    private AddressList addresses;
}
