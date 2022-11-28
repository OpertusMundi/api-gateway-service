package eu.opertusmundi.web.security;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.web.model.security.User;

@Service
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final CustomUserDetailsService userDetailsService;

    @Autowired
    public CustomJwtAuthenticationConverter(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        final String email       = jwt.getClaim("email");
        final User   userDetails = (User) userDetailsService.loadUserByUsername(email);

        // Override roles for JWT authentication
        userDetails.getAccount().setRoles(Set.of(EnumRole.ROLE_API));

        final JwtAuthenticationToken authentication = new CustomJwtAuthenticationToken(jwt, userDetails.getAuthorities(), email, userDetails);

        return authentication;
    }

}
