package com.tesco.pma.cms.service;

import com.tesco.pma.bpm.camunda.flow.AbstractCamundaSpringBootTest;
import com.tesco.pma.bpm.camunda.flow.CamundaSpringBootTestConfig;
import com.tesco.pma.cms.api.ContentEntry;
import com.tesco.pma.util.TestUtils.KEYS;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.tesco.pma.util.TestUtils.createColleague;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Vadim Shatokhin <a href="mailto:vadim.shatokhin1@tesco.com">vadim.shatokhin1@tesco.com</a>
 * 2022-02-16 10:34
 */
@ActiveProfiles(value = {"test"})
@SpringBootTest(classes = {JacksonAutoConfiguration.class, CamundaSpringBootTestConfig.class})
class HelpServiceTest extends AbstractCamundaSpringBootTest {
    private static final String KEY_SYSTEM_GUIDANCE_AND_FAQS = "system-guidance-and-faqs";
    private static final String OUTPUT_SYSTEM_GUIDANCE_AND_FAQS_PEOPLEDATAINTS = "https://tesco.sharepoint.com/:u:/r/sites/" +
            "TescoSourcingPeopleTeamSite/SitePages/System-Guides,-Learning-and-Help.aspx?csf=1&web=1&e=HUY4gs";
    private static final String OUTPUT_SYSTEM_GUIDANCE_AND_FAQS_UK_HOSPITALITY = "https://help.ourtesco.com/hc/en-us/sections/" +
            "4419952578962-System-Guidance-and-FAQs";
    private static final String OUTPUT_SYSTEM_GUIDANCE_AND_FAQS_IE = "https://colleague-help.ourtesco.com/hc/en-us/articles/" +
            "4417350843540-System-guides-learning-and-help";
    private static final String OUTPUT_SYSTEM_GUIDANCE_AND_FAQS_IN = "https://view.pagetiger.com/Your-ContributionBLR";
    private static final String OUTPUT_SYSTEM_GUIDANCE_AND_FAQS_GB = "https://colleague-help.ourtesco.com/hc/en-us/articles/" +
            "4417358220820-System-guides-learning-and-help";

    private static final String IAM_SOURCE_PEOPLEDATAINTS = "PeopleDataINTS";
    private static final String IAM_SOURCE_UK_HOSPITALITY = "UK Hospitality";
    private static final String COUNTRY_GB = "GB";
    private static final String COUNTRY_IE = "IE";
    private static final String COUNTRY_IN = "IN";

    @SpyBean
    HelpServiceImpl helpService;

    @MockBean
    ContentEntryService contentEntryService;

    @Test
    void getExact() {
        assertSuccessRule(Map.of(KEYS.COUNTRY_CODE, COUNTRY_GB), OUTPUT_SYSTEM_GUIDANCE_AND_FAQS_GB);
        assertSuccessRule(Map.of(KEYS.COUNTRY_CODE, "CH"), OUTPUT_SYSTEM_GUIDANCE_AND_FAQS_GB);
        assertSuccessRule(Map.of(KEYS.COUNTRY_CODE, COUNTRY_IE), OUTPUT_SYSTEM_GUIDANCE_AND_FAQS_IE);
        assertSuccessRule(Map.of(KEYS.COUNTRY_CODE, COUNTRY_IN), OUTPUT_SYSTEM_GUIDANCE_AND_FAQS_IN);

        assertSuccessRule(Map.of(KEYS.COUNTRY_CODE, COUNTRY_IN, KEYS.IAM_SOURCE, IAM_SOURCE_PEOPLEDATAINTS),
                OUTPUT_SYSTEM_GUIDANCE_AND_FAQS_PEOPLEDATAINTS);
        assertSuccessRule(Map.of(KEYS.COUNTRY_CODE, COUNTRY_GB, KEYS.IAM_SOURCE, IAM_SOURCE_UK_HOSPITALITY),
                OUTPUT_SYSTEM_GUIDANCE_AND_FAQS_UK_HOSPITALITY);
    }

    @Test
    void getAll() {
        var result = helpService.getHelpFaqUrls(createColleague(Map.of(KEYS.COUNTRY_CODE, COUNTRY_GB)), null);
        assertEquals(17, result.size());
    }

    private void assertSuccessRule(Map<KEYS, Object> params, String expected) {
        var result = helpService.getHelpFaqUrls(createColleague(params), Set.of(KEY_SYSTEM_GUIDANCE_AND_FAQS));

        assertEquals(expected, result.get(KEY_SYSTEM_GUIDANCE_AND_FAQS));
    }

    @Test
    void getHelpFaqContentEntriesTest() {
        var compoundKey = "help-faqs/iam-sources/peopledataints/ids/system-guidance-and-faqs";
        var content = createContentEntry(compoundKey);
        var colleague = createColleague(Map.of(KEYS.IAM_SOURCE, "peopledataints"));

        Mockito.when(contentEntryService.find(Mockito.eq(compoundKey))).thenReturn(List.of(content));

        var result = helpService.getHelpFaqs(colleague, Set.of("system-guidance-and-faqs"));

        assertEquals(content.getUuid(), result.get("system-guidance-and-faqs").get(0).getUuid());
    }

    private ContentEntry createContentEntry(String key){
        var content = new ContentEntry();
        content.setUuid(UUID.randomUUID());
        content.setKey(key);
        return content;
    }
}