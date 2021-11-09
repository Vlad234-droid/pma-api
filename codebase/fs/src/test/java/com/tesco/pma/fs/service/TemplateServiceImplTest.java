package com.tesco.pma.fs.service;

import com.tesco.pma.exception.NotFoundException;
import com.tesco.pma.exception.RegistrationException;
import com.tesco.pma.fs.domain.ProcessTemplate;
import com.tesco.pma.fs.domain.UploadMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import com.tesco.pma.fs.dao.TemplateDAO;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import static com.tesco.pma.fs.domain.ProcessTemplateStatus.ACTIVE;
import static com.tesco.pma.fs.domain.ProcessTemplateType.FORM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@ActiveProfiles("test")
@SpringBootTest(classes = TemplateServiceImpl.class)
@ExtendWith(MockitoExtension.class)
public class TemplateServiceImplTest {

    private static final UUID TEMPLATE_UUID_1 = UUID.fromString("6d37262f-3a00-4706-a74b-6bf98be65765");
    private static final UUID TEMPLATE_UUID_2 = UUID.fromString("3d1ebbee-bafc-467c-acf1-5334db06e723");
    private static final String FILE_NAME = "test1.txt";
    private static final byte[] CONTENT = "Hello, World".getBytes();
    private static final String CREATOR_ID = "test";
    private static final String PATH = "/home/dev";

    @Autowired
    private TemplateServiceImpl service;

    @MockBean
    private TemplateDAO templateDao;

    @Test
    void upload() throws IOException {
        var file = new MockMultipartFile(FILE_NAME, FILE_NAME, TEXT_PLAIN_VALUE, CONTENT);
        var inputStream = file.getInputStream();
        var uploadMetadata = new UploadMetadata();

        when(templateDao.getMaxVersion(PATH, FILE_NAME)).thenReturn(1);
        when(templateDao.save(any())).thenReturn(1);

        var result = service.upload(inputStream, uploadMetadata, file, CREATOR_ID);
        assertNotNull(result);
        assertEquals(FILE_NAME, result.getFileName());
    }

    @Test
    void uploadThrowsExceptionWhenDaoReturnsNotOne() throws IOException {
        var template = buildProcessTemplate(TEMPLATE_UUID_1, 1);
        var file = new MockMultipartFile("file1", FILE_NAME, TEXT_PLAIN_VALUE, CONTENT);
        var inputStream = file.getInputStream();
        var uploadMetadata = new UploadMetadata();

        when(templateDao.getMaxVersion(anyString(), anyString())).thenReturn(anyInt());
        when(templateDao.save(template)).thenReturn(-1);

        assertThrows(RegistrationException.class, () -> service.upload(inputStream, uploadMetadata, file, CREATOR_ID));
    }

    @Test
    void findByUuid() {
        var template = buildProcessTemplate(TEMPLATE_UUID_1, 1);
        when(templateDao.findByUuid(TEMPLATE_UUID_1, false)).thenReturn(template);

        var result = service.findByUuid(TEMPLATE_UUID_1, false);

        assertEquals(template, result);
    }

    @Test
    void findByUuidThrowsExceptionWhenDaoReturnsNull() {
        when(templateDao.findByUuid(TEMPLATE_UUID_1, true)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> service.findByUuid(TEMPLATE_UUID_1, true));
    }

    @Test
    void findAll() {
        var templates = Arrays.asList(buildProcessTemplate(TEMPLATE_UUID_1, 1),
                buildProcessTemplate(TEMPLATE_UUID_2, 1));

        when(templateDao.findAll(false)).thenReturn(templates);

        var result = service.findAll(false);

        assertEquals(templates, result);
    }

    private ProcessTemplate buildProcessTemplate(UUID uuid, Integer version) {
        final var template = new ProcessTemplate();
        template.setUuid(uuid);
        template.setPath(PATH);
        template.setVersion(version);
        template.setType(FORM);
        template.setStatus(ACTIVE);
        template.setDescription("other template");
        template.setCreatedBy(CREATOR_ID);
        template.setCreatedTime(Instant.parse("2021-11-03T22:38:14Z"));
        template.setFileName(FILE_NAME);
        template.setFileDate(Instant.parse("2021-04-22T08:50:08Z"));
        template.setFileLength(3);
        template.setFileContent(new byte[] { 72, 101, 108});

        return template;
    }
}