package com.tesco.pma.rest;

import com.tesco.pma.security.UserRoleNames;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.BasicJsonTester;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@ActiveProfiles("test")
@SuppressWarnings("PMD.TooManyMethods")
public abstract class AbstractEndpointTest {

    protected final BasicJsonTester json = new BasicJsonTester(getClass());

    @Autowired
    protected MockMvc mvc;

    protected void assertResponseContent(MockHttpServletResponse actualResponse,
                                         String expectedJsonResource) throws UnsupportedEncodingException {
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

    protected MvcResult performMultipartWithMetadata(RequestPostProcessor postProcessor,
                                                     MockMultipartFile uploadMetadata, MockMultipartFile fileToUpload,
                                                     ResultMatcher status,
                                                     String urlTemplate, Object... uriVars) throws Exception {
        return mvc.perform(multipart(urlTemplate, uriVars)
                        .file(fileToUpload)
                        .file(uploadMetadata)
                        .with(postProcessor))
                .andExpect(status)
                .andReturn();
    }

    protected MvcResult performGet(ResultMatcher status,
                                   String urlTemplate, Object... uriVars) throws Exception {
        return performGet(status, MediaType.APPLICATION_JSON, urlTemplate, uriVars);
    }

    protected MvcResult performGet(ResultMatcher status, MediaType contentType,
                                   String urlTemplate, Object... uriVars) throws Exception {
        return mvc.perform(get(urlTemplate, uriVars))
                .andDo(print())
                .andExpect(status)
                .andExpect(content().contentTypeCompatibleWith(contentType))
                .andReturn();
    }

    protected MvcResult performGetWith(RequestPostProcessor postProcessor, ResultMatcher status,
                                       String urlTemplate, Object... uriVars) throws Exception {
        return performGetWith(postProcessor, status, MediaType.APPLICATION_JSON, urlTemplate, uriVars);
    }

    protected MvcResult performGetWith(RequestPostProcessor postProcessor, ResultMatcher status, MediaType contentType,
                                       String urlTemplate, Object... uriVars) throws Exception {
        return mvc.perform(get(urlTemplate, uriVars).with(postProcessor))
                .andDo(print())
                .andExpect(status)
                .andExpect(content().contentTypeCompatibleWith(contentType))
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

    protected MvcResult performDeleteWith(RequestPostProcessor postProcessor, ResultMatcher status,
                                          String urlTemplate, Object... uriVars) throws Exception {
        return mvc.perform(delete(urlTemplate, uriVars).with(postProcessor))
                .andDo(print())
                .andExpect(status)
                .andReturn();
    }

    protected MockMvc getMvc() {
        return mvc;
    }

    protected RequestPostProcessor colleague() {
        return initRequestPostProcessor(UserRoleNames.COLLEAGUE);
    }

    protected RequestPostProcessor colleague(String subject) {
        return initRequestPostProcessor(UserRoleNames.COLLEAGUE, subject);
    }

    protected RequestPostProcessor admin() {
        return initRequestPostProcessor(UserRoleNames.ADMIN);
    }

    protected RequestPostProcessor admin(String subject) {
        return initRequestPostProcessor(UserRoleNames.ADMIN, subject);
    }

    protected RequestPostProcessor lineManager() {
        return initRequestPostProcessor(UserRoleNames.LINE_MANAGER);
    }

    protected RequestPostProcessor lineManager(String subject) {
        return initRequestPostProcessor(UserRoleNames.LINE_MANAGER, subject);
    }

    protected RequestPostProcessor peopleTeam() {
        return initRequestPostProcessor(UserRoleNames.PEOPLE_TEAM);
    }

    protected RequestPostProcessor peopleTeam(String subject) {
        return initRequestPostProcessor(UserRoleNames.PEOPLE_TEAM, subject);
    }

    protected RequestPostProcessor talentAdmin() {
        return initRequestPostProcessor(UserRoleNames.TALENT_ADMIN);
    }

    protected RequestPostProcessor talentAdmin(String subject) {
        return initRequestPostProcessor(UserRoleNames.TALENT_ADMIN, subject);
    }

    protected RequestPostProcessor processManager() {
        return initRequestPostProcessor(UserRoleNames.PROCESS_MANAGER);
    }

    protected RequestPostProcessor processManager(String subject) {
        return initRequestPostProcessor(UserRoleNames.PROCESS_MANAGER, subject);
    }

    protected RequestPostProcessor allRoles() {
        return allRoles("user");
    }

    protected RequestPostProcessor allRoles(String subject) {
        return roles(UserRoleNames.ALL, subject);
    }

    protected RequestPostProcessor roles(List<String> roles) {
        return roles(roles, "user");
    }

    protected RequestPostProcessor roles(List<String> roles, String subject) {
        var authorities = roles.stream().map(role -> "ROLE_" + role).collect(Collectors.toList());
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(builder -> builder.subject(subject))
                .authorities(AuthorityUtils.createAuthorityList(authorities.toArray(new String[0])));
    }

    private RequestPostProcessor initRequestPostProcessor(String role) {
        return initRequestPostProcessor(role, "user");
    }

    private RequestPostProcessor initRequestPostProcessor(String role, String subject) {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(builder -> builder.subject(subject))
                .authorities(AuthorityUtils.createAuthorityList("ROLE_" + role));
    }

    protected RequestPostProcessor security(String role) {
        final RequestPostProcessor requestPostProcessor;
        switch (role) {
            case UserRoleNames.ADMIN:
                requestPostProcessor = admin();
                break;
            case UserRoleNames.COLLEAGUE:
                requestPostProcessor = colleague();
                break;
            case UserRoleNames.LINE_MANAGER:
                requestPostProcessor = lineManager();
                break;
            case UserRoleNames.PEOPLE_TEAM:
                requestPostProcessor = peopleTeam();
                break;
            case UserRoleNames.TALENT_ADMIN:
                requestPostProcessor = talentAdmin();
                break;
            case UserRoleNames.PROCESS_MANAGER:
                requestPostProcessor = processManager();
                break;
            case "Anonymous":
                requestPostProcessor = SecurityMockMvcRequestPostProcessors.anonymous();
                break;
            default:
                throw new IllegalArgumentException("Unsupported role: " + role);
        }
        return requestPostProcessor;
    }

    protected SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtWithSubject(String subject) {
        return SecurityMockMvcRequestPostProcessors.jwt().jwt(builder -> builder.subject(subject));
    }


}
