package eu.opertusmundi.web.model;

import org.springframework.security.core.AuthenticationException;

public class OAuth2ProviderNotSupportedException extends AuthenticationException {

    private static final long serialVersionUID = 1L;

    public OAuth2ProviderNotSupportedException(String msg) {
        super(msg);
    }

}
