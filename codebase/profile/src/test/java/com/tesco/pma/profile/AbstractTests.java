package com.tesco.pma.profile;

import com.tesco.pma.profile.domain.AttributeType;
import com.tesco.pma.profile.domain.ProfileAttribute;
import com.tesco.pma.service.colleague.client.model.Colleague;
import org.jeasy.random.EasyRandom;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class AbstractTests {

    protected static final EasyRandom RANDOM = new EasyRandom();

    protected List<ProfileAttribute> profileAttributes(long size) {
        return LongStream.rangeClosed(1, size)
                .mapToObj(this::profileAttribute)
                .collect(Collectors.toList());
    }

    protected ProfileAttribute profileAttribute(long id) {
        ProfileAttribute profileAttribute = new ProfileAttribute();
        profileAttribute.setColleagueUuid(UUID.randomUUID());
        profileAttribute.setName("name" + id);
        profileAttribute.setValue("value" + id);
        profileAttribute.setType(AttributeType.STRING);
        return profileAttribute;
    }

    protected Colleague randomColleague() {
        return RANDOM.nextObject(Colleague.class);
    }

}
