package eu.opertusmundi.web.config;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import eu.opertusmundi.common.model.EnumAuthProvider;
import eu.opertusmundi.web.logging.filter.MappedDiagnosticContextFilter;
import eu.opertusmundi.web.security.CustomUserDetailsService;

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true)
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final Logger oauth2Logger = LoggerFactory.getLogger("OAUTH");

    @Value("${springdoc.api-docs.path}")
    private String openApiSpec;

    @Value("${opertus-mundi.security.csrf-enabled:true}")
    private boolean csrfEnabled;

    @Value("${opertus-mundi.authentication-providers:forms}")
    private String authProviders;

    private static final String ACTION_REG_EX = "/action/.*";

    private final Pattern csrfMethods = Pattern.compile("^(POST|PUT|DELETE)$");

    @Autowired
    @Qualifier("defaultUserDetailsService")
    private CustomUserDetailsService userDetailsService;

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        final LinkedHashMap<RequestMatcher, AuthenticationEntryPoint> map = new LinkedHashMap<RequestMatcher, AuthenticationEntryPoint>();

        map.put(new RegexRequestMatcher(ACTION_REG_EX, null), new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));

        final DelegatingAuthenticationEntryPoint entryPoint = new DelegatingAuthenticationEntryPoint(map);
        entryPoint.setDefaultEntryPoint(new LoginUrlAuthenticationEntryPoint("/"));

        return entryPoint;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Configure request authentication
        http.authorizeRequests()
            // Restrict access to actuator endpoints (you may further restrict details via configuration)
            .requestMatchers(EndpointRequest.toAnyEndpoint()).hasIpAddress("127.0.0.1/8")
            // Public
            .antMatchers(
                // Permit all endpoints. Actions are secured using
                // annotations
                "/**"
             ).permitAll()
            // Secure any other path
            .anyRequest().authenticated();

        http.csrf().requireCsrfProtectionMatcher((HttpServletRequest req) -> {
            // Include all state-changing methods
            if (this.csrfMethods.matcher(req.getMethod()).matches()) {
                // Enable CSRF with configuration
                return this.csrfEnabled;
            }

            return false;
        });

        http.exceptionHandling().authenticationEntryPoint(this.authenticationEntryPoint());

        final List<EnumAuthProvider> providers = Arrays.stream(this.authProviders.split(","))
            .map(String::trim)
            .map(EnumAuthProvider::fromString)
            .filter(s -> s != null)
            .collect(Collectors.toList());

        // Enable forms authentication
        if (providers.contains(EnumAuthProvider.Forms)) {
            http.formLogin()
                .loginPage("/login")
                .failureUrl("/signin?error=1")
                .defaultSuccessUrl("/logged-in", true)
                .usernameParameter("username")
                .passwordParameter("password");
        }

        http.logout()
            .logoutUrl("/logout")
            .logoutSuccessUrl("/logged-out")
            .invalidateHttpSession(true)
            .clearAuthentication(true)
            .permitAll();

        // Enable OAuth2
        if (providers.contains(EnumAuthProvider.OpertusMundi)) {
            http.oauth2Login()
                .userInfoEndpoint(userInfo -> userInfo.oidcUserService(this.oidcUserService()))
                .failureHandler((HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) -> {
                    oauth2Logger.error("OAuth2 request failed", exception);

                    response.sendRedirect("/signin?error=2");
                });
        }

        http.addFilterAfter(new MappedDiagnosticContextFilter(), SwitchUserFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder builder) throws Exception {
        builder.userDetailsService(this.userDetailsService).passwordEncoder(new BCryptPasswordEncoder());
        builder.eraseCredentials(true);
    }

    private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        final OidcUserService delegate = new OidcUserService();

        return (userRequest) -> {
            // Delegate to the default implementation for loading a user
            final OidcUser oidcUser = delegate.loadUser(userRequest);
            final String email = (String) oidcUser.getAttributes().get("email");
            final UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            return (OidcUser) userDetails;
        };
    }

}