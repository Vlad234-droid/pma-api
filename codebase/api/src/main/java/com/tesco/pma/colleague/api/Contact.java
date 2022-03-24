package com.tesco.pma.colleague.api;

import com.tesco.pma.colleague.api.contact.Address;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Contact implements Serializable {

    private static final long serialVersionUID = 3803787587233119188L;

    private String email;
    private String workPhoneNumber;
    private List<Address> addresses;

    @Override
    public String toString() {
        return "Contact{" +
                "email='*****'" +
                ", workPhoneNumber='" + workPhoneNumber + '\'' +
                ", addresses=" + addresses +
                '}';
    }
}
