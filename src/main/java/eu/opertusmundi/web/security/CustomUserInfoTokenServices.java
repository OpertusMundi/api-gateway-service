package eu.opertusmundi.web.security;

import java.util.AbstractMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import eu.opertusmundi.common.model.EnumAuthProvider;
import eu.opertusmundi.common.model.dto.AccountCommandDto;
import eu.opertusmundi.common.model.dto.AccountDto;
import eu.opertusmundi.common.model.dto.AccountProfileCommandDto;
import eu.opertusmundi.web.config.OAuthUserInfoDetailResolver;
import eu.opertusmundi.web.model.security.User;

public class CustomUserInfoTokenServices extends UserInfoTokenServices {

    private final OAuthUserInfoDetailResolver userInfoDetailResolver;

    private final UserService userService;

    private final EnumAuthProvider provider;

    public CustomUserInfoTokenServices(
        EnumAuthProvider provider,
        String userInfoEndpointUrl,
        String clientId,
        UserService userService,
        OAuthUserInfoDetailResolver userInfoDetailResolver
    ) {
        super(userInfoEndpointUrl, clientId);

        this.provider = provider;
        this.userInfoDetailResolver = userInfoDetailResolver;
        this.userService = userService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public OAuth2Authentication loadAuthentication(String accessToken) throws AuthenticationException, InvalidTokenException {
        //  Load the credentials for the specified access token
        final OAuth2Authentication authentication = super.loadAuthentication(accessToken);

        // Create custom user details
        final AccountCommandDto command = new AccountCommandDto();
        command.setProfile(new AccountProfileCommandDto());

        // Identity provider addition
        String idpUserName = authentication.getPrincipal().toString();
        String idpUserImage = null;

        final AbstractMap<String, String> details = (AbstractMap<String, String>) authentication.getUserAuthentication().getDetails();

        for (final String key : details.keySet()) {
            final String property = this.userInfoDetailResolver.resolve(key);

            if (!StringUtils.isBlank(property)) {
                switch (property) {
                    case OAuthUserInfoDetailResolver.NAME_PROPERTY :
                        idpUserName = details.get(key);
                        break;
                    case OAuthUserInfoDetailResolver.EMAIL_PROPERTY :
                        command.setEmail(details.get(key));
                        break;
                    case OAuthUserInfoDetailResolver.IMAGE_PROPERTY :
                        idpUserImage = details.get(key);
                        break;
                    case OAuthUserInfoDetailResolver.LOCALE_PROPERTY :
                        command.getProfile().setLocale(details.get(key));
                        break;
                }
            }
        }

        // An email is required
        if (StringUtils.isBlank(command.getEmail())) {
            throw new UsernameNotFoundException("A valid email address is required.");
        }

        // Find account by email
        AccountDto account = this.userService.findOneByUserName(command.getEmail(), this.provider).orElse(null);

        // If the account does not exists, register the user
        if (account == null) {
            // Create user
            command.setActive(true);
            command.setBlocked(false);
            command.setIdpName(this.provider);
            command.setProfile(new AccountProfileCommandDto());

            account = this.userService.createAccount(command);
        }

        // Inject identity provider information
        account.setIdpUserName(idpUserName);
        account.setIdpUserImage(idpUserImage);

        final UserDetails principal = new User(account, "");

        // Replace authentication
        final UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        return new OAuth2Authentication(authentication.getOAuth2Request(), token);
    }

}
