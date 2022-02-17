package com.tesco.pma.configuration;

import com.tesco.pma.configuration.rest.model.AbstractRequestQueryMixIn;
import com.tesco.pma.configuration.security.AdditionalAuthProperties;
import com.tesco.pma.error.ApiError;
import com.tesco.pma.error.ApiValidationError;
import com.tesco.pma.exception.ErrorCodes;
import com.tesco.pma.pagination.RequestQuery;
import com.tesco.pma.rest.HttpStatusCodes;
import com.tesco.pma.rest.RestResponse;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.SpringDocUtils;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import static com.tesco.pma.exception.ErrorCodes.ACCESS_DENIED;
import static com.tesco.pma.exception.ErrorCodes.UNAUTHENTICATED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * SpringDoc OpenApi Configuration related to /api-docs endpoint.
 */
@RequiredArgsConstructor
@SuppressWarnings("PMD.CouplingBetweenObjects") //High amount of different objects as members denotes a high coupling
@Configuration
@OpenAPIDefinition(servers = @Server(url = "/", description = "Default server URL"),
        info = @Info(title = "PMA API", version = "1.0", description = "Documentation PMA API v1.0"))
public class SpringDocOpenApiConfiguration {

    public static final String BEARER_JWT_IDENTITY_SECURITY_SCHEMA_NAME = "bearer-jwt-identity";
    public static final String ADDITIONAL_AUTH_JWT_SECURITY_SCHEMA_NAME = "additional-auth-jwt";
    public static final String UNAUTHENTICATED_HEADER_EXAMPLE = "Bearer error=\"invalid_token\", "
            + "error_description=\"Provided token isn't active\", "
            + "error_uri=\"https://tools.ietf.org/html/rfc6750#section-3.1\"";
    public static final String FORBIDDEN_HEADER_EXAMPLE = "Bearer error=\"insufficient_scope\", "
            + "error_description=\"The request requires higher privileges than provided by the access token.\", "
            + "error_uri=\"https://tools.ietf.org/html/rfc6750#section-3.1\"";
    public static final String REST_RESPONSE_VOID = RestResponse.class.getSimpleName() + Void.class.getSimpleName();

    static {
        SpringDocUtils.getConfig().replaceWithClass(RequestQuery.class, AbstractRequestQueryMixIn.class);
    }

    private final NamedMessageSourceAccessor messages;

    @Bean
    public OpenApiCustomiser securityOpenApiBuilderCustomizer() {
        return openApi -> openApi.addSecurityItem(new SecurityRequirement()
                .addList(BEARER_JWT_IDENTITY_SECURITY_SCHEMA_NAME))
                .getComponents()
                .addSecuritySchemes(BEARER_JWT_IDENTITY_SECURITY_SCHEMA_NAME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("supports old Identity V3 token as a fallback"));
    }

    @Bean
    @ConditionalOnProperty(name = "tesco.application.security.additional-auth.enabled", havingValue = "true", matchIfMissing = true)
    public OpenApiCustomiser additionalSecurityOpenApiBuilderCustomizer(AdditionalAuthProperties additionalAuthProperties) {
        return openApi -> openApi.addSecurityItem(new SecurityRequirement()
                .addList(ADDITIONAL_AUTH_JWT_SECURITY_SCHEMA_NAME))
                .getComponents()
                .addSecuritySchemes(ADDITIONAL_AUTH_JWT_SECURITY_SCHEMA_NAME, new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name(additionalAuthProperties.getTokenHeaderName())
                        .description("additional auth. Used to authorize API by AD groups."));
    }

    @Bean
    public OpenApiCustomiser globalResponsesOpenApiBuilderCustomizer() {
        return openApi -> openApi.getComponents()
                .addResponses(BAD_REQUEST.name(), badRequestApiResponse())
                .addResponses(UNAUTHORIZED.name(), unauthorizedApiResponse())
                .addResponses(FORBIDDEN.name(), forbiddenApiResponse())
                .addResponses(INTERNAL_SERVER_ERROR.name(), internalServerErrorApiResponse())
                .addResponses(NOT_FOUND.name(), notFoundApiResponse());
    }

    @Bean
    public OperationCustomizer defaultResponsesAdderOperationCustomizer() {
        return (operation, handlerMethod) -> {
            if (handlerMethod.getMethodParameters().length != 0) {
                operation.getResponses().computeIfAbsent(HttpStatusCodes.BAD_REQUEST, key -> new ApiResponse().$ref(BAD_REQUEST.name()));
            }
            operation.getResponses().computeIfAbsent(HttpStatusCodes.UNAUTHORIZED, key -> new ApiResponse().$ref(UNAUTHORIZED.name()));
            operation.getResponses().computeIfAbsent(HttpStatusCodes.FORBIDDEN, key -> new ApiResponse().$ref(FORBIDDEN.name()));
            operation.getResponses().computeIfAbsent(HttpStatusCodes.INTERNAL_SERVER_ERROR, key ->
                    new ApiResponse().$ref(INTERNAL_SERVER_ERROR.name()));
            operation.getResponses().computeIfPresent(HttpStatusCodes.NOT_FOUND, (key, value) ->
                    value.getContent() == null ? new ApiResponse().$ref(NOT_FOUND.name()) : value);
            return operation;
        };
    }

    private ApiResponse badRequestApiResponse() {
        final var apiError = ApiError.builder()
                .code(ErrorCodes.CONSTRAINT_VIOLATION.getCode())
                .message("Constraint violation")
                .build();
        apiError.addDetails(ApiValidationError.builder()
                .message("must not be blank")
                .field("param_name")
                .build());
        return new ApiResponse()
                .description(BAD_REQUEST.getReasonPhrase())
                .content(new Content().addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                        new MediaType()
                                .schema(new ObjectSchema().$ref(REST_RESPONSE_VOID))
                                .example(RestResponse.fail(apiError))));
    }

    /**
     * Must be consistent with {@link com.tesco.pma.configuration.security.CustomBearerTokenAuthenticationEntryPoint#commence}.
     *
     * @return unauthorized api response.
     * @see WebSecurityConfiguration
     */
    private ApiResponse unauthorizedApiResponse() {
        final ApiError exampleContent = ApiError.builder()
                .code(UNAUTHENTICATED.getCode())
                .message(messages.getMessage(UNAUTHENTICATED))
                .build();
        exampleContent.addDetails(ApiError.builder()
                .target("auth-provider-name")
                .message("Detailed auth error message")
                .build());
        return new ApiResponse()
                .description(UNAUTHORIZED.getReasonPhrase())
                .addHeaderObject(HttpHeaders.WWW_AUTHENTICATE, new Header()
                        .schema(new StringSchema()
                                .description("https://tools.ietf.org/html/rfc6750#section-3.1")
                                .example(UNAUTHENTICATED_HEADER_EXAMPLE)))
                .content(new Content().addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                        new MediaType().schema(new ObjectSchema().$ref(REST_RESPONSE_VOID))
                                .example(RestResponse.fail(exampleContent))));
    }

    /**
     * Must be consistent with {@link com.tesco.pma.error.ApiExceptionHandler#handleAccessDeniedException}.
     *
     * @return forbidden api response.
     */
    private ApiResponse forbiddenApiResponse() {
        return new ApiResponse()
                .description(FORBIDDEN.getReasonPhrase())
                .addHeaderObject(HttpHeaders.WWW_AUTHENTICATE, new Header()
                        .schema(new StringSchema()
                                .description("https://tools.ietf.org/html/rfc6750#section-3.1")
                                .example(FORBIDDEN_HEADER_EXAMPLE)))
                .content(new Content().addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                        new MediaType().schema(new ObjectSchema().$ref(REST_RESPONSE_VOID))
                                .example(RestResponse.fail(ApiError.builder()
                                        .code(ACCESS_DENIED.getCode())
                                        .message(messages.getMessage(ACCESS_DENIED))
                                        .build()))));
    }

    private ApiResponse internalServerErrorApiResponse() {
        return new ApiResponse()
                .description(INTERNAL_SERVER_ERROR.getReasonPhrase())
                .content(new Content().addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                        new MediaType().schema(new ObjectSchema().$ref(REST_RESPONSE_VOID))
                                .example(RestResponse.fail(ApiError.builder()
                                        .code(ErrorCodes.ER_CODE_UNEXPECTED_EXCEPTION.getCode())
                                        .message("Unexpected internal service error")
                                        .build()))));
    }

    private ApiResponse notFoundApiResponse() {
        return new ApiResponse()
                .description(NOT_FOUND.getReasonPhrase())
                .content(new Content().addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                        new MediaType().schema(new ObjectSchema().$ref(REST_RESPONSE_VOID))
                                .example(RestResponse.fail(ApiError.builder()
                                        .code("ENTITY_NOT_FOUND")
                                        .message("Entity was not found for parameter")
                                        .build()))));
    }
}
