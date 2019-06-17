package com.rbkmoney.cashreg.configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.rbkmoney.cashreg.model.StatusPolling;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CaffeineConfiguration {

    @Bean
    Cache<String, StatusPolling> cacheCaffeine(
            @Value("${caffeine.expire_after_write:90}") long expireAfterWrite,
            @Value("${caffeine.maximum_size:1000}") long maximumSize
    ) {
        return Caffeine.newBuilder()
                .expireAfterWrite(expireAfterWrite, TimeUnit.MINUTES)
                .maximumSize(maximumSize)
                .build();
    }

}
