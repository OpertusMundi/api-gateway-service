package eu.opertusmundi.web.feign.client.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.auth.BasicAuthRequestInterceptor;

@Configuration
public class RatingServiceFeignClientConfiguration {

    @Value("${opertusmundi.feign.rating-service.basic-auth.username}")
    private String username;

    @Value("${opertusmundi.feign.rating-service.basic-auth.password}")
    private String password;

    @Bean
    public BasicAuthRequestInterceptor authInterceptor() {
        return new BasicAuthRequestInterceptor(this.username, this.password);
    }

}
