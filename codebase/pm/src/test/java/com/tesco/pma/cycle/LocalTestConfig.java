package com.tesco.pma.cycle;

import com.tesco.pma.configuration.MessageSourceConfig;
import com.tesco.pma.configuration.NamedMessageSourceAccessor;
import com.tesco.pma.util.ResourceProvider;
import com.tesco.pma.process.service.ClasspathResourceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.context.annotation.*;

@Profile("test")
@Configuration
@Import({MessageSourceAutoConfiguration.class,
        MessageSourceConfig.class})
public class LocalTestConfig {

    @Autowired
    private NamedMessageSourceAccessor messageSourceAccessor;

    @Bean
    public ResourceProvider classpathResourceProvider(){
        return new ClasspathResourceProvider(messageSourceAccessor);
    }

}
