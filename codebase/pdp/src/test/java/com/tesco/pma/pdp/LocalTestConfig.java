package com.tesco.pma.pdp;

import com.tesco.pma.configuration.MessageSourceConfig;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.ApplicationAvailabilityBean;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.Properties;

@Profile("test")
@Configuration
@ComponentScan(basePackages = {"com.tesco.pma.pdp", "com.tesco.pma.configuration", "com.tesco.pma.error"})
@Import({MessageSourceAutoConfiguration.class,
        MessageSourceConfig.class,
        JacksonAutoConfiguration.class,
        HttpMessageConvertersAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class,
        RestTemplateAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class
})
public class LocalTestConfig {

    private static final String STANDARD_PDP_FORM = "pdp/forms/standard_pdp.form";
    private static final String PDP_TEMPLATE = "pdp/templates/Personal Development Plan Template.pptx";

    @Bean
    public static PropertySourcesPlaceholderConfigurer properties() throws Exception {
        var pspc = new PropertySourcesPlaceholderConfigurer();

        var properties = new Properties();
        properties.setProperty("tesco.application.pdp.form.key", STANDARD_PDP_FORM);
        properties.setProperty("tesco.application.pdp.template.key", PDP_TEMPLATE);
        pspc.setProperties(properties);
        return pspc;
    }

    @Bean
    public BuildProperties buildProperties() {
        return new BuildProperties(new Properties());
    }

    @Bean
    public ApplicationAvailability applicationAvailability() {
        return new ApplicationAvailabilityBean();
    }
}