package com.tesco.pma.profile;

import com.tesco.pma.profile.domain.AttributeType;
import com.tesco.pma.profile.domain.ProfileAttribute;
import com.tesco.pma.service.colleague.client.model.Colleague;
import org.jeasy.random.EasyRandom;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AbstractTests {

    protected static final EasyRandom RANDOM = new EasyRandom();

    protected static final String COLLEAGUE_UUID_1_STRING = "6d37262f-3a00-4706-a74b-6bf98be65765";

    protected static final UUID COLLEAGUE_UUID_1 = UUID.fromString(COLLEAGUE_UUID_1_STRING);

    private static final String[] names = {"emergencyContact", "emergencyPhone", "businessUnitBonus"};

    protected List<ProfileAttribute> profileAttributes(int size) {
        return IntStream.rangeClosed(1, size)
                .mapToObj(this::profileAttribute)
                .collect(Collectors.toList());
    }

    protected ProfileAttribute profileAttribute(int index) {
        ProfileAttribute profileAttribute = new ProfileAttribute();
        profileAttribute.setColleagueUuid(COLLEAGUE_UUID_1);
        profileAttribute.setName(getName(index));
        profileAttribute.setTitle("title" + index);
        profileAttribute.setValue("value" + index);
        profileAttribute.setType(AttributeType.STRING);
        return profileAttribute;
    }

    private String getName(int index) {
        if (index > names.length) {
            throw new IllegalArgumentException();
        }
        return names[index - 1];
    }

    protected Colleague randomColleague() {
        return RANDOM.nextObject(Colleague.class);
    }

}
