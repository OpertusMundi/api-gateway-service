package eu.opertusmundi.web.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.opertusmundi.common.model.EnumAuthProvider;
import eu.opertusmundi.common.model.ServiceResponse;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.EnumActivationStatus;
import eu.opertusmundi.common.model.account.ExternalIdpAccountCommand;
import eu.opertusmundi.web.model.OAuth2AccountCreationException;
import eu.opertusmundi.web.model.OAuth2AccountExistsException;
import eu.opertusmundi.web.model.security.User;

@Service
public class DefaultUserDetailsService implements CustomUserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Assert.hasText(username, "Expected a non-empty user name");
        final AccountDto account = this.userService.findOneByUserName(username).orElse(null);

        if (account == null || account.getActivationStatus() != EnumActivationStatus.COMPLETED) {
            throw new UsernameNotFoundException(username);
        }

        return new User(account, account.getPassword());
    }

    @Override
    public UserDetails loadUserByUsername(
        String username, EnumAuthProvider provider, ExternalIdpAccountCommand command
    ) throws UsernameNotFoundException, OAuth2AccountCreationException {
        Assert.hasText(username, "Expected a non-empty user name");
        Assert.notNull(provider, "Expected a non-null provider");
        AccountDto providerAccount = this.userService.findOneByUserName(username, provider).orElse(null);

        if (providerAccount == null) {
            if (command == null) {
                throw new UsernameNotFoundException(username);
            }
            final AccountDto accountWithSameUsername = this.userService.findOneByUserName(username).orElse(null);
            if (accountWithSameUsername != null) {
                throw new OAuth2AccountExistsException(String.format(
                    "Cannot create account. Username already exists [username=%s]", username
                ));
            }
            // Create new external IdP account and start an instance of the
            // account activation workflow
            final ServiceResponse<AccountDto> response = userService.createExternalIdpAccount(command);
            if (!response.getMessages().isEmpty()) {
                throw new OAuth2AccountCreationException(response.getMessages().get(0).getDescription());
            }
            providerAccount = response.getResult();
        }

        return new User(providerAccount, providerAccount.getPassword());
    }
}
