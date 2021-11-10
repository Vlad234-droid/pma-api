package com.tesco.pma.fs.rest;

import com.tesco.pma.fs.domain.File;
import com.tesco.pma.fs.service.FileService;
import com.tesco.pma.rest.AbstractEndpointTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;

import java.time.Instant;
import java.util.UUID;

import static com.tesco.pma.fs.domain.FileStatus.ACTIVE;
import static com.tesco.pma.fs.domain.FileType.FORM;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
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

    private static final byte[] CONTENT = "Spring Boot Framework 2".getBytes();
    private static final String TXT_FILE_CONTENT_TYPE = "application/vnd.oasis.opendocument.text";

    private static final byte[] UPLOAD_FILE_METADATA_CONTENT = ("{\n" +
            "  \"uploadMetadataList\": [\n" +
            "    {\n" +
            "      \"path\": \"/home/dev\",\n" +
            "      \"type\": \"FORM\",\n" +
            "      \"status\": \"ACTIVE\",\n" +
            "      \"description\": \"other file\",\n" +
            "      \"fileDate\": \"2021-04-22T08:50:08Z\"\n" +
            "    }\n" +
            "  ]\n" +
            "}").getBytes();

    private static final Instant FILE_DATE = Instant.parse("2021-04-22T08:50:08Z");
    private static final String DESCRIPTION = "other file";
    private static final Instant CREATED_TIME = Instant.parse("2021-11-03T22:38:14Z");

    @MockBean
    private FileService service;

    @Test
    void findByUuid() throws Exception {
        when(service.findByUuid(FILE_UUID_1, true)).thenReturn(buildFileData(FILE_UUID_1, 1));

        var result = performGet(status().isOk(), FILES_URL + "/" + FILE_UUID_1);

        assertResponseContent(result.getResponse(), "file_get_ok_response.json");
    }

    @Test
    void uploadFilesSuccess() throws Exception {
        var multipartUploadMetadataMock = getUploadMetadataMultipartFile();
        var multipartFileMock = getMultipartFileToUpload(CONTENT);

        var dataFile = buildFileData(FILE_UUID_1, 1);

        when(this.service.upload(any(), any(), any(), any())).thenReturn(dataFile);

        final var result = performMultipartWithMetadata(multipartUploadMetadataMock, multipartFileMock,
                status().isCreated(), FILES_URL);

        assertResponseContent(result.getResponse(), "files_upload_ok_response.json");
    }

    private MockMultipartFile getUploadMetadataMultipartFile() {
        return new MockMultipartFile("uploadMetadata", "test_metadata.json",
                APPLICATION_JSON_VALUE, UPLOAD_FILE_METADATA_CONTENT);
    }

    private MockMultipartFile getMultipartFileToUpload(byte[] content) {
        return new MockMultipartFile("files", FILE_NAME, TXT_FILE_CONTENT_TYPE, content);
    }

    private File buildFileData(UUID uuid, Integer version) {
        var file = new File();
        file.setUuid(uuid);
        file.setPath(PATH);
        file.setVersion(version);
        file.setType(FORM);
        file.setStatus(ACTIVE);
        file.setDescription(DESCRIPTION);
        file.setCreatedBy(CREATOR_ID);
        file.setCreatedTime(CREATED_TIME);
        file.setFileName(FILE_NAME);
        file.setFileDate(FILE_DATE);
        file.setFileLength(23);
        file.setFileContent(new byte[] { 72, 101, 108});

        return file;
    }
}