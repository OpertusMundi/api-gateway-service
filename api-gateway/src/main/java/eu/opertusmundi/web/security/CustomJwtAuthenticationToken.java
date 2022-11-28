package eu.opertusmundi.web.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import eu.opertusmundi.web.model.security.User;
import lombok.Getter;

public class CustomJwtAuthenticationToken extends JwtAuthenticationToken {

    private static final long serialVersionUID = 1L;

    @Getter
    private final User principal;

    public CustomJwtAuthenticationToken(Jwt jwt, Collection<? extends GrantedAuthority> authorities, String name, User principal) {
        super(jwt, authorities, name);

        this.principal = principal;
    }

}
