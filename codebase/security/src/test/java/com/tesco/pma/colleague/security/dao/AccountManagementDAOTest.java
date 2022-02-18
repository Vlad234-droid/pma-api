package com.tesco.pma.colleague.security.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.colleague.security.domain.AccountStatus;
import com.tesco.pma.colleague.security.domain.AccountType;
import com.tesco.pma.dao.AbstractDAOTest;
import com.tesco.pma.pagination.RequestQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

// TODO Implement all tests
@DataSet({"com/tesco/pma/colleague/security/dao/accounts_init.xml",
        "com/tesco/pma/colleague/security/dao/colleagues_init.xml"})
class AccountManagementDAOTest extends AbstractDAOTest {

    @Autowired
    private AccountManagementDAO dao;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }

    @Test
    void findAllRoles() {
        final var roles = dao.findAllRoles();
        assertThat(roles.size()).isEqualTo(14);
    }

    @Test
    void findRoles() {
        final var roles = dao.findRoles();
        assertThat(roles.size()).isEqualTo(8);
    }

    @Test
    void shouldGetAccounts() {

        RequestQuery requestQuery = new RequestQuery();
        requestQuery.setOffset(0);
        requestQuery.setLimit(10);

        final var result = dao.get(requestQuery);

        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(3);
    }

    @Test
    void createSucceeded() {
        final int inserted = dao.create("string 4", "string 4", AccountStatus.ENABLED, AccountType.USER);
        assertThat(inserted).isEqualTo(1);
    }

    @Test
    void createThrowDuplicatedAccountException() {

        assertThatCode(() -> dao.create("string 3", "string 3",
                AccountStatus.ENABLED, AccountType.USER))
                .isExactlyInstanceOf(DuplicateKeyException.class)
                .hasMessageContaining("string 3");
    }

    @Test
    void findAccountByNameSucceeded() {
        var account= dao.findAccountByName("string 1");

        assertThat(account.getId()).isEqualTo(UUID.fromString("a3d51c49-0ab3-448e-ae31-2c865e27c6ea"));
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ENABLED);
        assertThat(account.getType()).isEqualTo(AccountType.USER);
   }

    @Test
    void disableAccountSucceeded() {
        final int updated = dao.disableAccount("string 1", AccountStatus.DISABLED);

        assertThat(updated).isEqualTo(1);
    }

    @Test
    void enableAccountSucceeded() {
        final int updated = dao.enableAccount("string 1", AccountStatus.ENABLED);

        assertThat(updated).isEqualTo(1);
    }

    @Test
    void assignRoleSucceeded() {
        final int updated = dao.assignRole(UUID.fromString("3c9d0dcd-318b-4094-9743-98e8460aac7e"), 2);

        assertThat(updated).isEqualTo(1);
    }

    @Test
    void removeRoleSucceeded() {
        final int updated = dao.removeRole(UUID.fromString("3c9d0dcd-318b-4094-9743-98e8460aac7e"), 1);

        assertThat(updated).isEqualTo(1);
    }

    @Test
    void findAccountByIamIdSucceeded() {
        var account= dao.findAccountByIamId("string 2");

        assertThat(account.getId()).isEqualTo(UUID.fromString("d7b90699-521d-48cc-8d08-eaf240ffcb0d"));
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ENABLED);
        assertThat(account.getType()).isEqualTo(AccountType.USER);
    }

    @Test
    void findAccountByColleagueUuidShouldReturnAccount() {
        var account= dao.findAccountByColleagueUuid(UUID.fromString("10000000-0000-0000-0000-000000000001"));

        assertThat(account.getId()).isEqualTo(UUID.fromString("a3d51c49-0ab3-448e-ae31-2c865e27c6ea"));
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ENABLED);
        assertThat(account.getType()).isEqualTo(AccountType.USER);
        assertThat(account.getRoles()).isNotEmpty();
        assertThat(account.getRoles().size()).isEqualTo(3);
    }

    @Test
    void findAccountByColleagueUuidShouldReturnNull() {
        var account= dao.findAccountByColleagueUuid(UUID.fromString("10000000-0000-0000-0000-000000000002"));

        assertThat(account).isNull();
    }


}