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
@DataSet({"com/tesco/pma/colleague/security/dao/accounts_init.xml"})
class AccountManagementDAOTest extends AbstractDAOTest {

    @Autowired
    private AccountManagementDAO instance;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }

    @Test
    void shouldGetAccounts() {

        RequestQuery requestQuery = new RequestQuery();
        requestQuery.setOffset(0);
        requestQuery.setLimit(10);

        final var result = instance.get(requestQuery);

        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(3);
    }

    @Test
    void createSucceeded() {
        final int inserted = instance.create("string 4", "string 4", AccountStatus.ENABLED, AccountType.USER);
        assertThat(inserted).isEqualTo(1);
    }

    @Test
    void createThrowDuplicatedAccountException() {

        assertThatCode(() -> instance.create("string 3", "string 3",
                AccountStatus.ENABLED, AccountType.USER))
                .isExactlyInstanceOf(DuplicateKeyException.class)
                .hasMessageContaining("string 3");
    }

    @Test
    void findAccountByNameSucceeded() {
        var account= instance.findAccountByName("string 1");

        assertThat(account.getId()).isEqualTo(UUID.fromString("a3d51c49-0ab3-448e-ae31-2c865e27c6ea"));
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ENABLED);
        assertThat(account.getType()).isEqualTo(AccountType.USER);
   }

    @Test
    void disableAccountSucceeded() {
        final int updated = instance.disableAccount("string 1",AccountStatus.DISABLED);

        assertThat(updated).isEqualTo(1);
    }

    @Test
    void enableAccountSucceeded() {
        final int updated = instance.enableAccount("string 1",AccountStatus.ENABLED);

        assertThat(updated).isEqualTo(1);
    }

    @Test
    void assignRoleSucceeded() {
        final int updated = instance.assignRole(UUID.fromString("3c9d0dcd-318b-4094-9743-98e8460aac7e"), 2);

        assertThat(updated).isEqualTo(1);
    }

    @Test
    void removeRoleSucceeded() {
        final int updated = instance.removeRole(UUID.fromString("3c9d0dcd-318b-4094-9743-98e8460aac7e"), 1);

        assertThat(updated).isEqualTo(1);
    }

}