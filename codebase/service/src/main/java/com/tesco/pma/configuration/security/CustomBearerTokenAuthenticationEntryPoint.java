package com.tesco.pma.configuration.security;

import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.error.ApiError;
import com.tesco.pma.exception.ErrorCodes;
import com.tesco.pma.logging.LogFormatter;
import com.tesco.pma.rest.RestResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Customization of {@link BearerTokenAuthenticationEntryPoint} to add {@link RestResponse} body to the response.
 *
 * <p>Motivation: impossibility to handle {@link AuthenticationException} in {@link com.tesco.pma.error.ApiExceptionHandler}
 * cause it's happened in Security Filters before any @ControllerAdvice.
 */
@Slf4j
public class CustomBearerTokenAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final MappingJackson2HttpMessageConverter messageConverter;
    private final NamedMessageSourceAccessor messages;
    private final BearerTokenAuthenticationEntryPoint delegate = new BearerTokenAuthenticationEntryPoint();
    private String realmName;

    public CustomBearerTokenAuthenticationEntryPoint(MappingJackson2HttpMessageConverter messageConverter,
                                                     NamedMessageSourceAccessor messages) {
        this.messageConverter = messageConverter;
        this.messages = messages;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        delegate.commence(request, response, authException);

        final var message = messages.getMessage(ErrorCodes.UNAUTHENTICATED);
        log.error(LogFormatter.formatMessage(ErrorCodes.UNAUTHENTICATED, message), authException);

        final var apiError = ApiError.builder()
                .code(ErrorCodes.UNAUTHENTICATED.getCode())
                .message(message)
                .build();
        apiError.addDetails(ApiError.builder()
                .target(realmName)
                .message(authException.getMessage())
                .build());

        messageConverter.write(RestResponse.fail(apiError), MediaType.APPLICATION_JSON, new ServletServerHttpResponse(response));
    }

    /**
     * Set the default realm name to use in the bearer token error response
     * @param realmName realm name
     */
    public void setRealmName(String realmName) {
        this.realmName = realmName;
        delegate.setRealmName(realmName);
    }
}
