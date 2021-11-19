package com.tesco.pma.colleague.profile;

import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.profile.domain.AttributeType;
import com.tesco.pma.colleague.profile.domain.TypedAttribute;
import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import org.jeasy.random.EasyRandom;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class AbstractProfileTests {

    protected static final EasyRandom RANDOM = new EasyRandom();

    protected static final String COLLEAGUE_UUID_1_STRING = "6d37262f-3a00-4706-a74b-6bf98be65765";

    protected static final UUID COLLEAGUE_UUID_1 = UUID.fromString(COLLEAGUE_UUID_1_STRING);

    private static final String[] NAMES = {"emergencyContact", "emergencyPhone", "businessUnitBonus"};

    protected List<TypedAttribute> profileAttributes(int size) {
        return IntStream.rangeClosed(1, size)
                .mapToObj(this::profileAttribute)
                .collect(Collectors.toList());
    }

    protected TypedAttribute profileAttribute(int index) {
        TypedAttribute profileAttribute = new TypedAttribute();
        profileAttribute.setColleagueUuid(COLLEAGUE_UUID_1);
        profileAttribute.setName(getName(index));
        profileAttribute.setValue("value" + index);
        profileAttribute.setType(AttributeType.STRING);
        return profileAttribute;
    }

    private String getName(int index) {
        if (index > NAMES.length) {
            throw new IllegalArgumentException();
        }
        return NAMES[index - 1];
    }

    protected ColleagueEntity randomColleagueEntity() {
        return RANDOM.nextObject(ColleagueEntity.class);
    }

    protected Colleague randomColleague() {
        return RANDOM.nextObject(Colleague.class);
    }

}
