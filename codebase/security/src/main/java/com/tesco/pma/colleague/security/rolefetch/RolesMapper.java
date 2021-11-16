package com.tesco.pma.colleague.security.rolefetch;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Data
@NoArgsConstructor
public class RolesMapper {

    private Map<String, String> rolesMapping = new HashMap<>();

    public String findRoleByCode(String code) {
        return Optional.ofNullable(rolesMapping.get(code)).orElse("");
    }

}
