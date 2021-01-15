package eu.opertusmundi.web.feign.client.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.opertusmundi.web.util.JwtUtils;

@Configuration
public class MessageServiceFeignClientConfiguration {

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${opertusmundi.feign.message-service.jwt.subject}")
    private String subject;

    @Bean
    public JwtRequestInterceptor authInterceptor() throws Exception {
        final String token = this.jwtUtils.createToken(this.subject);

        return new JwtRequestInterceptor(token);
    }

}
