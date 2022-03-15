package com.tesco.pma.review.service.rest;

import com.tesco.pma.colleague.profile.domain.ColleagueEntity;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.configuration.audit.AuditorAware;
import com.tesco.pma.cycle.service.PMCycleService;
import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.file.api.File;
import com.tesco.pma.file.api.FileType;
import com.tesco.pma.fs.service.FileService;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.rest.AbstractEndpointTest;
import com.tesco.pma.review.LocalTestConfig;
import com.tesco.pma.review.service.ReviewService;
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
import static com.tesco.pma.review.util.TestDataUtils.files;
import static com.tesco.pma.security.UserRoleNames.ADMIN;
import static com.tesco.pma.security.UserRoleNames.COLLEAGUE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReviewEndpoint.class)
@ContextConfiguration(classes = {LocalTestConfig.class, ReviewEndpoint.class})
class ReviewEndpointTest extends AbstractEndpointTest {

    public static final String REVIEWS_FILES_URL = "/reviews/files";
    public static final String REVIEWS_FILES_URL_TEMPLATE = "/colleagues/{colleagueUuid}" + REVIEWS_FILES_URL;

    private static final String RESOURCES_PATH = "/com/tesco/pma/review/service/rest/";
    private static final String COLLEAGUE_UUID_STR = "6d37262f-3a00-4706-a74b-6bf98be65765";
    private static final UUID FILE_UUID = UUID.fromString("d3b0f689-ccce-4e80-9012-89f1ee39468d");
    private static final UUID CREATOR_ID = UUID.fromString(COLLEAGUE_UUID_STR);
    private static final String PATH = "/home/dev";

    private static final byte[] CONTENT = {72, 101, 108};
    private static final String TXT_FILE_CONTENT_TYPE = "application/vnd.oasis.opendocument.text";

    private static final Instant FILE_DATE = Instant.parse("2021-04-22T08:50:08Z");
    private static final String DESCRIPTION = "other file";
    private static final Instant CREATED_TIME = Instant.parse("2021-11-03T22:38:14Z");
    private static final int FILE_LENGTH = 23;
    private static final String PDF_FILE_NAME = "test.pdf";

    @MockBean
    private ReviewService mockReviewService;

    @MockBean
    private AuditorAware<UUID> mockAuditorAware;

    @MockBean
    private PMCycleService mockPmCycleService;

    @MockBean
    private FileService mockFileService;

    @MockBean
    private ProfileService mockProfileService;

    @Test
    void getReviewsFilesByColleagueWithColleague() throws Exception { //NOSONAR used MockMvc checks
        var colleagueUuid = UUID.randomUUID();

        when(mockFileService.get(new RequestQuery(), false, colleagueUuid, true)).thenReturn(files(3));

        mvc.perform(get(REVIEWS_FILES_URL_TEMPLATE, colleagueUuid.toString())
                        .with(colleague(colleagueUuid.toString())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));
    }

    @Test
    void getReviewsFilesByColleagueWithLineManager() throws Exception { //NOSONAR used MockMvc checks
        var colleagueUuid = UUID.randomUUID();
        var currentUserUuid = UUID.randomUUID();
        var colleagueEntity = new ColleagueEntity();
        colleagueEntity.setManagerUuid(currentUserUuid);

        when(mockProfileService.getColleague(colleagueUuid)).thenReturn(colleagueEntity);
        when(mockFileService.get(new RequestQuery(), false, colleagueUuid, true)).thenReturn(files(3));

        mvc.perform(get(REVIEWS_FILES_URL_TEMPLATE, colleagueUuid.toString())
                        .with(colleague(currentUserUuid.toString())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));
    }

    @Test
    void deleteFileByUuids() throws Exception { //NOSONAR used MockMvc checks
        var colleagueUuid = UUID.randomUUID();
        var fileUuid = UUID.randomUUID();
        doNothing().when(mockFileService).delete(fileUuid, colleagueUuid);

        performDeleteWith(colleague(colleagueUuid.toString()), status().isOk(), REVIEWS_FILES_URL + "/" + fileUuid);
    }

    @Test
    void deleteFileByUuidsUnsuccessIfFileIsNotFound() throws Exception { //NOSONAR used MockMvc checks
        var fileUuid = UUID.randomUUID();
        doThrow(NotFoundException.class).when(mockFileService).delete(fileUuid, null);

        performDeleteWith(roles(List.of(COLLEAGUE, ADMIN)), status().isNotFound(), REVIEWS_FILES_URL + "/" + fileUuid);
    }

    @Test
    void uploadReviewFilesSuccess() throws Exception {
        var multipartUploadMetadataMock = getUploadMetadataMultipartFile("test_metadata.json");
        var multipartFileMock = getMultipartFileToUpload(CONTENT, PDF_FILE_NAME);
        var fileType = new FileType();
        fileType.setId(3);
        fileType.setCode("PDF");
        fileType.setDescription("Portable document format file");

        var dataFile = buildFileData(FILE_UUID, PDF_FILE_NAME, 1, fileType);
        when(mockFileService.upload(any(), any(), any())).thenReturn(dataFile);

        final var result = performMultipartWithMetadata(colleague(COLLEAGUE_UUID_STR), multipartUploadMetadataMock, multipartFileMock,
                status().isCreated(), REVIEWS_FILES_URL);

        assertResponseContent(result.getResponse(), "files_upload_ok_response.json");
    }

    @Test
    void uploadReviewFilesUnsuccessWithInvalidType() throws Exception {
        var multipartUploadMetadataMock = getUploadMetadataMultipartFile("test_metadata_invalid.json");
        var multipartFileMock = getMultipartFileToUpload(CONTENT, "test.form");
        var fileUuid = UUID.randomUUID();
        var fileType = new FileType();
        fileType.setId(2);
        fileType.setCode("FORM");
        fileType.setDescription("GUI Form file");

        var dataFile = buildFileData(fileUuid, "test.form", 1, fileType);
        when(mockFileService.upload(any(), any(), any())).thenReturn(dataFile);

        final var result = performMultipartWithMetadata(colleague(COLLEAGUE_UUID_STR), multipartUploadMetadataMock, multipartFileMock,
                status().isBadRequest(), REVIEWS_FILES_URL);

        assertResponseContent(result.getResponse(), "files_upload_failed_type_response.json");
    }

    @Test
    void downloadSuccess() throws Exception {
        var fileType = new FileType();
        fileType.setId(3);
        fileType.setCode("PDF");
        fileType.setDescription("Portable document format file");
        when(mockFileService.get(FILE_UUID, true, CREATOR_ID))
                .thenReturn(buildFileData(FILE_UUID, PDF_FILE_NAME, 1, fileType));

        var result = performGetWith(colleague(COLLEAGUE_UUID_STR), status().isOk(),
                MediaType.APPLICATION_OCTET_STREAM, REVIEWS_FILES_URL_TEMPLATE + "/" + FILE_UUID + "/download", CREATOR_ID);

        assertThat(result.getResponse().getContentAsByteArray()).isNotSameAs(CONTENT);
    }

    @Test
    void downloadWithLineManager() throws Exception {
        var currentUserUuid = UUID.randomUUID();
        var colleagueEntity = new ColleagueEntity();
        colleagueEntity.setManagerUuid(currentUserUuid);

        var fileType = new FileType();
        fileType.setId(3);
        fileType.setCode("PDF");
        fileType.setDescription("Portable document format file");

        when(mockProfileService.getColleague(CREATOR_ID)).thenReturn(colleagueEntity);
        when(mockFileService.get(FILE_UUID, true, CREATOR_ID))
                .thenReturn(buildFileData(FILE_UUID, PDF_FILE_NAME, 1, fileType));

        var result = performGetWith(colleague(currentUserUuid.toString()), status().isOk(),
                MediaType.APPLICATION_OCTET_STREAM, REVIEWS_FILES_URL_TEMPLATE + "/" + FILE_UUID + "/download", CREATOR_ID);

        assertThat(result.getResponse().getContentAsByteArray()).isNotSameAs(CONTENT);
    }

    @Test
    void downloadUnsuccess() throws Exception { //NOSONAR used MockMvc checks
        var colleagueUuid = UUID.randomUUID();

        when(mockFileService.get(FILE_UUID, true, colleagueUuid)).thenThrow(NotFoundException.class);

        performGetWith(colleague(colleagueUuid.toString()), status().isNotFound(),
                REVIEWS_FILES_URL_TEMPLATE + "/" + FILE_UUID + "/download", colleagueUuid);
    }

    private MockMultipartFile getUploadMetadataMultipartFile(String fileName) throws IOException {
        return new MockMultipartFile("uploadMetadata", fileName, APPLICATION_JSON_VALUE, resourceToByteArray(fileName));
    }

    private MockMultipartFile getMultipartFileToUpload(byte[] content, String fileName) {
        return new MockMultipartFile("files", fileName, TXT_FILE_CONTENT_TYPE, content);
    }

    private byte[] resourceToByteArray(final String resourceName) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(RESOURCES_PATH + resourceName)) {
            return IOUtils.toByteArray(is);
        }
    }

    private File buildFileData(UUID uuid, String fileName, Integer version, FileType type) {
        var fileData = new File();
        fileData.setUuid(uuid);
        fileData.setPath(PATH);
        fileData.setVersion(version);
        fileData.setType(type);
        fileData.setStatus(ACTIVE);
        fileData.setDescription(DESCRIPTION);
        fileData.setCreatedBy(CREATOR_ID);
        fileData.setCreatedTime(CREATED_TIME);
        fileData.setFileName(fileName);
        fileData.setFileDate(FILE_DATE);
        fileData.setFileLength(FILE_LENGTH);
        fileData.setFileContent(CONTENT);

        return fileData;
    }

}