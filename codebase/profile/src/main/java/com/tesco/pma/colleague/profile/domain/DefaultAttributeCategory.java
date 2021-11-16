package com.tesco.pma.colleague.profile.domain;

import com.tesco.pma.api.DictionaryItem;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum DefaultAttributeCategory implements DictionaryItem<Integer> {

    NOTIFICATION(1, "Notifications");

    private final Integer id;
    private final String description;

    @Override
    public String getCode() {
        return name();
    }
}
