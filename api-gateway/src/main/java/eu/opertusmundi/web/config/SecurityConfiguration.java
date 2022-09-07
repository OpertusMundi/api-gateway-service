package eu.opertusmundi.web.config;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import eu.opertusmundi.common.model.EnumAuthProvider;
import eu.opertusmundi.common.model.account.AccountProfileCommandDto;
import eu.opertusmundi.common.model.account.ExternalIdpAccountCommand;
import eu.opertusmundi.web.logging.filter.MappedDiagnosticContextFilter;
import eu.opertusmundi.web.model.OAuth2ProviderNotSupportedException;
import eu.opertusmundi.web.security.CustomUserDetailsService;

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true)
@EnableWebSecurity
public class SecurityConfiguration {

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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        final AuthenticationManagerBuilder authManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(new BCryptPasswordEncoder());
        authManagerBuilder.eraseCredentials(true);
        final AuthenticationManager authenticationManager = authManagerBuilder.build();

        http.authenticationManager(authenticationManager);

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
                .userInfoEndpoint(userInfo -> userInfo
                    .oidcUserService(this.oidcUserService())
                    .userService(this.oauthUserService())
                )
                .failureHandler((HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) -> {
                    oauth2Logger.error("OAuth2 request failed", exception);

                    response.sendRedirect("/signin?error=2");
                });
        }

        http.addFilterAfter(new MappedDiagnosticContextFilter(), SwitchUserFilter.class);

        return http.build();
    }

    private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        final OidcUserService delegate = new OidcUserService();

        return (userRequest) -> {
            final String                    clientName  = userRequest.getClientRegistration().getClientName();
            final EnumAuthProvider          provider    = EnumAuthProvider.fromString(clientName);
            final OidcUser                  oidcUser    = delegate.loadUser(userRequest);
            final String                    email       = (String) oidcUser.getAttributes().get("email");
            final ExternalIdpAccountCommand command     = this.createCommand(provider, oidcUser.getAttributes());
            final UserDetails               userDetails = provider == EnumAuthProvider.OpertusMundi
                ? userDetailsService.loadUserByUsername(email)
                : userDetailsService.loadUserByUsername(email, provider, command);

            return (OidcUser) userDetails;
        };
    }

    private OAuth2UserService<OAuth2UserRequest, OAuth2User> oauthUserService() {
        final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

        return (userRequest) -> {
            final String                    clientName  = userRequest.getClientRegistration().getClientName();
            final EnumAuthProvider          provider    = EnumAuthProvider.fromString(clientName);
            final OAuth2User                oauthUser   = delegate.loadUser(userRequest);
            final String                    email       = (String) oauthUser.getAttributes().get("email");
            final ExternalIdpAccountCommand command     = this.createCommand(provider, oauthUser.getAttributes());
            final UserDetails               userDetails = userDetailsService.loadUserByUsername(email, provider, command);

            return (OAuth2User) userDetails;
        };
    }

    private ExternalIdpAccountCommand createCommand(EnumAuthProvider provider, Map<String, Object> attributes) {
        if (provider == null) {
            return null;
        }
        switch (provider) {
            case Google :
                return this.createCommandFromGoogleAttributes(attributes);
            case GitHub :
                return this.createCommandFromGitHubAttributes(attributes);
            case OpertusMundi :
                return null;
            default :
                throw new OAuth2ProviderNotSupportedException(String.format("Provider is not supported [provider=%s]", provider));
        }
    }

    private ExternalIdpAccountCommand createCommandFromGoogleAttributes(Map<String, Object> attributes) {
        final String                   email     = (String) attributes.get("email");
        final String                   firstName = (String) attributes.get("given_name");
        final String                   lastName  = (String) attributes.get("family_name");
        final AccountProfileCommandDto profile = AccountProfileCommandDto.builder()
            .firstName(firstName)
            .lastName(lastName)
            .build();

        final ExternalIdpAccountCommand command = ExternalIdpAccountCommand.builder()
            .idpName(EnumAuthProvider.Google)
            .email(email)
            .profile(profile)
            .build();

        return command;
    }

    private ExternalIdpAccountCommand createCommandFromGitHubAttributes(Map<String, Object> attributes) {
        final String                   email   = (String) attributes.get("email");
        final String                   name    = (String) attributes.get("name");
        final AccountProfileCommandDto profile = AccountProfileCommandDto.builder()
            .firstName(name)
            .lastName("")
            .build();

        final ExternalIdpAccountCommand command = ExternalIdpAccountCommand.builder()
            .idpName(EnumAuthProvider.GitHub)
            .email(email)
            .profile(profile)
            .build();

        return command;
    }
}