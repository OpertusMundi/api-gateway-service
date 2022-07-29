package eu.opertusmundi.web.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import eu.opertusmundi.common.model.EnumAuthProvider;
import eu.opertusmundi.common.model.account.ExternalIdpAccountCommand;
import eu.opertusmundi.web.model.OAuth2AccountCreationException;

public interface CustomUserDetailsService extends UserDetailsService {

    default UserDetails loadUserByUsername(String username, EnumAuthProvider provider) throws UsernameNotFoundException {
        return this.loadUserByUsername(username, provider, null);
    }

    /**
     * Load user details for the given user name.
     *
     * <p>
     * If the user is not found and a valid command exists, the user is created;
     * Otherwise an {@link UsernameNotFoundException} is thrown.
     *
     * @param username
     * @param provider
     * @param command
     * @return
     * @throws UsernameNotFoundException if the account was not found
     * @throws OAuth2AccountCreationException if the account creation has failed
     */
    UserDetails loadUserByUsername(
        String username, EnumAuthProvider provider, ExternalIdpAccountCommand command
    ) throws UsernameNotFoundException, OAuth2AccountCreationException;

}
