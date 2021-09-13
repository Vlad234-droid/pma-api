package com.tesco.pma.colleague.profile.configuration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableCaching
public class CacheConfig {

//    @Bean
//    public RedisCacheManagerBuilderCustomizer myRedisCacheManagerBuilderCustomizer() {
//        return (builder) -> builder
//                .withCacheConfiguration("colleagues", RedisCacheConfiguration
//                        .defaultCacheConfig().entryTtl(Duration.ofSeconds(10)))
//                .withCacheConfiguration("cache2", RedisCacheConfiguration
//                        .defaultCacheConfig().entryTtl(Duration.ofMinutes(1)));
//
//    }

}
