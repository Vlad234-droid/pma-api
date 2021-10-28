package com.tesco.pma.colleague.security.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import com.tesco.pma.api.DictionaryItem;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum AccountStatus implements DictionaryItem<Integer> {

    ACTIVE(1, "Active"),
    INACTIVE(2, "Inactive");

    private final Integer id;
    private final String description;

    private static final Map<String, AccountStatus> LOOKUP = Arrays.stream(AccountStatus.values())
            .collect(Collectors.toMap(AccountStatus::getDescription, Function.identity()));

    AccountStatus(Integer id, String description) {
        this.id = id;
        this.description = description;
    }

    @Override
    public String getCode() {
        return name();
    }

    @JsonValue
    @Override
    public String getDescription() {
        return description;
    }

    public static Optional<AccountStatus> getAccountStatus(String description) {
        return Optional.ofNullable(LOOKUP.get(description));
    }

}
