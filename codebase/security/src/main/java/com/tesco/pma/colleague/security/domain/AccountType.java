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
public enum AccountType implements DictionaryItem<Integer> {

    USER(1, "User");

    private final Integer id;
    private final String description;

    private static final Map<String, AccountType> LOOKUP = Arrays.stream(AccountType.values())
            .collect(Collectors.toMap(AccountType::getDescription, Function.identity()));

    AccountType(Integer id, String description) {
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

    public static Optional<AccountType> getAccountType(String description) {
        return Optional.ofNullable(LOOKUP.get(description));
    }

}
