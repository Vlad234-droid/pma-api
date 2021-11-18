package com.tesco.pma.colleague.security.rolefetch;

import java.util.Collection;
import java.util.UUID;

/**
 * Role fetch service.
 */
public interface RoleFetchService {

    /**
     * Try to find roles in account storage
     *
     * @param colleagueUuid
     * @return List of roles
     */
    Collection<String> findRolesInAccountStorage(UUID colleagueUuid);

}
