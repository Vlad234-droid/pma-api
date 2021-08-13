package com.tesco.pma.bpm.camunda.util;

import static java.util.Objects.requireNonNull;

import org.springframework.context.ConfigurableApplicationContext;

public class ApplicationContextProvider {

    private static final String CHECK_NULL_MESSAGE =
            "ApplicationContextProvider must be initialized before using. Call initialize method.";

    private static ApplicationContextProvider instance;

    private final ConfigurableApplicationContext context;

    private ApplicationContextProvider(ConfigurableApplicationContext context) {
        requireNonNull(context, "context must be specified");
        this.context = context;
    }

    public static synchronized void initialize(ConfigurableApplicationContext context) { //NOPMD
        if (instance == null || !instance.context.isActive()) {
            instance = new ApplicationContextProvider(context);
        }
    }

    /**
     * Get a Spring bean by name.
     * @see org.springframework.beans.factory.BeanFactory#getBean(String)
     *
     * @param name bean nanme
     * @return bean instance
     */
    public static <T> T getBean(String name) {
        requireNonNull(instance, CHECK_NULL_MESSAGE);
        return (T) instance.context.getBean(name);
    }

    /**
     * Get a Spring bean by name and type.
     * @see org.springframework.beans.factory.BeanFactory#getBean(String, Class)
     * @param name bean name
     * @param requiredType bean type
     * @return bean instance
     */
    public static <T> T getBean(String name, Class<T> requiredType) {
        requireNonNull(instance, CHECK_NULL_MESSAGE);
        return instance.context.getBean(name, requiredType);
    }

    /**
     * Get a Spring bean by name. Allows for specifying explicit constructor arguments / factory method arguments, overriding the specified
     * default arguments (if any) in the bean definition.
     * @see org.springframework.beans.factory.BeanFactory#getBean(String, Object...)
     *
     * @param name bean name
     * @param args arguments
     * @return bean instance
     */
    public static <T> T getBean(String name, Object... args) {
        requireNonNull(instance, CHECK_NULL_MESSAGE);
        return (T) instance.context.getBean(name, args);
    }

    /**
     * Get a Spring bean by type.
     * @see org.springframework.beans.factory.BeanFactory#getBean(Class)
     * @param requiredType bean type
     * @return bean instance
     */
    public static <T> T getBean(Class<T> requiredType) {
        requireNonNull(instance, CHECK_NULL_MESSAGE);
        return instance.context.getBean(requiredType);
    }

    /**
     * Get a Spring bean by type. Allows for specifying explicit constructor arguments / factory method arguments, overriding the specified
     * default arguments (if any) in the bean definition.
     * @see org.springframework.beans.factory.BeanFactory#getBean(Class, Object...)
     * @param requiredType bean type
     * @param args arguments
     * @return bean instance
     */
    public static <T> T getBean(Class<T> requiredType, Object... args) {
        requireNonNull(instance, CHECK_NULL_MESSAGE);
        return instance.context.getBean(requiredType, args);
    }
}
