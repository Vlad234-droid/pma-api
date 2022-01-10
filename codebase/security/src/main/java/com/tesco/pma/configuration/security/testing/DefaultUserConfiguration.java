package com.tesco.pma.configuration.security.testing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class DefaultUserConfiguration {

    @Value("${tesco.application.security.default-user.uuid:10000000-1000-1000-1000-100000000001}")
    private String defaultUserUuid;

    private static final int DEFAULT_USER_FILTER_PRECEDENCE = 107;
    private static final String[] DEFAULT_USER_URL_PATTERNS = {"/*"};

    /**
     * Register the servlet filter for setting default user authentication
     *
     * @return FilterRegistrationBean
     */
    @Bean
    @ConditionalOnProperty(name = "tesco.application.security.enabled", havingValue = "false")
    public FilterRegistrationBean<DefaultUserFilter> defaultUserFilter() {
        FilterRegistrationBean<DefaultUserFilter> filterRegistration = new FilterRegistrationBean<>();
        filterRegistration.setName("pmaDefaultUserFilter");
        filterRegistration.setFilter(new DefaultUserFilter(createDefaultUserAuthentication()));
        // Make sure the filter is registered after the Spring Security Filter Chain
        filterRegistration.setOrder(DEFAULT_USER_FILTER_PRECEDENCE);
        filterRegistration.addUrlPatterns(DEFAULT_USER_URL_PATTERNS);
        return filterRegistration;
    }

    private Authentication createDefaultUserAuthentication() {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        var user = new User(defaultUserUuid, "password", grantedAuthorities);
        return new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
    }

}
