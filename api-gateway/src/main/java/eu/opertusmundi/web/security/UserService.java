package eu.opertusmundi.web.security;

import java.util.Optional;
import java.util.UUID;

import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import eu.opertusmundi.common.model.EnumAuthProvider;
import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.ServiceResponse;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.AccountProfileCommandDto;
import eu.opertusmundi.common.model.account.ActivationTokenCommandDto;
import eu.opertusmundi.common.model.account.ActivationTokenDto;
import eu.opertusmundi.common.model.account.EnumActivationTokenType;
import eu.opertusmundi.common.model.account.JoinVendorCommandDto;
import eu.opertusmundi.common.model.account.PlatformAccountCommandDto;
import eu.opertusmundi.common.model.account.VendorAccountCommandDto;
import eu.opertusmundi.web.model.security.CreateAccountResult;
import eu.opertusmundi.web.model.security.PasswordChangeCommandDto;

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
     * @return
     */
    ServiceResponse<CreateAccountResult> createPlatformAccount(PlatformAccountCommandDto command);

    /**
     * Create new vendor account
     *
     * @param command Account creation command
     * @return
     */
    ServiceResponse<AccountDto> createVendorAccount(VendorAccountCommandDto command);

    /**
     * Update existing vendor account
     *
     * @param command Account creation command
     * @return
     */
    ServiceResponse<AccountDto> updateVendorAccount(VendorAccountCommandDto command);

    /**
     * Create activation token
     *
     * @param command Token creation command
     * @param sendMail If true, send new token by mail
     * @return Instance of {@link ServiceResponse} with a result of type {@link ActivationTokenDto}
     */
    default ServiceResponse<ActivationTokenDto> createToken(ActivationTokenCommandDto command, boolean sendMail) {
        return this.createToken(null, command, sendMail);
    }

    /**
     * Create activation token
     *
     * @param type Type of token to create
     * @param command Token creation command
     * @param sendMail If true, send new token by mail
     * @return Instance of {@link ServiceResponse} with a result of type {@link ActivationTokenDto}
     */
    ServiceResponse<ActivationTokenDto> createToken(@Nullable EnumActivationTokenType type, ActivationTokenCommandDto command, boolean sendMail);

    /**
     * Create activation token for vendor account
     *
     * @param vendorKey The parent vendor account unique key
     * @param accountKey Account unique key
     * @return Instance of {@link ServiceResponse} with a result of type {@link ActivationTokenDto}
     */
    ServiceResponse<AccountDto> invite(UUID vendorKey, UUID accountKey);

    /**
     * Join vendor organization
     *
     * @param command
     * @return
     */
    ServiceResponse<Void> joinOrganization(JoinVendorCommandDto command);

    /**
     * Enable vendor account
     *
     * If the account registration is not completed, an invite is sent
     *
     * @param vendorKey
     * @param accountKey
     * @return
     */
    ServiceResponse<AccountDto> enableVendorAccount(UUID vendorKey, UUID accountKey);

    /**
     * Disable vendor account
     *
     * @param vendorKey
     * @param accountKey
     * @return
     */
    ServiceResponse<AccountDto> disableVendorAccount(UUID vendorKey, UUID accountKey);

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
    AccountDto updateProfile(AccountProfileCommandDto command);

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
     * Change password for the authenticated user
     *
     * @param command
     */
    void changePassword(PasswordChangeCommandDto command);

    /**
     * Set the password password of a vendor account
     *
     * @param command
     */
    void changePassword(JoinVendorCommandDto command);

}
