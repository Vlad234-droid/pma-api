package com.tesco.pma.colleague.security.configuration;

import com.tesco.pma.colleague.security.rolefetch.RoleFetchFilter;
import com.tesco.pma.colleague.security.rolefetch.RoleFetchService;
import com.tesco.pma.colleague.security.rolefetch.RolesMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class RoleFetchConfiguration {

    private static final int ROLE_FETCH_FILTER_PRECEDENCE = 103;
    private static final String[] ROLE_FETCH_URL_PATTERNS = {"/*"};


    /**
     * Register the servlet filter for fetching roles from local storage
     *
     * @return FilterRegistrationBean
     */
    @Bean
    @ConditionalOnProperty(name = "tesco.application.security.enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<RoleFetchFilter> roleFetchFilter(RoleFetchService roleFetchService) {
        FilterRegistrationBean<RoleFetchFilter> filterRegistration = new FilterRegistrationBean<>();
        filterRegistration.setName("pmaRoleFetchFilter");
        filterRegistration.setFilter(new RoleFetchFilter(roleFetchService));
        // Make sure the filter is registered after the Spring Security Filter Chain
        filterRegistration.setOrder(ROLE_FETCH_FILTER_PRECEDENCE);
        filterRegistration.addUrlPatterns(ROLE_FETCH_URL_PATTERNS);
        return filterRegistration;
    }

    /**
     * Register the servlet filter for processing configuration role mapping
     *
     * @return FilterRegistrationBean
     */
    @Bean
    public RolesMapper accountRolesMapper(@NotNull final Map<String, List<String>> rolesMapping) {
        final var mapper = new RolesMapper();

        // Invert mapping : from "PMA role" -> "Tesco group" to "Tesco group" -> "PMA role"
        final var attributeToRole = new HashMap<String, String>();
        for (Map.Entry<String, List<String>> entry : rolesMapping.entrySet()) {
            for (String attribute : entry.getValue()) {
                attributeToRole.putIfAbsent(attribute, entry.getKey());
            }
        }
        mapper.setRolesMapping(attributeToRole);

        return mapper;
    }

}
