package eu.opertusmundi.web.model.security;

import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration for an OAuth client
 */
public class ClientResources {

    @Getter
    private final AuthorizationCodeResourceDetails client = new AuthorizationCodeResourceDetails();

    /**
     * OpenId Connect user info endpoint
     */
    @Getter
    @Setter
    private String userInfoEndpoint;

}
