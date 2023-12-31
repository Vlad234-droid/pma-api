package com.tesco.pma.contact.api;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

/**
 * Contact API
 * /messaging
 * Description of a recipient for the message
 */

@Data
public class Recipient implements Serializable {

    private static final long serialVersionUID = -1913830987518689523L;

    public static final String COLLEAGUE_MESSAGE_TYPE = "colleague";
    private static final String UUID_FORMAT = "trn:tesco:uid:uuid:%s";

    private UUID uuid;
    private DestinationType destination;

    @JsonGetter("_type")
    public String getType() {
        return COLLEAGUE_MESSAGE_TYPE;
    }

    public String getDestination() {
        return destination.getValue();
    }

    public String getUuid() {
        return String.format(UUID_FORMAT, this.uuid);
    }
}
