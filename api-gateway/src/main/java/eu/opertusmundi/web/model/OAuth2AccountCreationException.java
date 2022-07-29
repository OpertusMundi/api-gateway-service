package eu.opertusmundi.web.model;

import org.springframework.security.core.AuthenticationException;

public class OAuth2AccountCreationException extends AuthenticationException {

    private static final long serialVersionUID = 1L;

    public OAuth2AccountCreationException(String msg) {
        super(msg);
    }

}
