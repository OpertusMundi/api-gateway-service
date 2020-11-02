package eu.opertusmundi.web.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.filter.CompositeFilter;

import eu.opertusmundi.common.model.EnumAuthProvider;
import eu.opertusmundi.web.logging.filter.MappedDiagnosticContextFilter;
import eu.opertusmundi.web.model.security.ClientResources;
import eu.opertusmundi.web.repository.AccountRepository;
import eu.opertusmundi.web.security.CustomUserDetailsService;
import eu.opertusmundi.web.security.CustomUserInfoTokenServices;
import eu.opertusmundi.web.security.UserService;

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true)
@EnableOAuth2Client
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${springdoc.api-docs.path}")
    private String openApiSpec;

    @Value("${opertus-mundi.oauth.callback-uri:/callback}")
    private String callbackUri;

    @Value("${opertus-mundi.oauth.failure-uri:/error/401}")
    private String failureUri;

    @Value("${opertus-mundi.security.csrf-enabled:true}")
    private boolean csrfEnabled;

    private static final String ACTION_REG_EX = "/action/.*";

    private final Pattern csrfMethods = Pattern.compile("^(POST|PUT|DELETE)$");

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    @Qualifier("defaultUserDetailsService")
    CustomUserDetailsService userDetailsService;

    @Autowired
    UserService userService;

    @Autowired
    OAuth2ClientContext oauth2ClientContext;

    @Autowired
    OAuthUserInfoDetailResolver userInfoDetailResolver;

    @Bean
    public SimpleUrlAuthenticationFailureHandler authenticationFailureHandler() {
        final SimpleUrlAuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();
        failureHandler.setUseForward(true);
        failureHandler.setDefaultFailureUrl(this.failureUri);
        return failureHandler;
    }

    /**
     * Returns the authentication manager currently used by Spring. It represents a
     * bean definition with the aim allow wiring from other classes performing the
     * Inversion of Control (IoC).
     *
     * @throws Exception
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public RequestContextListener requestContextListener() {
        return new RequestContextListener();
    }

    public SimpleUrlAuthenticationFailureHandler oauth2FailureHandler() {
        final SimpleUrlAuthenticationFailureHandler handler = new SimpleUrlAuthenticationFailureHandler();
        handler.setDefaultFailureUrl(this.failureUri);
        return handler;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        final LinkedHashMap<RequestMatcher, AuthenticationEntryPoint> map = new LinkedHashMap<RequestMatcher, AuthenticationEntryPoint>();

        map.put(new RegexRequestMatcher(ACTION_REG_EX, null), new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));

        final DelegatingAuthenticationEntryPoint entryPoint = new DelegatingAuthenticationEntryPoint(map);
        entryPoint.setDefaultEntryPoint(new LoginUrlAuthenticationEntryPoint("/"));

        return entryPoint;
    }

    @Override
    public void configure(WebSecurity security) throws Exception {
        security.ignoring()
            .antMatchers(
                // Client SPA application assets
                "/assets/**",
                // Swagger UI
                "/swagger-ui/**",
                // ReDoc Open API documentation viewer
                "/docs",
                // Open API specification
                this.openApiSpec,
                this.openApiSpec + "/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Configure request authentication
        http.authorizeRequests()
            // Public
            .antMatchers(
                // Application entry point
                "/",
                // Login
                "/login",
                "/login/*",
                "/logged-out",
                // Redirect pages
                "/token/verify",
                // Errors
                "/error/**",
                // Actions
                "/action/**"
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

        http.formLogin()
            .loginPage("/login")
            .failureUrl(this.failureUri)
            .defaultSuccessUrl("/logged-in", true)
            .usernameParameter("username")
            .passwordParameter("password");

        http.logout()
            .logoutUrl("/logout")
            .logoutSuccessUrl("/logged-out")
            .invalidateHttpSession(true)
            .clearAuthentication(true)
            .permitAll();

        http.addFilterBefore(this.oauth2Filter(), BasicAuthenticationFilter.class);
        http.addFilterAfter(new MappedDiagnosticContextFilter(), SwitchUserFilter.class);
    }

    private Filter oauth2Filter() {
        final CompositeFilter filter = new CompositeFilter();
        final List<Filter> filters = new ArrayList<>();

        filters.add(this.oauth2Filter(EnumAuthProvider.Google, this.google(), "/login/google"));
        filters.add(this.oauth2Filter(EnumAuthProvider.GitHub, this.github(), "/login/github"));
        filters.add(this.oauth2Filter(EnumAuthProvider.OpertusMundi, this.opertusMundi(), "/login/opertus-mundi"));
        filter.setFilters(filters);

        return filter;
    }

    private Filter oauth2Filter(EnumAuthProvider provider, ClientResources client, String path) {
        final OAuth2ClientAuthenticationProcessingFilter filter = new OAuth2ClientAuthenticationProcessingFilter(path);

        final OAuth2RestTemplate template = new OAuth2RestTemplate(client.getClient(), this.oauth2ClientContext);

        final CustomUserInfoTokenServices tokenServices = new CustomUserInfoTokenServices(
            provider,
            client.getUserInfoEndpoint(),
            client.getClient().getClientId(),
            this.userService,
            this.userInfoDetailResolver);

        tokenServices.setRestTemplate(template);

        filter.setRestTemplate(template);
        filter.setTokenServices(tokenServices);

        filter.setAuthenticationSuccessHandler(new SimpleUrlAuthenticationSuccessHandler(this.callbackUri));
        filter.setAuthenticationFailureHandler(this.oauth2FailureHandler());

        return filter;
    }

    @Bean
    @ConfigurationProperties("github")
    public ClientResources github() {
        return new ClientResources();
    }

    @Bean
    @ConfigurationProperties("google")
    public ClientResources google() {
        return new ClientResources();
    }

    @Bean
    @ConfigurationProperties("opertus-mundi")
    public ClientResources opertusMundi() {
        return new ClientResources();
    }

    @Bean
    public FilterRegistrationBean<OAuth2ClientContextFilter> oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
        final FilterRegistrationBean<OAuth2ClientContextFilter> registration = new FilterRegistrationBean<OAuth2ClientContextFilter>();
        registration.setFilter(filter);
        registration.setOrder(-100);
        return registration;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder builder) throws Exception {
        builder.userDetailsService(this.userDetailsService).passwordEncoder(new BCryptPasswordEncoder());
        builder.eraseCredentials(true);
    }

}