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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
        assertEquals(14, roles.size());
    }

    @Test
    void findRoles() {
        final var roles = dao.findRoles();
        assertEquals(8, roles.size());
    }

    @Test
    void shouldGetAccounts() {

        RequestQuery requestQuery = new RequestQuery();
        requestQuery.setOffset(0);
        requestQuery.setLimit(10);

        final var result = dao.get(requestQuery);

        assertFalse(result.isEmpty());
        assertEquals(3, result.size());
    }

    @Test
    void createSucceeded() {
        final int inserted = dao.create("string 4", "string 4", AccountStatus.ENABLED, AccountType.USER);
        assertEquals(1, inserted);
    }

    @Test
    void createThrowDuplicatedAccountException() {
        try {
            dao.create("string 3", "string 3",
                    AccountStatus.ENABLED, AccountType.USER);
            fail();
        } catch (DuplicateKeyException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("string 3"));
        }
    }

    @Test
    void findAccountByNameSucceeded() {
        var account= dao.findAccountByName("string 1");

        assertEquals(UUID.fromString("a3d51c49-0ab3-448e-ae31-2c865e27c6ea"), account.getId());
        assertEquals(AccountStatus.ENABLED, account.getStatus());
        assertEquals(AccountType.USER, account.getType());
   }

    @Test
    void disableAccountSucceeded() {
        assertEquals(1, dao.disableAccount("string 1", AccountStatus.DISABLED));
    }

    @Test
    void enableAccountSucceeded() {
        assertEquals(1, dao.enableAccount("string 1", AccountStatus.ENABLED));
    }

    @Test
    void assignRoleSucceeded() {
        assertEquals(1, dao.assignRole(UUID.fromString("3c9d0dcd-318b-4094-9743-98e8460aac7e"), 2));
    }

    @Test
    void removeRoleSucceeded() {
        assertEquals(1, dao.removeRole(UUID.fromString("3c9d0dcd-318b-4094-9743-98e8460aac7e"), 1));
    }

    @Test
    void findAccountByIamIdSucceeded() {
        var account= dao.findAccountByIamId("string 2");

        assertEquals(UUID.fromString("d7b90699-521d-48cc-8d08-eaf240ffcb0d"), account.getId());
        assertEquals(AccountStatus.ENABLED, account.getStatus());
        assertEquals(AccountType.USER, account.getType());
    }

    @Test
    void findAccountByColleagueUuidShouldReturnAccount() {
        var account= dao.findAccountByColleagueUuid(UUID.fromString("10000000-0000-0000-0000-000000000001"));

        assertEquals(UUID.fromString("a3d51c49-0ab3-448e-ae31-2c865e27c6ea"), account.getId());
        assertEquals(AccountStatus.ENABLED, account.getStatus());
        assertEquals(AccountType.USER, account.getType());
        assertFalse(account.getRoles().isEmpty());
        assertEquals(3, account.getRoles().size());
    }

    @Test
    void findAccountByColleagueUuidShouldReturnNull() {
        assertNull(dao.findAccountByColleagueUuid(UUID.fromString("10000000-0000-0000-0000-000000000002")));
    }


}