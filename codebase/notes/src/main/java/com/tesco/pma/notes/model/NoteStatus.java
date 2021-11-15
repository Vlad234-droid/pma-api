package com.tesco.pma.notes.model;

import com.tesco.pma.api.DictionaryItem;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NoteStatus implements DictionaryItem<Integer> {

    CREATED(1, "Note created status"),
    ARCHIVED(2, "Note archived status");

    private final Integer id;
    private final String description;

    @Override
    public String getCode() {
        return name();
    }
}
