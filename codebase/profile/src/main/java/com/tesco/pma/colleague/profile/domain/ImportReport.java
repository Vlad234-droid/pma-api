package com.tesco.pma.colleague.profile.domain;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.Set;
import java.util.UUID;

@Builder
@Value
public class ImportReport {

    UUID requestUuid;

    @Singular("importColleague")
    Set<UUID> imported;

    @Singular("skipColleague")
    Set<ImportError> skipped;

    @Singular("warnColleague")
    Set<ImportError> warn;
}
