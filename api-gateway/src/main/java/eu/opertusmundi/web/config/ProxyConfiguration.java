package eu.opertusmundi.web.config;

import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import eu.opertusmundi.web.zuul.filter.CatalogueResponseBodyFilter;

@Profile("experimental")
@EnableZuulProxy
@Configuration
public class ProxyConfiguration {

    @Bean
    public CatalogueResponseBodyFilter catalogueResponseBodyFilter() {
        return new CatalogueResponseBodyFilter();
    }

}
