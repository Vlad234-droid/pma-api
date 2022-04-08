package com.tesco.pma.cep.cfapi.v2.service;

import com.tesco.pma.cep.cfapi.v2.domain.ColleagueChangeEventPayload;
import com.tesco.pma.cep.cfapi.v2.domain.EventType;
import com.tesco.pma.colleague.api.Colleague;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.colleague.security.domain.Account;
import com.tesco.pma.colleague.security.domain.AccountStatus;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@UtilityClass
public class TestDataUtils {

    public static final UUID COLLEAGUE_UUID = UUID.fromString("10000000-0000-0000-0000-000000000001");

    public static ColleagueChangeEventPayload buildColleagueChangeEventPayload(EventType eventType) {
        var colleagueChangeEventPayload = new ColleagueChangeEventPayload();
        colleagueChangeEventPayload.setColleagueUuid(COLLEAGUE_UUID);
        colleagueChangeEventPayload.setEventType(eventType);

        if (EventType.MODIFICATION.equals(eventType)) {
            colleagueChangeEventPayload.setChangedAttributes(List.of("attribute1", "attribute2"));
        }

        colleagueChangeEventPayload.setCurrent(buildColleague());

        return colleagueChangeEventPayload;
    }

    public static Colleague buildColleague() {
        Colleague colleague = new Colleague();
        colleague.setColleagueUUID(COLLEAGUE_UUID);
        return colleague;
    }

    public static Optional<ColleagueProfile> buildColleagueProfile(UUID colleagueUuid) {
        var colleagueProfile = new ColleagueProfile();

        var colleague = new Colleague();
        colleague.setColleagueUUID(colleagueUuid);
        colleagueProfile.setColleague(colleague);
        colleagueProfile.setProfileAttributes(List.of());

        return Optional.of(colleagueProfile);
    }

    public static Account buildAccount(UUID colleagueUuid) {
        var account = new Account();
        account.setId(colleagueUuid);
        account.setStatus(AccountStatus.ENABLED);
        return account;
    }

    public static String buildRandomUUID() {
        return UUID.randomUUID().toString();
    }

}
