package com.tesco.pma.rest;

import com.tesco.pma.TestConfig;
import com.tesco.pma.api.User;
import com.tesco.pma.api.security.SubsidiaryPermission;
import com.tesco.pma.security.UserRoleNames;
import com.tesco.pma.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.BasicJsonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.UUID.fromString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@ActiveProfiles("test")
@ContextConfiguration(classes = TestConfig.class)
@WithMockUser(username = AbstractEndpointTest.MOCK_CREATOR_ID)
public abstract class AbstractEndpointTest {

    protected static final UUID RP_UUID = UUID.fromString("7f8218ac-2537-4091-9baf-84b37df450e6");
    protected static final UUID RP_UUID_2 = UUID.fromString("4e10cb67-7667-4a01-b0ca-032bf5ae8e3f");
    protected static final String RP_NAME = "2021";
    protected static final String RP_NAME_2 = "2020";
    protected static final LocalDate START_DATE = LocalDate.parse("2021-04-05");
    protected static final LocalDate END_DATE = LocalDate.parse("2021-04-04");
    protected static final LocalDate START_DATE_2 = LocalDate.parse("2020-04-05");
    protected static final LocalDate END_DATE_2 = LocalDate.parse("2020-04-04");
    protected static final UUID SUBSIDIARY_UUID = fromString("5d9bbac9-850a-45e3-856b-50be9b9f563c");
    protected static final UUID ILLEGAL_SUBSIDIARY_UUID = UUID.fromString("5d9bbac9-850a-45e3-856b-50be9b9f564c");
    protected static final String MOCK_CREATOR_ID = "MockCreatorId";

    protected static final String DIMENSION_CONFIG_NAME = "Dim1";
    protected static final String DIMENSION_CONFIG_NAME_2 = "Dim2";

    protected static final String EMPTY_URL_TEMPLATE = "/";
    protected static final String NOT_FOUND_URL_TEMPLATE = "/wrong/template";

    protected static final String ERROR_GENERAL_RESPONSE_JSON_FILE_NAME = "error_general_response.json";
    protected static final String ERROR_GENERAL_FOR_ILLEGAL_URI_VAR_RESPONSE_JSON_FILE_NAME = "general_error_for_illegal_uri_var_response.json";

    protected static final String SUCCESS_RESPONSE_WITHOUT_DATA = "{\"success\":true}";
    protected static final String SUCCESS_RESPONSE_WITH_EMPTY_ARRAY_DATA = "{\"success\":true,\"data\":[]}";
    protected static final UUID USER_COLLEAGUE_UUID = UUID.randomUUID();

    protected final BasicJsonTester json = new BasicJsonTester(getClass());

    @Autowired
    protected MockMvc mvc;

    @MockBean
    protected UserService mockUserService;

    protected void assertResponseContent(MockHttpServletResponse actualResponse, String expectedJsonResource) throws UnsupportedEncodingException {
        var actualJsonContent = json.from(actualResponse.getContentAsString());
        assertNotNull(actualJsonContent);
        assertThat(actualJsonContent).isStrictlyEqualToJson(expectedJsonResource);
    }


    protected Instant getInstantMoment(String dateString) {
        var isoFormatter = DateTimeFormatter.ISO_INSTANT;
        return Instant.from(isoFormatter.parse(dateString));
    }

    protected MvcResult performMultipart(MockMultipartFile fileToUpload,
                                         ResultMatcher status,
                                         String urlTemplate, Object... uriVars) throws Exception {
        return mvc.perform(multipart(urlTemplate, uriVars)
                .file(fileToUpload))
                .andExpect(status)
                .andReturn();
    }

    protected MvcResult performMultipartWith(RequestPostProcessor postProcessor, MockMultipartFile fileToUpload,
                                             ResultMatcher status,
                                             String urlTemplate, Object... uriVars) throws Exception {
        return mvc.perform(multipart(urlTemplate, uriVars)
                .file(fileToUpload).with(postProcessor))
                .andExpect(status)
                .andReturn();
    }

    protected MvcResult performMultipartWithMetadata(MockMultipartFile uploadMetadata, MockMultipartFile fileToUpload,
                                                     ResultMatcher status,
                                                     String urlTemplate, Object... uriVars) throws Exception {
        return mvc.perform(multipart(urlTemplate, uriVars)
                .file(fileToUpload)
                .file(uploadMetadata))
                .andExpect(status)
                .andReturn();
    }

    protected MvcResult performGet(ResultMatcher status,
                                   String urlTemplate, Object... uriVars) throws Exception {
        return mvc.perform(get(urlTemplate, uriVars))
                .andDo(print())
                .andExpect(status)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    protected MvcResult performGetWith(RequestPostProcessor postProcessor, ResultMatcher status,
                                       String urlTemplate, Object... uriVars) throws Exception {
        return mvc.perform(get(urlTemplate, uriVars).with(postProcessor))
                .andDo(print())
                .andExpect(status)
                .andReturn();
    }

    protected MvcResult performPost(String contentSource, ResultMatcher status,
                                    String urlTemplate, Object... uriVars) throws Exception {
        return mvc.perform(post(urlTemplate, uriVars)
                .content(json.from(contentSource).getJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status)
                .andReturn();
    }

    protected MvcResult performPostWith(RequestPostProcessor postProcessor, String contentSource, ResultMatcher status,
                                        String urlTemplate, Object... uriVars) throws Exception {
        return mvc.perform(post(urlTemplate, uriVars).with(postProcessor)
                .content(json.from(contentSource).getJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status)
                .andReturn();
    }

    protected MvcResult performPut(String contentSource, ResultMatcher status,
                                   String urlTemplate, Object... uriVars) throws Exception {
        return mvc.perform(put(urlTemplate, uriVars)
                .content(json.from(contentSource).getJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status)
                .andReturn();
    }

    protected MvcResult performPatch(String contentSource, ResultMatcher status,
                                     String urlTemplate, Object... uriVars) throws Exception {
        return mvc.perform(patch(urlTemplate, uriVars)
                .content(json.from(contentSource).getJson())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status)
                .andReturn();
    }

    protected MvcResult performDelete(ResultMatcher status, String urlTemplate, Object... uriVars) throws Exception {
        return mvc.perform(delete(urlTemplate, uriVars))
                .andDo(print())
                .andExpect(status)
                .andReturn();
    }

    protected MockMvc getMvc() {
        return mvc;
    }

    protected RequestPostProcessor viewer() {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .authorities(AuthorityUtils.createAuthorityList("ROLE_" + UserRoleNames.VIEWER));
    }

    protected RequestPostProcessor admin() {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .authorities(AuthorityUtils.createAuthorityList("ROLE_" + UserRoleNames.ADMIN));
    }

    protected RequestPostProcessor subsidiaryManagerOf(UUID... subsidiaryUuids) {
        final var requestPostProcessor = SecurityMockMvcRequestPostProcessors.jwt()
                .authorities(AuthorityUtils.createAuthorityList("ROLE_" + UserRoleNames.SUBSIDIARY_MANAGER));
        if (subsidiaryUuids.length != 0) {
            final var subsidiaryPermissions = Arrays.stream(subsidiaryUuids).filter(Objects::nonNull)
                    .map(subsidiaryUuid -> SubsidiaryPermission.of(USER_COLLEAGUE_UUID, subsidiaryUuid, UserRoleNames.SUBSIDIARY_MANAGER))
                    .collect(Collectors.toSet());
            final var user = new User(USER_COLLEAGUE_UUID);
            user.setSubsidiaryPermissions(subsidiaryPermissions);
            when(mockUserService.findUserByAuthentication(any(), any())).thenReturn(Optional.of(user));
        }
        return requestPostProcessor;
    }

    protected RequestPostProcessor security(String role, UUID... subsidiaryUuids) {
        final RequestPostProcessor requestPostProcessor;
        switch (role) {
            case UserRoleNames.ADMIN:
                requestPostProcessor = admin();
                break;
            case UserRoleNames.VIEWER:
                requestPostProcessor = viewer();
                break;
            case UserRoleNames.SUBSIDIARY_MANAGER:
                requestPostProcessor = subsidiaryManagerOf(subsidiaryUuids);
                break;
            case "Anonymous":
                requestPostProcessor = SecurityMockMvcRequestPostProcessors.anonymous();
                break;
            default:
                throw new IllegalArgumentException("Unsupported role: " + role);
        }
        return requestPostProcessor;
    }
}
