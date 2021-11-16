package com.tesco.pma.colleague.profile.domain;

import com.tesco.pma.api.DictionaryItem;
import lombok.Getter;

/**
 * Status for request processing in its life cycle (file registration, mapping, parsing)
 */
@Getter
public enum ImportRequestStatus implements DictionaryItem {

    REGISTERED(1, "File processing request was registered to database and data stored to file storage"),
    IN_PROGRESS(2, "Parsing or mapping is in progress"),
    PROCESSED(3, "Parsing and mapping are completed and data stored to database"),
    FAILED(4, "Some errors occurred while parsing or mapping");

    private final Integer id;
    private final String description;

    ImportRequestStatus(Integer id, String description) {
        this.id = id;
        this.description = description;
    }

    @Override
    public String getCode() {
        return name();
    }
}