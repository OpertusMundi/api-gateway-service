package eu.opertusmundi.web.security;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

import eu.opertusmundi.common.model.EnumAuthProvider;
import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.ServiceResponse;
import eu.opertusmundi.common.model.dto.AccountCreateCommandDto;
import eu.opertusmundi.common.model.dto.AccountDto;
import eu.opertusmundi.common.model.dto.AccountProfileUpdateCommandDto;
import eu.opertusmundi.common.model.dto.ActivationTokenCommandDto;
import eu.opertusmundi.common.model.dto.ActivationTokenDto;
import eu.opertusmundi.common.model.dto.AddressCommandDto;

public interface UserService {

    /**
     * Find account by user name
     *
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    Optional<AccountDto> findOneByUserName(String username) throws UsernameNotFoundException;

    /**
     * Find account by user name and authentication provider
     *
     * @param username
     * @param provider
     * @return
     * @throws UsernameNotFoundException
     */
    Optional<AccountDto> findOneByUserName(String username, EnumAuthProvider provider) throws UsernameNotFoundException;

    /**
     * Create new account
     *
     * @param command Account creation command
     * @return The new account
     */
    AccountDto createAccount(AccountCreateCommandDto command);

    /**
     * Create activation token
     *
     * @param command Token creation command
     * @return Instance of {@link ServiceResponse} with a result of type {@link ActivationTokenDto}
     */
    ServiceResponse<ActivationTokenDto> createToken(ActivationTokenCommandDto command);

    /**
     * Redeem activation token
     *
     * @param token
     * @return
     */
    ServiceResponse<Void> redeemToken(UUID token);

    /**
     * Update user profile
     *
     * @param command The profile update command
     * @return
     */
    AccountDto updateProfile(AccountProfileUpdateCommandDto command);

    /**
     * Grant roles to an account
     *
     * @param e
     * @param roles
     */
    void grant(AccountDto account, AccountDto grantedBy, EnumRole... roles);

    /**
     * Revoke roles from an account
     *
     * @param e
     * @param roles
     */
    void revoke(AccountDto account, EnumRole... roles);

    /**
     * Add address to user profile
     *
     * @return
     */
    AccountDto createAddress(AddressCommandDto command);

    /**
     * Update address in user profile
     *
     * @return
     */
    AccountDto updateAddress(AddressCommandDto command);

    /**
     * Remove address from user profile
     *
     * @return
     */
    AccountDto deleteAddress(AddressCommandDto command);

}
