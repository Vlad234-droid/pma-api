package com.tesco.pma.configuration.colleague;

import com.tesco.pma.service.colleague.client.ColleagueApiClient;
import com.tesco.pma.service.colleague.client.RestTemplateBasedColleagueApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ColleagueApiClientConfiguration {

    @Bean
    public ColleagueApiClient colleagueApiClient(
            RestTemplate restTemplate,
            @Value("${tesco.application.external-endpoints.colleague-api.base-url}") String colleagueApiBaseUrl) {
        return new RestTemplateBasedColleagueApiClient(colleagueApiBaseUrl, restTemplate);
    }

}
