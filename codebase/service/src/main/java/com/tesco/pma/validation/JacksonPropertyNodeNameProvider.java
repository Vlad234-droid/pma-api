package com.tesco.pma.validation;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import org.hibernate.validator.spi.nodenameprovider.JavaBeanProperty;
import org.hibernate.validator.spi.nodenameprovider.Property;
import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Use Jackson property names.
 *
 * @see <a href="https://docs.jboss.org/hibernate/validator/6.1/reference/en-US/html_single/#section-property-node-name-provider">Explanation</a>
 */
public class JacksonPropertyNodeNameProvider implements PropertyNodeNameProvider {
    private final ObjectMapper objectMapper;
    private final Map<Class<?>, BeanDescription> cache = new ConcurrentHashMap<>();

    public JacksonPropertyNodeNameProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName(Property property) {
        if (property instanceof JavaBeanProperty) {
            return getJavaBeanPropertyName((JavaBeanProperty) property);
        }
        return getDefaultName(property);
    }

    private String getJavaBeanPropertyName(JavaBeanProperty property) {
        BeanDescription desc = cache.computeIfAbsent(property.getDeclaringClass(), aClass -> objectMapper.getSerializationConfig()
                .introspect(objectMapper.constructType(aClass)));

        return desc.findProperties()
                .stream()
                .filter(prop -> prop.getInternalName().equals(property.getName()))
                .map(BeanPropertyDefinition::getName)
                .findFirst()
                .orElse(property.getName());
    }

    private String getDefaultName(Property property) {
        return property.getName();
    }
}
