package eu.opertusmundi.web.config.security;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import eu.opertusmundi.web.logging.filter.MappedDiagnosticContextFilter;
import eu.opertusmundi.web.security.CustomJwtAuthenticationConverter;
import eu.opertusmundi.web.security.CustomUserDetailsService;

@Configuration
public class ResourceServerSecurityConfiguration {

    private static final String API_URL_PATTERN = "/api/**";

    @Value("${opertusmundi.web.cors.allowed-origins:}")
    private String allowedOrigins;

    @Autowired
    @Qualifier("defaultUserDetailsService")
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private CustomJwtAuthenticationConverter jwtAuthenticationConverter;

    @Bean
    @Order(1)
    public SecurityFilterChain resourceServerFilterChain(HttpSecurity http) throws Exception {
        http
            .antMatcher(API_URL_PATTERN)
            .authorizeRequests()
                .anyRequest()
                .authenticated();

        http
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                .jwtAuthenticationConverter(jwtAuthenticationConverter)
            ));

        if (!StringUtils.isBlank(allowedOrigins)) {
            http.cors(cors -> {
                final CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
                configuration.setAllowedMethods(List.of("GET", "POST"));
                configuration.setAllowedHeaders(List.of(
                    "authorization",
                    "cache-control",
                    "content-type"
                ));
                configuration.setAllowCredentials(true);

                final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration(API_URL_PATTERN, configuration);

                cors.configurationSource(source);
            });
        }


        http.addFilterAfter(new MappedDiagnosticContextFilter(), SwitchUserFilter.class);

        return http.build();
    }

}
