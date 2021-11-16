package com.tesco.pma.colleague.profile.dao;

import com.tesco.pma.colleague.profile.domain.DefaultAttribute;
import com.tesco.pma.colleague.profile.domain.DefaultAttributeCategory;
import com.tesco.pma.colleague.profile.domain.DefaultAttributeCriteria;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DefaultAttributesDAO {

    DefaultAttribute findByCriteriaAndCategory(@Param("criteria") DefaultAttributeCriteria criteria,
                                    @Param("category") DefaultAttributeCategory category);

    List<DefaultAttribute> findByCriteriasAndCategory(@Param("criterias") List<DefaultAttributeCriteria> criterias,
                                                @Param("category") DefaultAttributeCategory category);

}
