package com.tesco.pma.validation;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProvider;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.Configuration;

/**
 * Hibernate specific LocalValidatorFactoryBean.
 */
public class AppLocalValidatorFactoryBean extends LocalValidatorFactoryBean {

    private PropertyNodeNameProvider propertyNodeNameProvider;

    @Override
    protected void postProcessConfiguration(Configuration<?> configuration) {

        super.postProcessConfiguration(configuration);

        if (configuration instanceof HibernateValidatorConfiguration) {
            var hibernateConfiguration = (HibernateValidatorConfiguration) configuration;

            if (propertyNodeNameProvider != null) {
                hibernateConfiguration.propertyNodeNameProvider(propertyNodeNameProvider);
            }
        }
    }

    public void setPropertyNodeNameProvider(PropertyNodeNameProvider propertyNodeNameProvider) {
        this.propertyNodeNameProvider = propertyNodeNameProvider;
    }
}
