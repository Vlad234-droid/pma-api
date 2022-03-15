package com.tesco.pma.configuration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.exception.ErrorCodes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.json.BasicJsonTester;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomBearerTokenAuthenticationEntryPointTest {

    private CustomBearerTokenAuthenticationEntryPoint instance;

    @Mock
    private NamedMessageSourceAccessor mockNamedMessageSourceAccessor;

    private final BasicJsonTester jsonTester = new BasicJsonTester(getClass());

    @BeforeEach
    void setUp() {
        instance = new CustomBearerTokenAuthenticationEntryPoint(new MappingJackson2HttpMessageConverter(new ObjectMapper()),
                mockNamedMessageSourceAccessor);
        when(mockNamedMessageSourceAccessor.getMessage(ErrorCodes.UNAUTHENTICATED)).thenReturn("error-message");
    }

    @Test
    void commenceSetRestResponseWith401StatusAndWwwAuthHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        instance.setRealmName("test-realm");

        instance.commence(request, response, new BadCredentialsException("from-exception-message"));

        assertThat(response)
                .returns(401, from(HttpServletResponse::getStatus))
                .satisfies(wwwAuthenticateHeaderPresent())
                .returns(MediaType.APPLICATION_JSON_VALUE, from(MockHttpServletResponse::getContentType))
                .satisfies(hasJsonBodyEqualsTo("auth_failed.json"));
    }

    private Consumer<MockHttpServletResponse> hasJsonBodyEqualsTo(String json) {
        return response -> {
            try {
                assertThat(jsonTester.from(response.getContentAsString())).isEqualToJson(json);
            } catch (UnsupportedEncodingException e) {
                //never happen
            }
        };
    }

    private Consumer<MockHttpServletResponse> wwwAuthenticateHeaderPresent() {
        return response -> assertThat(response.getHeader("WWW-Authenticate")).isNotNull();
    }
}