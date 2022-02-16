package com.tesco.pma.config.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.config.domain.ImportError;
import com.tesco.pma.config.domain.ImportRequest;
import com.tesco.pma.config.domain.ImportRequestStatus;
import com.tesco.pma.dao.AbstractDAOTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImportColleagueDAOTest extends AbstractDAOTest {

    private static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/config/dao/";
    public static final UUID REQUEST_UUID = UUID.fromString("c409869b-2acf-45cd-8cc6-e13af2e6f935");
    public static final UUID REQUEST_UUID_2 = UUID.fromString("a019869b-2acf-45cd-8cc6-e13af2e6a218");

    @Autowired
    private ImportColleaguesDAO dao;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }


    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "import-request.xml"})
    void registerRequest() {
        var ir = new ImportRequest();
        ir.setUuid(REQUEST_UUID);
        ir.setStatus(ImportRequestStatus.REGISTERED);
        ir.setFileName("FN");

        dao.registerRequest(ir);

        var request = dao.getRequest(REQUEST_UUID);
        assertEquals(REQUEST_UUID, request.getUuid());
        assertEquals(ImportRequestStatus.REGISTERED, request.getStatus());
        assertEquals("FN", request.getFileName());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "import-request.xml"})
    void updateRequest() {

        var request = dao.getRequest(REQUEST_UUID_2);
        assertEquals(ImportRequestStatus.REGISTERED, request.getStatus());

        request.setStatus(ImportRequestStatus.FAILED);

        dao.updateRequest(request);

        var updated = dao.getRequest(REQUEST_UUID_2);
        assertEquals(ImportRequestStatus.FAILED, updated.getStatus());
    }

    @Test
    @DataSet({BASE_PATH_TO_DATA_SET + "import-request.xml"})
    void saveAndGetImportErrors() {
        var ie = new ImportError();
        var colleagueUuid = UUID.randomUUID();
        var message = "message";

        ie.setRequestUuid(REQUEST_UUID_2);
        ie.setColleagueUuid(colleagueUuid);
        ie.setCode("code");
        ie.setMessage(message);

        dao.saveError(ie);

        var requestErrors = dao.getRequestErrors(REQUEST_UUID_2);

        assertEquals(3, requestErrors.size());
        assertError(requestErrors.get(2), "code", message, colleagueUuid);
        assertError(requestErrors.get(0), "ERROR", message, null);
    }

    private void assertError(ImportError importError, String code, String message, UUID colleagueUuid) {
        assertEquals(code, importError.getCode());
        assertEquals(message, importError.getMessage());
        assertEquals(REQUEST_UUID_2, importError.getRequestUuid());
        if (colleagueUuid != null) {
            assertEquals(colleagueUuid, importError.getColleagueUuid());
        }
    }


}
