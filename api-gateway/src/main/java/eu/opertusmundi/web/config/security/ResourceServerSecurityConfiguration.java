package eu.opertusmundi.web.config.security;

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

import com.google.common.collect.ImmutableList;
import com.nimbusds.oauth2.sdk.util.StringUtils;

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
                configuration.setAllowedOrigins(ImmutableList.copyOf(allowedOrigins.split(",")));
                configuration.setAllowedMethods(ImmutableList.of("GET"));
                configuration.setAllowedHeaders(ImmutableList.of(
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
