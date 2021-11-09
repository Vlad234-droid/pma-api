package com.tesco.pma.colleague.profile.domain;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.Set;
import java.util.UUID;

@Builder
@Value
public class ImportReport {

    @Singular("importColleague")
    Set<UUID> imported;

    @Singular("skipColleague")
    Set<UUID> skipped;

    @Singular("usersManagerSkip")
    Set<UUID> managerSkippedForUser;
}
