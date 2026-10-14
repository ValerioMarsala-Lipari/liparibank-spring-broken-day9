package com.lipari.bank.shared.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@Profile("prod")
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager =
            new CaffeineCacheManager("branches", "branchStats");
        manager.setCaffeine(
                Caffeine.newBuilder()
                        .maximumSize(100)
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .recordStats()
        );
        return manager;
    }
}
