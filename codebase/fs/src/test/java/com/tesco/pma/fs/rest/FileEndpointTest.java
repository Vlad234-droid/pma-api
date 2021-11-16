package com.tesco.pma.fs.rest;

import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.exception.RegistrationException;
import com.tesco.pma.fs.domain.File;
import com.tesco.pma.fs.service.FileService;
import com.tesco.pma.rest.AbstractEndpointTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.UUID;

import static com.tesco.pma.fs.domain.FileStatus.ACTIVE;
import static com.tesco.pma.fs.domain.FileType.FORM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FileEndpoint.class, properties = {
        "tesco.application.security.enabled=false",
})
public class FileEndpointTest extends AbstractEndpointTest {

    private static final UUID FILE_UUID_1 = UUID.fromString("6d37262f-3a00-4706-a74b-6bf98be65765");

    private static final String RESOURCES_PATH = "/com/tesco/pma/fs/rest/";
    private static final String FILE_NAME = "test1.txt";
    private static final String CREATOR_ID = "test";
    private static final String PATH = "/home/dev";
    private static final String FILES_URL = "/files";

    private static final byte[] CONTENT = {72, 101, 108};
    private static final String TXT_FILE_CONTENT_TYPE = "application/vnd.oasis.opendocument.text";

    private static final Instant FILE_DATE = Instant.parse("2021-04-22T08:50:08Z");
    private static final String DESCRIPTION = "other file";
    private static final Instant CREATED_TIME = Instant.parse("2021-11-03T22:38:14Z");
    private static final String DOWNLOAD = "/download/";
    private static final int FILE_LENGTH = 23;

    @MockBean
    private FileService service;

    @Test
    void getByUuid() throws Exception {
        when(service.get(FILE_UUID_1, true)).thenReturn(buildFileData(FILE_UUID_1, 1));

        var result = performGet(status().isOk(), FILES_URL + "/" + FILE_UUID_1);

        assertResponseContent(result.getResponse(), "file_get_ok_response.json");
    }

    @Test
    void getByUuidUnsuccess() throws Exception {
        when(service.get(FILE_UUID_1, true)).thenThrow(NotFoundException.class);

        performGet(status().isNotFound(), FILES_URL + "/" + FILE_UUID_1);
    }

    @Test
    void downloadSuccess() throws Exception {
        when(service.get(FILE_UUID_1, true)).thenReturn(buildFileData(FILE_UUID_1, 1));

        var result = performGet(status().isOk(), MediaType.APPLICATION_OCTET_STREAM, FILES_URL + DOWNLOAD + FILE_UUID_1);

        assertThat(result.getResponse().getContentAsByteArray()).isNotSameAs(CONTENT);
    }

    @Test
    void downloadUnsuccess() throws Exception {
        when(service.get(FILE_UUID_1, true)).thenThrow(NotFoundException.class);

        performGet(status().isNotFound(), FILES_URL + DOWNLOAD + FILE_UUID_1);
    }

    @Test
    void uploadFilesSuccess() throws Exception {
        var multipartUploadMetadataMock = getUploadMetadataMultipartFile("test_metadata.json");
        var multipartFileMock = getMultipartFileToUpload(CONTENT);

        var dataFile = buildFileData(FILE_UUID_1, 1);

        when(this.service.upload(any(), any(), any())).thenReturn(dataFile);

        final var result = performMultipartWithMetadata(multipartUploadMetadataMock, multipartFileMock,
                status().isCreated(), FILES_URL);

        assertResponseContent(result.getResponse(), "files_upload_ok_response.json");
    }

    @Test
    void uploadFilesUnsuccessWithBadRequest() throws Exception {
        var multipartUploadMetadataMock = getUploadMetadataMultipartFile("test_metadata_for_2_files.json");
        var multipartFileMock = getMultipartFileToUpload(CONTENT);

        var dataFile = buildFileData(FILE_UUID_1, 1);

        when(this.service.upload(any(), any(), any())).thenReturn(dataFile);

        final var result = performMultipartWithMetadata(multipartUploadMetadataMock, multipartFileMock,
                status().isBadRequest(), FILES_URL);

        assertResponseContent(result.getResponse(), "files_upload_failed_response.json");
    }

    @Test
    void uploadFilesUnsuccessWithInternalServerError() throws Exception {
        var multipartUploadMetadataMock = getUploadMetadataMultipartFile("test_metadata.json");
        var multipartFileMock = getMultipartFileToUpload(CONTENT);

        when(this.service.upload(any(), any(), any())).thenThrow(RegistrationException.class);

        performMultipartWithMetadata(multipartUploadMetadataMock, multipartFileMock, status().isInternalServerError(), FILES_URL);
    }

    private MockMultipartFile getUploadMetadataMultipartFile(String fileName) throws IOException {
        return new MockMultipartFile("uploadMetadata", fileName, APPLICATION_JSON_VALUE, resourceToByteArray(fileName));
    }

    private MockMultipartFile getMultipartFileToUpload(byte[] content) {
        return new MockMultipartFile("files", FILE_NAME, TXT_FILE_CONTENT_TYPE, content);
    }

    private byte[] resourceToByteArray(final String resourceName) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(RESOURCES_PATH + resourceName)) {
            return IOUtils.toByteArray(is);
        }
    }

    private File buildFileData(UUID uuid, Integer version) {
        var fileData = new File();
        fileData.setUuid(uuid);
        fileData.setPath(PATH);
        fileData.setVersion(version);
        fileData.setType(FORM);
        fileData.setStatus(ACTIVE);
        fileData.setDescription(DESCRIPTION);
        fileData.setCreatedBy(CREATOR_ID);
        fileData.setCreatedTime(CREATED_TIME);
        fileData.setFileName(FILE_NAME);
        fileData.setFileDate(FILE_DATE);
        fileData.setFileLength(FILE_LENGTH);
        fileData.setFileContent(CONTENT);

        return fileData;
    }
}