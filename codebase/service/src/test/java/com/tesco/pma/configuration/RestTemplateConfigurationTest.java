package com.tesco.pma.configuration;

import java.io.IOException;
import java.net.URI;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

@ActiveProfiles("test")
@SpringBootTest(classes = {JacksonAutoConfiguration.class, RestTemplateAutoConfiguration.class,
        RestTemplateConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RestTemplateConfigurationTest {

    private static int actualConnectTimeout;
    private static int actualReadTimeout;

    @Value("${tesco.application.rest-template.connect-timeout}")
    private int expectedConnectTimeout;

    @Value("${tesco.application.rest-template.read-timeout}")
    private int expectedReadTimeout;

    @Autowired
    private RestTemplate restTemplate;

    @MockBean
    private NamedMessageSourceAccessor mockNamedMessageSourceAccessor;

    @MockBean(name = "pmaClientTokenSupplier")
    private Supplier<String> pmaClientTokenSupplier;

    @Test
    void test() {
        assertEquals(restTemplate.getRequestFactory().getClass(), InterceptingClientHttpRequestFactory.class);
        assertFalse(restTemplate.getInterceptors().isEmpty());
        assertEquals(expectedConnectTimeout, actualConnectTimeout);
        assertEquals(expectedReadTimeout, actualReadTimeout);
    }

    public static class TestFactory implements ClientHttpRequestFactory {

        @Override
        public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
            return mock(ClientHttpRequest.class);
        }

        public void setConnectTimeout(int timeout) {
            actualConnectTimeout = timeout;
        }

        public void setReadTimeout(int timeout) {
            actualReadTimeout = timeout;
        }
    }
}
