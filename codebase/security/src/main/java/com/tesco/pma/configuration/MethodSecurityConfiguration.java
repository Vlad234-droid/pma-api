package com.tesco.pma.configuration;

import com.tesco.pma.colleague.profile.service.ProfileService;
import com.tesco.pma.configuration.security.PmaMethodSecurityExpressionOperations;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.core.Authentication;

/**
 * Configuration for authorization.
 *
 * @see PmaMethodSecurityExpressionOperations
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "tesco.application.security.enabled", havingValue = "true", matchIfMissing = true)
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfiguration extends GlobalMethodSecurityConfiguration {

    private ObjectProvider<ProfileService> profileServiceObjectProvider;

    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        return new DefaultMethodSecurityExpressionHandler() {
            @Override
            protected MethodSecurityExpressionOperations createSecurityExpressionRoot(Authentication authentication,
                                                                                      MethodInvocation invocation) {
                return new PmaMethodSecurityExpressionOperations(
                        super.createSecurityExpressionRoot(authentication, invocation),
                        profileServiceObjectProvider.getObject());
            }
        };
    }

    /**
     * Inject BeanFactory in MethodSecurityExpressionHandler.
     * Needed to use beans in expressions.
     *
     * @param objectPostProcessor objectPostProcessor
     */
    @Override
    @Autowired(required = false)
    public void setObjectPostProcessor(ObjectPostProcessor<Object> objectPostProcessor) {
        super.setObjectPostProcessor(objectPostProcessor);
        objectPostProcessor.postProcess(getExpressionHandler());
    }

    @Autowired
    public void setProfileServiceObjectProvider(ObjectProvider<ProfileService> profileServiceObjectProvider) {
        this.profileServiceObjectProvider = profileServiceObjectProvider;
    }

}
