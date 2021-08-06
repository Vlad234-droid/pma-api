package com.tesco.pma.security;

import com.tesco.pma.api.User;
import com.tesco.pma.api.security.SubsidiaryPermission;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserDetailsTest {
    @Test
    void hasSubsidiaryPermission() {
        final var colleagueUuid = UUID.randomUUID();
        final var subsidiaryUuid1 = UUID.randomUUID();
        final var subsidiaryUuid2 = UUID.randomUUID();
        final var role1 = "target-role";
        final var role2 = "dummy-role";
        final var user = new User(colleagueUuid);
        user.getSubsidiaryPermissions().add(SubsidiaryPermission.of(colleagueUuid, subsidiaryUuid1, role1));
        user.getSubsidiaryPermissions().add(SubsidiaryPermission.of(colleagueUuid, subsidiaryUuid2, role2));
        UserDetails userDetails = new UserDetails(user);

        assertThat(userDetails.hasSubsidiaryPermission(subsidiaryUuid1, role1)).isTrue();
        assertThat(userDetails.hasSubsidiaryPermission(subsidiaryUuid1, role2)).isFalse();
        assertThat(userDetails.hasSubsidiaryPermission(subsidiaryUuid2, role1)).isFalse();
        assertThat(userDetails.hasSubsidiaryPermission(subsidiaryUuid2, role2)).isTrue();
    }
}