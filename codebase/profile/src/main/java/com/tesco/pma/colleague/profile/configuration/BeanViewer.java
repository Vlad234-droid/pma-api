package com.tesco.pma.colleague.profile.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;

//@Configuration
@Component
@SuppressWarnings("PMD.UseVarargs")
public class BeanViewer {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @EventListener
    public void showBeansRegistered(ApplicationReadyEvent event) {
        String[] beanNames = event.getApplicationContext().getBeanDefinitionNames();
        inspectBeans(beanNames);
    }

    //    @Bean
    //    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
    //        return args -> {
    //            String[] beanNames = ctx.getBeanDefinitionNames();
    //            inspectBeans(beanNames);
    //        };
    //    }

    private void inspectBeans(String[] beanNames) {
        logger.info(String.format("Let's inspect the %s beans provided by Spring Boot:", beanNames.length));
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            // if (beanName.endsWith("DAO")) {
            logger.info("{}", beanName);
            // }
        }
    }

}
