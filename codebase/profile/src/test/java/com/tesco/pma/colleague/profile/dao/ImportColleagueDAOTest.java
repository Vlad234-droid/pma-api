package com.tesco.pma.colleague.profile.dao;

import com.github.database.rider.core.api.dataset.DataSet;
import com.tesco.pma.colleague.profile.domain.ImportError;
import com.tesco.pma.colleague.profile.domain.ImportRequest;
import com.tesco.pma.colleague.profile.domain.ImportRequestStatus;
import com.tesco.pma.dao.AbstractDAOTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImportColleagueDAOTest extends AbstractDAOTest {

    private static final String BASE_PATH_TO_DATA_SET = "com/tesco/pma/colleague/profile/dao/";
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
        ie.setRequestUuid(REQUEST_UUID_2);
        ie.setColleagueUuid(colleagueUuid);
        ie.setCode("code");
        ie.setMessage("message");

        dao.saveError(ie);

        var requestErrors = dao.getRequestErrors(REQUEST_UUID_2);

        Assertions.assertThat(requestErrors)
                .hasSize(3)
                .element(2)
                .returns("code", ImportError::getCode)
                .returns("message", ImportError::getMessage)
                .returns(REQUEST_UUID_2, ImportError::getRequestUuid)
                .returns(colleagueUuid, ImportError::getColleagueUuid);

        Assertions.assertThat(requestErrors)
                .element(1)
                .returns("ERROR", ImportError::getCode)
                .returns("message", ImportError::getMessage)
                .returns(REQUEST_UUID_2, ImportError::getRequestUuid);
    }


}
