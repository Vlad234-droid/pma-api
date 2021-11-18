package com.tesco.pma.colleague.security.rolefetch;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Processing configuration role mapping
 */
@Data
@NoArgsConstructor
public class RolesMapper {

    private Map<String, String> rolesMapping = new HashMap<>();

    /**
     *
     * @param code - Tesco group
     * @return PMA Role
     */
    public String findRoleByCode(String code) {
        return Optional.ofNullable(rolesMapping.get(code)).orElse("");
    }

}
