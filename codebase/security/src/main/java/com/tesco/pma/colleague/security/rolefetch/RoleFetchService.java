package com.tesco.pma.colleague.security.rolefetch;

import java.util.Collection;
import java.util.UUID;

/**
 * Role fetch service.
 */
public interface RoleFetchService {

    Collection<String> findRolesInAccountStorage(UUID colleagueUuid);

}
