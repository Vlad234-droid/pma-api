package com.tesco.pma.fs.rest;

import com.tesco.pma.TestConfig;
import com.tesco.pma.configuration.audit.AuditorAware;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.exception.RegistrationException;
import com.tesco.pma.file.api.FileType;
import com.tesco.pma.file.api.File;
import com.tesco.pma.fs.service.FileService;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.rest.AbstractEndpointTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.tesco.pma.file.api.FileStatus.ACTIVE;
import static com.tesco.pma.security.UserRoleNames.ADMIN;
import static com.tesco.pma.security.UserRoleNames.COLLEAGUE;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FileEndpoint.class)
@ContextConfiguration(classes = TestConfig.class)
class FileEndpointTest extends AbstractEndpointTest {

    static final String COLLEAGUE_UUID_STR = "6d37262f-3a00-4706-a74b-6bf98be65765";

    private static final UUID FILE_UUID_1 = UUID.fromString("6d37262f-3a00-4706-a74b-6bf98be65765");

    private static final String RESOURCES_PATH = "/com/tesco/pma/fs/rest/";
    private static final String FILE_NAME = "test1.txt";
    private static final UUID CREATOR_ID = UUID.fromString(COLLEAGUE_UUID_STR);
    private static final String PATH = "/home/dev";
    private static final Integer VERSION_1 = 1;
    private static final Integer VERSION_2 = 2;

    private static final String FILES_URL = "/files";
    private static final String VERSIONS_URL = "/versions";
    private static final String PATH_AND_NAME_PARAMS_URL = "?path=" + PATH + "&fileName=" + FILE_NAME;
    private static final String VERSIONS_PARAMS_URL = "&versions=" + VERSION_1 + "&versions=" + VERSION_2;
    private static final String DELETE_BY_VERSIONS_URL = FILES_URL + VERSIONS_URL + PATH_AND_NAME_PARAMS_URL + VERSIONS_PARAMS_URL;

    private static final byte[] CONTENT = {72, 101, 108};
    private static final String TXT_FILE_CONTENT_TYPE = "application/vnd.oasis.opendocument.text";

    private static final Instant FILE_DATE = Instant.parse("2021-04-22T08:50:08Z");
    private static final String DESCRIPTION = "other file";
    private static final Instant CREATED_TIME = Instant.parse("2021-11-03T22:38:14Z");
    private static final String DOWNLOAD = "/{fileUuid}/download";
    private static final int FILE_LENGTH = 23;

    private static final String FILES_GET_OK_RESPONSE_JSON_FILE_NAME = "files_get_ok_response.json";

    @MockBean
    private FileService service;

    @MockBean
    private AuditorAware<UUID> auditorAware;

    @Test
    void getByUuid() throws Exception {
        when(service.get(FILE_UUID_1, false, CREATOR_ID)).thenReturn(buildFileData(FILE_UUID_1, 1));

        var result = performGetWith(colleague(COLLEAGUE_UUID_STR),
                status().isOk(), FILES_URL + "/" + FILE_UUID_1);

        assertResponseContent(result.getResponse(), "file_get_ok_response.json");
    }

    @Test
    void getByUuidWithAdmin() throws Exception {
        when(service.get(FILE_UUID_1, false, null)).thenReturn(buildFileData(FILE_UUID_1, 1));

        var result = performGetWith(roles(List.of(COLLEAGUE, ADMIN)),
                status().isOk(), FILES_URL + "/" + FILE_UUID_1);

        assertResponseContent(result.getResponse(), "file_get_ok_response.json");
    }

    @Test
    void getByUuidUnsuccess() throws Exception { //NOSONAR used MockMvc checks
        when(service.get(FILE_UUID_1, false, CREATOR_ID)).thenThrow(NotFoundException.class);

        performGetWith(colleague(COLLEAGUE_UUID_STR), status().isNotFound(), FILES_URL + "/" + FILE_UUID_1);
    }

    @Test
    void getByRequestQuery() throws Exception {
        when(service.get(any(RequestQuery.class), eq(false), eq(CREATOR_ID), eq(true))).thenReturn(List.of(buildFileData(FILE_UUID_1, 1)));

        var result = performGetWith(colleague(COLLEAGUE_UUID_STR),
                status().isOk(), FILES_URL + "?status_in[0]=ACTIVE&file-length_gt=16");

        assertResponseContent(result.getResponse(), FILES_GET_OK_RESPONSE_JSON_FILE_NAME);
    }

    @Test
    void getByRequestQueryWithAdmin() throws Exception {
        when(service.get(any(RequestQuery.class), eq(false), eq(null), eq(true))).thenReturn(List.of(buildFileData(FILE_UUID_1, 1)));

        var result = performGetWith(roles(List.of(COLLEAGUE, ADMIN)),
                status().isOk(), FILES_URL + "?status_in[0]=ACTIVE&file-length_gt=16");

        assertResponseContent(result.getResponse(), FILES_GET_OK_RESPONSE_JSON_FILE_NAME);
    }

    @Test
    void getByRequestQueryHasEmptyDataResponseWhenServiceReturnsNothing() throws Exception {
        when(service.get(any(RequestQuery.class), eq(false), eq(null), eq(true))).thenReturn(emptyList());

        var result = performGetWith(colleague(COLLEAGUE_UUID_STR), status().isOk(), FILES_URL);

        assertResponseContent(result.getResponse(), "file_get_empty_data_response.json");
    }

    @Test
    void getAllVersions() throws Exception {
        when(service.getAllVersions(PATH, FILE_NAME, false, CREATOR_ID)).thenReturn(List.of(buildFileData(FILE_UUID_1, 1)));

        var result = performGetWith(colleague(COLLEAGUE_UUID_STR), status().isOk(),
                FILES_URL + VERSIONS_URL + PATH_AND_NAME_PARAMS_URL);

        assertResponseContent(result.getResponse(), FILES_GET_OK_RESPONSE_JSON_FILE_NAME);
    }

    @Test
    void getAllVersionsWithAdmin() throws Exception {
        when(service.getAllVersions(PATH, FILE_NAME, false, null)).thenReturn(List.of(buildFileData(FILE_UUID_1, 1)));

        var result = performGetWith(roles(List.of(COLLEAGUE, ADMIN)), status().isOk(),
                FILES_URL + VERSIONS_URL + PATH_AND_NAME_PARAMS_URL);

        assertResponseContent(result.getResponse(), FILES_GET_OK_RESPONSE_JSON_FILE_NAME);
    }

    @Test
    void getAllVersionsHasEmptyDataResponseWhenServiceReturnsNothing() throws Exception {
        when(service.getAllVersions(PATH, FILE_NAME, false, null)).thenReturn(emptyList());

        var result = performGetWith(colleague(COLLEAGUE_UUID_STR), status().isOk(),
                FILES_URL + VERSIONS_URL + PATH_AND_NAME_PARAMS_URL);

        assertResponseContent(result.getResponse(), "file_get_empty_data_response.json");
    }

    @Test
    void downloadSuccess() throws Exception {
        when(service.get(FILE_UUID_1, true, CREATOR_ID)).thenReturn(buildFileData(FILE_UUID_1, 1));

        var result = performGetWith(colleague(COLLEAGUE_UUID_STR), status().isOk(),
                MediaType.APPLICATION_OCTET_STREAM, FILES_URL + DOWNLOAD, FILE_UUID_1);

        assertNotSame(CONTENT, result.getResponse().getContentAsByteArray());
    }

    @Test
    void downloadSuccessWithAdmin() throws Exception {
        when(service.get(FILE_UUID_1, true, null)).thenReturn(buildFileData(FILE_UUID_1, 1));

        var result = performGetWith(roles(List.of(COLLEAGUE, ADMIN)),
                status().isOk(), MediaType.APPLICATION_OCTET_STREAM, FILES_URL + DOWNLOAD, FILE_UUID_1);

        assertNotSame(CONTENT, result.getResponse().getContentAsByteArray());
    }

    @Test
    void downloadUnsuccess() throws Exception { //NOSONAR used MockMvc checks
        when(service.get(FILE_UUID_1, true, CREATOR_ID)).thenThrow(NotFoundException.class);

        performGetWith(colleague(COLLEAGUE_UUID_STR), status().isNotFound(), FILES_URL + DOWNLOAD, FILE_UUID_1);
    }

    @Test
    void uploadFilesSuccess() throws Exception {
        var multipartUploadMetadataMock = getUploadMetadataMultipartFile("test_metadata.json");
        var multipartFileMock = getMultipartFileToUpload(CONTENT);

        var dataFile = buildFileData(FILE_UUID_1, 1);

        when(this.service.upload(any(), any(), any())).thenReturn(dataFile);

        when(auditorAware.getCurrentAuditor()). thenReturn(UUID.randomUUID());

        final var result = performMultipartWithMetadata(colleague(COLLEAGUE_UUID_STR), multipartUploadMetadataMock, multipartFileMock,
                status().isCreated(), FILES_URL);

        assertResponseContent(result.getResponse(), "files_upload_ok_response.json");
    }

    @Test
    void uploadFilesUnsuccessWithBadRequest() throws Exception {
        var multipartUploadMetadataMock = getUploadMetadataMultipartFile("test_metadata_for_2_files.json");
        var multipartFileMock = getMultipartFileToUpload(CONTENT);

        var dataFile = buildFileData(FILE_UUID_1, 1);

        when(this.service.upload(any(), any(), any())).thenReturn(dataFile);

        final var result = performMultipartWithMetadata(colleague(COLLEAGUE_UUID_STR), multipartUploadMetadataMock, multipartFileMock,
                status().isBadRequest(), FILES_URL);

        assertResponseContent(result.getResponse(), "files_upload_failed_response.json");
    }

    @Test
    void uploadFilesUnsuccessWithIncorrectFileDateBadRequest() throws Exception {
        var multipartUploadMetadataMock = getUploadMetadataMultipartFile("test_metadata_invalid_date.json");
        var multipartFileMock = getMultipartFileToUpload(CONTENT);

        var dataFile = buildFileData(FILE_UUID_1, 1);

        when(this.service.upload(any(), any(), any())).thenReturn(dataFile);

        final var result = performMultipartWithMetadata(colleague(COLLEAGUE_UUID_STR), multipartUploadMetadataMock, multipartFileMock,
                status().isBadRequest(), FILES_URL);

        assertResponseContent(result.getResponse(), "files_upload_failed_invalid_date_response.json");
    }

    @Test
    void uploadFilesUnsuccessWithInternalServerError() throws Exception { //NOSONAR used MockMvc checks
        var multipartUploadMetadataMock = getUploadMetadataMultipartFile("test_metadata.json");
        var multipartFileMock = getMultipartFileToUpload(CONTENT);

        when(this.service.upload(any(), any(), any())).thenThrow(RegistrationException.class);

        performMultipartWithMetadata(colleague(COLLEAGUE_UUID_STR), multipartUploadMetadataMock, multipartFileMock,
                status().isInternalServerError(), FILES_URL);
    }

    @Test
    void deleteFileByUuids() throws Exception { //NOSONAR used MockMvc checks
        doNothing().when(service).delete(FILE_UUID_1, CREATOR_ID);

        performDeleteWith(colleague(COLLEAGUE_UUID_STR), status().isOk(), FILES_URL + "/" + FILE_UUID_1);
    }

    @Test
    void deleteFileByUuidsUnsuccessIfFileIsNotFound() throws Exception { //NOSONAR used MockMvc checks
        doThrow(NotFoundException.class).when(service).delete(FILE_UUID_1, null);

        performDeleteWith(roles(List.of(COLLEAGUE, ADMIN)), status().isNotFound(), FILES_URL + "/" + FILE_UUID_1);
    }

    @Test
    void deleteFileByVersions() throws Exception { //NOSONAR used MockMvc checks
        doNothing().when(service).deleteVersions(PATH, FILE_NAME, List.of(VERSION_1, VERSION_2), CREATOR_ID);

        performDeleteWith(colleague(COLLEAGUE_UUID_STR), status().isOk(), DELETE_BY_VERSIONS_URL);
    }

    @Test
    void deleteFileByVersionsUnsuccessIfFileIsNotFound() throws Exception { //NOSONAR used MockMvc checks
        doThrow(NotFoundException.class).when(service).deleteVersions(PATH, FILE_NAME, List.of(VERSION_1, VERSION_2), null);

        performDeleteWith(roles(List.of(COLLEAGUE, ADMIN)), status().isNotFound(), DELETE_BY_VERSIONS_URL);
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
        FileType type = new FileType();
        type.setId(2);
        type.setCode("FORM");
        type.setDescription("GUI Form file");
        fileData.setType(type);
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