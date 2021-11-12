package com.tesco.pma.user.configuration;

import com.tesco.pma.user.util.RolesMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class UserConfiguration {

    @Configuration
    public static class RolesMapperConfiguration {

        @Bean
        public RolesMapper accountRolesMapper(@NotNull final Map<String, List<String>> rolesMapping) {
            final var mapper = new RolesMapper();

            // Invert
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

}
