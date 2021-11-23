package com.tesco.pma.colleague.config.dao;

import com.tesco.pma.colleague.config.domain.DefaultAttribute;
import com.tesco.pma.colleague.config.domain.DefaultAttributeCategory;
import com.tesco.pma.colleague.config.domain.DefaultAttributeCriteria;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DefaultAttributesDAO {

    List<DefaultAttribute> findByCriteriaAndCategory(@Param("criteria") DefaultAttributeCriteria criteria,
                                    @Param("category") DefaultAttributeCategory category);

    List<DefaultAttribute> findByCriteriasAndCategory(@Param("criterias") List<DefaultAttributeCriteria> criterias,
                                                @Param("category") DefaultAttributeCategory category);

}
