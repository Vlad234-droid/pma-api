package com.tesco.pma.process.api;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;


@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TimelineResponse {
    String reviewUuid;
    String code;
    String description;
    String type;
    String status;
    Date startDate = new Date(ThreadLocalRandom.current().nextInt() * 1000L);
}
