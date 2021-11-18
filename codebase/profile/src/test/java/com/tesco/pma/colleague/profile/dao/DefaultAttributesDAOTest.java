package com.tesco.pma.colleague.profile.dao;

import com.tesco.pma.colleague.profile.domain.DefaultAttribute;
import com.tesco.pma.colleague.profile.domain.DefaultAttributeCategory;
import com.tesco.pma.colleague.profile.domain.DefaultAttributeCriteria;
import com.tesco.pma.dao.AbstractDAOTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultAttributesDAOTest extends AbstractDAOTest {


    @Autowired
    private DefaultAttributesDAO defaultAttributesDAO;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.default.jdbc-url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.default.password", CONTAINER::getPassword);
        registry.add("spring.datasource.default.username", CONTAINER::getUsername);
    }

    @Test
    public void findByCriteriaAndCategoryTest() {
        var attrs = defaultAttributesDAO.findByCriteriaAndCategory(DefaultAttributeCriteria.ALL_COLLEAGUES,
                DefaultAttributeCategory.NOTIFICATION);

        assertTrue(attrs.size()>0);
    }

    @Test
    public void findByCriteriasAndCategoryTest() {
        var criterias = List.of(DefaultAttributeCriteria.ALL_COLLEAGUES, DefaultAttributeCriteria.WK_4_5_ONLY);

        var attrs = defaultAttributesDAO.findByCriteriasAndCategory(
                criterias, DefaultAttributeCategory.NOTIFICATION);

        assertTrue(attrs.size()>0);

        for(DefaultAttribute attribute : attrs){
            assertTrue(criterias.contains(attribute.getCriteria()));
        }
    }

}
