package com.tesco.pma.config.dao;

import com.tesco.pma.config.domain.DefaultAttribute;
import com.tesco.pma.config.domain.DefaultAttributeCategory;
import com.tesco.pma.config.domain.DefaultAttributeCriteria;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DefaultAttributesDAO {

    /**
     * Get list of default attributes by criteria and category
     *
     * @param criteria
     * @param category
     * @return list of default attributes
     */
    List<DefaultAttribute> findByCriteriaAndCategory(@Param("criteria") DefaultAttributeCriteria criteria,
                                                     @Param("category") DefaultAttributeCategory category);

    /**
     * Get list of default attributes by a list of criterias and category
     *
     * @param criterias
     * @param category
     * @return list of default attributes
     */
    List<DefaultAttribute> findByCriteriasAndCategory(@Param("criterias") List<DefaultAttributeCriteria> criterias,
                                                      @Param("category") DefaultAttributeCategory category);

}
