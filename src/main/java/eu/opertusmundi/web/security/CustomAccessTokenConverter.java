package eu.opertusmundi.web.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;

/**
 * Custom implementation of {@link AccessTokenConverter} that injects
 * authorities from a database.
 */
public class CustomAccessTokenConverter extends DefaultAccessTokenConverter {

    private final String scopeAttribute = SCOPE;

    private final String clientIdAttribute = "azp";

    private final String grantTypeAttribute = "typ";

    private final CustomUserDetailsService    userService;

    public CustomAccessTokenConverter(CustomUserDetailsService userService) {
        super();

        this.userService = userService;
    }

    @Override
    public OAuth2Authentication extractAuthentication(Map<String, ?> map) {
        final Map<String, String> parameters = new HashMap<String, String>();
        final Set<String> scope = this.extractScope(map);

        UsernamePasswordAuthenticationToken token;

        // Get user only if an email is present
        if (map.containsKey("email")) {
            final String email = (String) map.get("email");

            final UserDetails principal = this.userService.loadUserByUsername(email);

            token = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        } else {
            throw new UsernameNotFoundException("A valid email address is required.");
        }

        // Get client Id
        final String clientId = (String) map.get(this.clientIdAttribute);
        parameters.put(this.clientIdAttribute, clientId);

        // Get grant type
        final String grantType = (String) map.get(this.grantTypeAttribute);
        parameters.put(this.grantTypeAttribute, grantType);

        // Get audience
        final Set<String> resourceIds = new LinkedHashSet<String>(
            map.containsKey(AUD) ? this.getAudience(map) : Collections.<String>emptySet()
        );

        final OAuth2Request request = new OAuth2Request(
            parameters, clientId, token.getAuthorities(), true, scope, resourceIds, null, null, null
        );

        return new OAuth2Authentication(request, token);
    }


    private Collection<String> getAudience(Map<String, ?> map) {
        final Object auds = map.get(AUD);

        if (auds instanceof Collection) {
            @SuppressWarnings("unchecked")
            final Collection<String> result = (Collection<String>) auds;
            return result;
        }

        return Collections.singleton((String) auds);
    }

    private Set<String> extractScope(Map<String, ?> map) {
        Set<String> scope = Collections.emptySet();

        if (map.containsKey(this.scopeAttribute)) {
            final Object scopeObj = map.get(this.scopeAttribute);
            if (String.class.isInstance(scopeObj)) {
                scope = new LinkedHashSet<String>(Arrays.asList(String.class.cast(scopeObj).split(" ")));
            } else if (Collection.class.isAssignableFrom(scopeObj.getClass())) {
                // Preserve ordering
                @SuppressWarnings("unchecked")
                final Collection<String> scopeColl = (Collection<String>) scopeObj;
                scope = new LinkedHashSet<String>(scopeColl);
            }
        }
        return scope;
    }

}
