package eu.opertusmundi.web.feign.client.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.opertusmundi.web.util.JwtUtils;

@Configuration
public class EmailServiceFeignClientConfiguration {

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${opertusmundi.feign.email-service.name}")
    private String alias;

    @Value("${opertusmundi.feign.email-service.jwt.subject}")
    private String subject;

    @Bean
    public JwtRequestInterceptor authInterceptor() throws Exception {
        final String token = this.jwtUtils.createToken(this.alias, this.subject);

        return new JwtRequestInterceptor(token);
    }

}
