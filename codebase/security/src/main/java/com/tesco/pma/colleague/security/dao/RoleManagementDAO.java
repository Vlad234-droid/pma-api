package com.tesco.pma.colleague.security.dao;

import com.tesco.pma.colleague.security.domain.Role;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

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
