package com.tesco.pma.colleague.security;

import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.colleague.security.domain.Account;
import com.tesco.pma.colleague.security.domain.AccountStatus;
import com.tesco.pma.colleague.security.domain.AccountType;
import com.tesco.pma.colleague.security.domain.Role;
import com.tesco.pma.colleague.security.domain.request.CreateAccountRequest;
import com.tesco.pma.colleague.security.domain.request.RoleRequest;
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
        account.setId(UUID.randomUUID());
        return account;
    }

    public static List<Account> buildAccounts(int size) {
        return IntStream.rangeClosed(1, size)
                .mapToObj(TestDataUtils::buildAccount)
                .collect(toList());
    }

    private static Account buildAccount(int index) {
        var account = buildAccount();
        account.setName("UKE1111" + index);
        if (index > 1) {
            account.setRoles(buildRoles(index));
        }
        return account;
    }

    public static CreateAccountRequest buildCreateAccountRequest(Object roleId) {
        var createAccountRequest = new CreateAccountRequest();
        createAccountRequest.setName("UKE11111");
        createAccountRequest.setIamId("UKE11111");
        createAccountRequest.setStatus(AccountStatus.ENABLED);
        createAccountRequest.setType(AccountType.USER);
        createAccountRequest.setRoleId(roleId);
        return createAccountRequest;
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

    public static RoleRequest buildRoleRequest(Object role) {
        var roleRequest = new RoleRequest();
        roleRequest.setAccountName("UKE11111");
        roleRequest.setRole(role);
        return roleRequest;
    }

}
