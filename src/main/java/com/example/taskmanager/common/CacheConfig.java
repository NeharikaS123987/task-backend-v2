package com.example.taskmanager.common;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Stage 4: Enable caching of frequently accessed data.
 * This uses Spring's in-memory ConcurrentMap cache by default.
 *
 * If you later add Redis to the project, you can replace the bean with a
 * RedisCacheManager and keep the same @Cacheable/@CacheEvict usage.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        // Cache names used across the app; add as needed.
        return new ConcurrentMapCacheManager(
                "boardDetails",
                "userProfiles",
                "searchResults"
        );
    }
}