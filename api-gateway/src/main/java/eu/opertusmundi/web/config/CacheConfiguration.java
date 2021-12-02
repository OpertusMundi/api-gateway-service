package eu.opertusmundi.web.config;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.github.benmanes.caffeine.cache.Caffeine;

@EnableCaching
@Configuration
public class CacheConfiguration {

    @Primary
    @Bean
    public CaffeineCacheManager defaultCacheManager() {
        final Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
            .recordStats()
            .expireAfterWrite(1L, TimeUnit.HOURS);

        final CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(caffeine);
        return cacheManager;
    }

}
