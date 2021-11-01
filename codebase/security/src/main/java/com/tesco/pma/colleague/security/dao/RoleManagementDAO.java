package com.tesco.pma.colleague.security.dao;

import com.tesco.pma.colleague.security.domain.Role;

import java.util.List;

/**
 * Interface to perform database operation on {@link Role}.
 */
public interface RoleManagementDAO {

    /**
     * Returns an available access levels & metadata
     *
     * @return a list of roles
     */
    List<Role> get();

}
