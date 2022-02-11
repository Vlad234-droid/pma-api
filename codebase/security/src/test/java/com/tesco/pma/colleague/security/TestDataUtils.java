package com.tesco.pma.colleague.security;

import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.colleague.security.domain.Account;
import com.tesco.pma.colleague.security.domain.AccountStatus;
import com.tesco.pma.colleague.security.domain.Role;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@UtilityClass
public class TestDataUtils {

    public static Account buildAccount() {
        var account = new Account();
        account.setStatus(AccountStatus.ENABLED);
        return account;
    }

    public static ColleagueEntity buildColleagueEntity(UUID uuid) {
        var colleagueEntity = new ColleagueEntity();
        colleagueEntity.setUuid(uuid);
        return colleagueEntity;
    }

    public static ColleagueEntity.WorkLevel buildWorkLevel(String workLevelCode) {
        var workLevel = new ColleagueEntity.WorkLevel();
        workLevel.setCode(workLevelCode);
        return workLevel;
    }

    public static Collection<Role> buildRoles(String... codes) {
        return Stream.of(codes)
                .map(code -> {
                    var role = new Role();
                    role.setCode(code);
                    return role;
                })
                .sorted(Comparator.comparing(Role::getCode))
                .collect(toList());
    }

    public static List<Role> buildRoles(int size) {
        return IntStream.rangeClosed(1, size)
                .mapToObj(TestDataUtils::buildRole)
                .collect(toList());
    }

    private static Role buildRole(int index) {
        Role role = new Role();
        role.setId(index);
        role.setCode("code" + index);
        role.setDescription("description" + index);
        return role;
    }

}
