package com.tesco.pma.fs.rest;

import com.tesco.pma.fs.domain.File;
import com.tesco.pma.fs.service.FileService;
import com.tesco.pma.rest.AbstractEndpointTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Instant;
import java.util.UUID;

import static com.tesco.pma.fs.domain.FileStatus.ACTIVE;
import static com.tesco.pma.fs.domain.FileType.FORM;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FileEndpoint.class, properties = {
        "tesco.application.security.enabled=false",
})
public class FileEndpointTest extends AbstractEndpointTest {

    private static final UUID FILE_UUID_1 = UUID.fromString("6d37262f-3a00-4706-a74b-6bf98be65765");

    private static final String FILE_NAME = "test1.txt";
    private static final String CREATOR_ID = "test";
    private static final String PATH = "/home/dev";
    private static final String FILES_URL = "/files";

    @MockBean
    private FileService service;

    @Test
    void findByUuid() throws Exception {
        when(service.findByUuid(FILE_UUID_1, true)).thenReturn(buildFileData(FILE_UUID_1, 1));

        var result = performGet(status().isOk(), FILES_URL + "/" + FILE_UUID_1);

        assertResponseContent(result.getResponse(), "file_get_ok_response.json");
    }

    private File buildFileData(UUID uuid, Integer version) {
        final var file = new File();
        file.setUuid(uuid);
        file.setPath(PATH);
        file.setVersion(version);
        file.setType(FORM);
        file.setStatus(ACTIVE);
        file.setDescription("other file");
        file.setCreatedBy(CREATOR_ID);
        file.setCreatedTime(Instant.parse("2021-11-03T22:38:14Z"));
        file.setFileName(FILE_NAME);
        file.setFileDate(Instant.parse("2021-04-22T08:50:08Z"));
        file.setFileLength(3);
        file.setFileContent(new byte[] { 72, 101, 108});

        return file;
    }
}