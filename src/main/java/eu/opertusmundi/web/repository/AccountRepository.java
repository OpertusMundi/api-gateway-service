package eu.opertusmundi.web.repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.opertusmundi.common.model.EnumActivationStatus;
import eu.opertusmundi.common.model.EnumAuthProvider;
import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.dto.AccountCommandDto;
import eu.opertusmundi.common.model.dto.AccountDto;
import eu.opertusmundi.common.model.dto.AccountProfileCommandDto;
import eu.opertusmundi.web.domain.AccountEntity;
import eu.opertusmundi.web.domain.AccountProfileEntity;
import eu.opertusmundi.web.domain.ActivationTokenEntity;

@Repository
@Transactional(readOnly = true)
public interface AccountRepository extends JpaRepository<AccountEntity, Integer> {

    @Query("SELECT a FROM Account a LEFT OUTER JOIN FETCH a.profile p WHERE a.key in :keys")
    List<AccountEntity> findAllByKey(@Param("keys") UUID[] keys);

    @Query("SELECT a FROM Account a LEFT OUTER JOIN FETCH a.profile p LEFT OUTER JOIN FETCH p.addresses addr WHERE a.username = :username")
    Optional<AccountEntity> findOneByUsername(@Param("username") String username);

    @Query("SELECT a FROM Account a LEFT OUTER JOIN FETCH a.profile p LEFT OUTER JOIN FETCH p.addresses addr WHERE a.email = :email")
    Optional<AccountEntity> findOneByEmail(@Param("email") String email);

    @Query("SELECT a FROM Account a LEFT OUTER JOIN FETCH a.profile p LEFT OUTER JOIN FETCH p.addresses addr WHERE a.email = :email and a.idpName = :provider")
    Optional<AccountEntity> findOneByEmailAndProvider(@Param("email") String email, @Param("provider") EnumAuthProvider provider);

    Optional<AccountEntity> findOneByEmailAndIdNot(String email, Integer id);

    @Query("SELECT t FROM ActivationToken t WHERE t.redeemedAt IS NULL AND t.email = :email")
    Optional<ActivationTokenEntity> findActiveActivationTokensForEmail(@Param("email") String email);

    @Transactional(readOnly = false)
    default AccountDto updateProfile(AccountProfileCommandDto command) {
        // Get account
        final AccountEntity account = this.findById(command.getId()).orElse(null);

        // Initialize profile if not already exists
        if (account.getProfile() == null) {
            account.setProfile(new AccountProfileEntity());
        }

        final AccountProfileEntity profile = account.getProfile();

        // Update account
        account.update(command);

        // Update profile
        profile.update(command);

        this.save(account);

        return account.toDto();
    }

    @Transactional(readOnly = false)
    default AccountDto create(AccountCommandDto command) {
        final AccountEntity        account = new AccountEntity();
        final AccountProfileEntity profile = new AccountProfileEntity();

        account.setProfile(profile);
        account.setActivationStatus(EnumActivationStatus.PENDING);
        account.setActive(true);
        account.setBlocked(false);
        account.setEmail(command.getEmail());
        account.setEmailVerified(false);
        account.setFirstName(command.getFirstName());
        account.setIdpName(command.getIdpName());
        account.setIdpUserAlias(command.getIdpUserAlias());
        account.setIdpUserImage(command.getIdpUserImage());
        account.setLastName(command.getLastName());
        if (StringUtils.isBlank(command.getLocale())) {
            account.setLocale("en");
        } else {
            account.setLocale(command.getLocale());
        }
        account.setProfile(profile);
        account.setUsername(command.getEmail());

        if (!StringUtils.isBlank(command.getPassword())) {
            final PasswordEncoder encoder = new BCryptPasswordEncoder();

            account.setPassword(encoder.encode(command.getPassword()));
        }

        profile.setAccount(account);
        profile.setAdditionalInfo(command.getProfile().getAdditionalInfo());
        profile.setBankAccountCurrency(command.getProfile().getBankAccountCurrency());
        profile.setBankAccountHolderName(command.getProfile().getBankAccountHolderName());
        profile.setBankAccountIban(command.getProfile().getBankAccountIban());
        profile.setBankAccountSwift(command.getProfile().getBankAccountSwift());
        profile.setCompany(command.getProfile().getCompany());
        profile.setCompanyType(command.getProfile().getCompanyType());
        profile.setCountry(command.getProfile().getCountry());
        profile.setCountryPhoneCode(command.getProfile().getCountryPhoneCode());
        profile.setEmail(command.getProfile().getEmail());
        profile.setEmailVerified(false);
        profile.setImage(command.getProfile().getImage());
        profile.setImageMimeType(command.getProfile().getImageMimeType());
        profile.setLogoImage(command.getProfile().getLogoImage());
        profile.setLogoImageMimeType(command.getProfile().getLogoImageMimeType());
        profile.setMobile(command.getProfile().getMobile());
        profile.setPhone(command.getProfile().getPhone());
        profile.setRatingCount(0);
        profile.setRatingTotal(0);
        profile.setSiteUrl(command.getProfile().getSiteUrl());
        profile.setTermsAccepted(false);
        profile.setVat(command.getProfile().getVat());

        profile.setModifiedOn(profile.getCreatedOn());
        profile.setRatingCount(0);

        // Always grant the default role
        if (!account.hasRole(EnumRole.ROLE_USER)) {
            account.grant(EnumRole.ROLE_USER, null);
        }

        this.saveAndFlush(account);

        return account.toDto();
    }

    @Transactional(readOnly = false)
    default AccountDto create(int id) {
        final AccountEntity account = this.findById(id).orElse(null);

        if(account == null) {
            throw new EntityNotFoundException();
        }

        account.getProfile().setTermsAccepted(true);
        account.getProfile().setTermsAcceptedAt(ZonedDateTime.now());

        this.saveAndFlush(account);

        return account.toDto();
    }

}
