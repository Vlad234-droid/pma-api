package com.tesco.pma.configuration;

import com.tesco.pma.configuration.security.AdditionalAuthProperties;
import com.tesco.pma.configuration.security.AdditionalBearerTokenAuthenticationFilter;
import com.tesco.pma.configuration.security.AppendGrantedAuthoritiesBearerTokenAuthenticationMerger;
import com.tesco.pma.configuration.security.CustomBearerTokenAuthenticationEntryPoint;
import com.tesco.pma.configuration.security.CustomJwtBearerTokenAuthenticationConverter;
import com.tesco.pma.configuration.security.GroupsClaimGrantedAuthoritiesConverter;
import com.tesco.pma.configuration.security.IdentityOpaqueTokenIntrospector;
import com.tesco.pma.configuration.security.StaticAuthenticationManagerResolver;
import com.tesco.pma.configuration.web.client.BearerAuthorizationClientHttpRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.mapping.Attributes2GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.MapBasedAttributes2GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtBearerTokenAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import org.springframework.security.oauth2.server.resource.web.HeaderBearerTokenResolver;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Security configuration.
 * Currently supports JWT and old Identity V3 tokens as a fallback.
 *
 * <p>Also perform additional auth to retrieve AD groups for authorization proposes.
 *
 * @see <a href='https://github.dev.global.tesco.org/pages/Customer-API-Platform/jwt-onboarding-documentation'>Tesco jwt docs</a>
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
    public static final String IDENTITY_REALM_NAME = "Identity";

    @Autowired
    private OAuth2ResourceServerProperties resourceServerProperties;

    @Autowired
    private OpaqueTokenIntrospector identityOpaqueTokenIntrospector;

    @Autowired
    @Qualifier("identityAuthenticationEntryPoint")
    private AuthenticationEntryPoint identityAuthenticationEntryPoint;

    @Autowired(required = false)
    private AdditionalBearerTokenAuthenticationFilter oneLoginFilter;

    @Value("${tesco.application.security.enabled:true}")
    private boolean securityEnabled;

    private static final String[] AUTH_WHITELIST = {
            // -- Swagger UI v3 (OpenAPI)
            "/api-docs*/**",
            "/swagger-ui*/**",
            // other public endpoints of the API may be appended to this array
    };

    private static final String[] UNAUTHENTICATED_ANT_MATCHERS = {
            "/_working",
            "/live",
            "/_ready",
            "/hc",
            "/test/colleagues*/**" //TODO:: remove when we will have service users on ppe
    };

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests(authorizeRequests ->
                authorizeRequests
                        // unsecured resources
                        .antMatchers(AUTH_WHITELIST).permitAll()
                        // remained
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
                        .authenticationManagerResolver(authenticationManagerResolver())
                        .authenticationEntryPoint(identityAuthenticationEntryPoint));

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.csrf().disable();

        if (oneLoginFilter != null) {
            http.addFilterAfter(oneLoginFilter, BearerTokenAuthenticationFilter.class);
        }
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        if (securityEnabled) {
            web.ignoring().antMatchers(UNAUTHENTICATED_ANT_MATCHERS);
        } else {
            web.ignoring().anyRequest();
        }
    }

    @Bean
    public AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver() {
        //first try parse and validate as jwt token. if fail - try validate on identity validate/introspect endpoint directly.
        return new StaticAuthenticationManagerResolver(
                new ProviderManager(identityJwtAuthenticationProvider(), identityOpaqueTokenAuthenticationProvider()));
    }

    @Bean
    public JwtAuthenticationProvider identityJwtAuthenticationProvider() {
        final var jwtAuthenticationProvider = new JwtAuthenticationProvider(identityJwtDecoder());
        jwtAuthenticationProvider.setJwtAuthenticationConverter(new JwtBearerTokenAuthenticationConverter());
        return jwtAuthenticationProvider;
    }

    @Bean
    public JwtDecoder identityJwtDecoder() {
        final var jwtProperties = resourceServerProperties.getJwt();
        var nimbusJwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwtProperties.getJwkSetUri())
                .jwsAlgorithm(SignatureAlgorithm.from(jwtProperties.getJwsAlgorithm())).build();
        String issuerUri = jwtProperties.getIssuerUri();
        if (issuerUri != null) {
            nimbusJwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuerUri));
        }
        return nimbusJwtDecoder;
    }

    @Bean
    public OpaqueTokenAuthenticationProvider identityOpaqueTokenAuthenticationProvider() {
        return new OpaqueTokenAuthenticationProvider(identityOpaqueTokenIntrospector);
    }

    @Bean
    public OpaqueTokenIntrospector identityOpaqueTokenIntrospector(RestTemplateBuilder restTemplateBuilder) {
        final var restTemplate = restTemplateBuilder.build();
        //remove BearerAuthorizationClientHttpRequestInterceptor if present
        restTemplate.getInterceptors().removeIf(interceptor -> interceptor instanceof BearerAuthorizationClientHttpRequestInterceptor);
        return new IdentityOpaqueTokenIntrospector(resourceServerProperties.getOpaquetoken().getIntrospectionUri(),
                resourceServerProperties.getOpaquetoken().getClientId(),
                resourceServerProperties.getOpaquetoken().getClientSecret(),
                restTemplate);
    }

    @Bean
    public AuthenticationEntryPoint identityAuthenticationEntryPoint(MappingJackson2HttpMessageConverter messageConverter,
                                                                     NamedMessageSourceAccessor messages) {
        final var authenticationEntryPoint = new CustomBearerTokenAuthenticationEntryPoint(messageConverter, messages);
        authenticationEntryPoint.setRealmName(IDENTITY_REALM_NAME);
        return authenticationEntryPoint;
    }

    @Configuration
    public static class RolesMappingConfiguration {

        @Bean
        public Attributes2GrantedAuthoritiesMapper rolesMapper() {
            final var mapper = new MapBasedAttributes2GrantedAuthoritiesMapper();
            final var mapping = rolesMapping();
            //invert
            final var attributeToRoles = new HashMap<String, List<String>>();
            for (Map.Entry<String, List<String>> entry : mapping.entrySet()) {
                for (String attribute : entry.getValue()) {
                    attributeToRoles.computeIfAbsent(attribute, attr -> new ArrayList<>()).add(entry.getKey());
                }
            }
            mapper.setAttributes2grantedAuthoritiesMap(attributeToRoles);
            return mapper;
        }

        @Bean
        @ConfigurationProperties("tesco.application.security.role-mapping")
        public Map<String, List<String>> rolesMapping() {
            return new HashMap<>();
        }
    }

    @Configuration
    @ConditionalOnProperty(name = "tesco.application.security.additional-auth.enabled", havingValue = "true", matchIfMissing = true)
    public static class OneLoginSecurityConfiguration {
        private static final String ONELOGIN_REALM_NAME = "OneLogin";

        @Bean
        public AdditionalBearerTokenAuthenticationFilter oneLoginBearerTokenAuthenticationFilter(
                @Qualifier("oneLoginAuthenticationEntryPoint") AuthenticationEntryPoint oneLoginAuthenticationEntryPoint,
                @Qualifier("oneLoginJwt") JwtAuthenticationProvider oneLoginJwt) {

            final var filter = new AdditionalBearerTokenAuthenticationFilter(oneLoginJwt::authenticate);

            final var bearerTokenResolver = new HeaderBearerTokenResolver(additionalAuthProperties().getTokenHeaderName());
            filter.setBearerTokenResolver(bearerTokenResolver);

            filter.setAuthenticationMerger(new AppendGrantedAuthoritiesBearerTokenAuthenticationMerger());
            filter.setAuthenticationEntryPoint(oneLoginAuthenticationEntryPoint);
            return filter;
        }

        @Bean
        public JwtAuthenticationProvider oneLoginJwt(Attributes2GrantedAuthoritiesMapper rolesMapper) {
            final var provider = new JwtAuthenticationProvider(oneLoginJwtDecoder());

            final var jwtAuthenticationConverter = new CustomJwtBearerTokenAuthenticationConverter();
            final var oneLoginGroupsGrantedAuthoritiesConverter = new GroupsClaimGrantedAuthoritiesConverter();
            oneLoginGroupsGrantedAuthoritiesConverter.setAttributes2GrantedAuthoritiesMapper(rolesMapper);
            jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(oneLoginGroupsGrantedAuthoritiesConverter);

            provider.setJwtAuthenticationConverter(jwtAuthenticationConverter);
            return provider;
        }

        @Bean
        public JwtDecoder oneLoginJwtDecoder() {
            final var jwtProperties = additionalAuthProperties().getJwt();
            var nimbusJwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwtProperties.getJwkSetUri())
                    .jwsAlgorithm(SignatureAlgorithm.from(jwtProperties.getJwsAlgorithm()))
                    .build();
            String issuerUri = jwtProperties.getIssuerUri();
            if (issuerUri != null) {
                nimbusJwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuerUri));
            }
            return nimbusJwtDecoder;
        }

        @Bean
        public AuthenticationEntryPoint oneLoginAuthenticationEntryPoint(MappingJackson2HttpMessageConverter messageConverter,
                                                                         NamedMessageSourceAccessor messages) {
            final var authenticationEntryPoint = new CustomBearerTokenAuthenticationEntryPoint(messageConverter, messages);
            authenticationEntryPoint.setRealmName(ONELOGIN_REALM_NAME);
            return authenticationEntryPoint;
        }

        @Bean
        @ConfigurationProperties("tesco.application.security.additional-auth")
        public AdditionalAuthProperties additionalAuthProperties() {
            return new AdditionalAuthProperties();
        }

    }

}
