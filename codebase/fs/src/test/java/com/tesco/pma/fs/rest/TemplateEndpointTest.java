package com.tesco.pma.fs.rest;

import com.tesco.pma.fs.domain.ProcessTemplate;
import com.tesco.pma.fs.service.TemplateService;
import com.tesco.pma.rest.AbstractEndpointTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Instant;
import java.util.UUID;

import static com.tesco.pma.fs.domain.ProcessTemplateStatus.ACTIVE;
import static com.tesco.pma.fs.domain.ProcessTemplateType.FORM;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TemplateEndpoint.class, properties = {
        "tesco.application.security.enabled=false",
})
public class TemplateEndpointTest extends AbstractEndpointTest {

    private static final UUID TEMPLATE_UUID_1 = UUID.fromString("6d37262f-3a00-4706-a74b-6bf98be65765");

    private static final String FILE_NAME = "test1.txt";
    private static final String CREATOR_ID = "test";
    private static final String PATH = "/home/dev";
    private static final String TEMPLATES_URL = "/templates";

    @MockBean
    private TemplateService service;

    @Test
    void findTemplate() throws Exception {
        when(service.findTemplateByUuid(TEMPLATE_UUID_1, true)).thenReturn(buildProcessTemplate(TEMPLATE_UUID_1, 1));

        var result = performGet(status().isOk(), TEMPLATES_URL + "/" + TEMPLATE_UUID_1);

        assertResponseContent(result.getResponse(), "templates_get_ok_response.json");
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