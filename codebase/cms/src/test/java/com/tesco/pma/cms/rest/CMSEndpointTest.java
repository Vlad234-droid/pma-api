package com.tesco.pma.cms.rest;

import com.tesco.pma.bpm.camunda.flow.CamundaSpringBootTestConfig;
import com.tesco.pma.cms.service.HelpService;
import com.tesco.pma.colleague.profile.domain.ColleagueProfile;
import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.configuration.audit.AuditorAware;
import com.tesco.pma.rest.AbstractEndpointTest;
import com.tesco.pma.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ContextConfiguration;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.tesco.pma.util.TestUtils.createColleague;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2022-02-16 15:18
 */
@WebMvcTest(controllers = CMSEndpoint.class)
@ContextConfiguration(classes = CMSEndpointTest.LocalTestConfig.class)
class CMSEndpointTest extends AbstractEndpointTest {
    private static final String HELP_FAQ_URLS = "/cms/help-faq-urls";
    private static final String COLLEAGUE_UUID_1 = "10000000-1000-1000-1000-100000000001";

    @MockBean
    private ProfileService profileService;

    @MockBean
    private HelpService helpService;

    @MockBean
    private AuditorAware<UUID> auditorAware;

    @Profile("test")
    @Configuration
    @ComponentScan(basePackages = "com.tesco.pma.cms")
    @Import({JacksonAutoConfiguration.class, CamundaSpringBootTestConfig.class,
            // for security
            HttpMessageConvertersAutoConfiguration.class,
            OAuth2ResourceServerAutoConfiguration.class,
            RestTemplateAutoConfiguration.class,
            OAuth2ClientAutoConfiguration.class})
    static class LocalTestConfig {
    }

    @Test
    void getAll() throws Exception {
        var colleagueProfile = new ColleagueProfile();
        colleagueProfile.setColleague(createColleague(Map.of(TestUtils.KEYS.COLLEAGUE_UUID, UUID.fromString(COLLEAGUE_UUID_1))));
        when(profileService.findProfileByColleagueUuid(any())).thenReturn(Optional.of(colleagueProfile));
        when(helpService.getHelpFaqUrls(any(), isNull())).thenReturn(Map.of("key1", "value1"));

        var result = performGetWith(colleague(COLLEAGUE_UUID_1), status().isOk(), HELP_FAQ_URLS);
        assertResponseContent(result.getResponse(), "get_all.json");
    }

    @Test
    void getExact() throws Exception {
        var colleagueProfile = new ColleagueProfile();
        colleagueProfile.setColleague(createColleague(Map.of(TestUtils.KEYS.COLLEAGUE_UUID, UUID.fromString(COLLEAGUE_UUID_1))));
        when(profileService.findProfileByColleagueUuid(any())).thenReturn(Optional.of(colleagueProfile));
        when(helpService.getHelpFaqUrls(any(), anyCollection())).thenReturn(Map.of("key1", "value1", "key2", "value2"));

        var result = performGetWith(colleague(COLLEAGUE_UUID_1), status().isOk(), HELP_FAQ_URLS + "?keys=key1,key2");
        assertResponseContent(result.getResponse(), "get_exact.json");
    }

}