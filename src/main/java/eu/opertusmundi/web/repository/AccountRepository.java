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
import eu.opertusmundi.common.model.EnumProviderRegistrationStatus;
import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.dto.AccountCreateCommandDto;
import eu.opertusmundi.common.model.dto.AccountDto;
import eu.opertusmundi.common.model.dto.AccountProfileConsumerCommandDto;
import eu.opertusmundi.common.model.dto.AccountProfileProviderCommandDto;
import eu.opertusmundi.common.model.dto.AccountProfileUpdateCommandDto;
import eu.opertusmundi.web.domain.AccountEntity;
import eu.opertusmundi.web.domain.AccountProfileEntity;
import eu.opertusmundi.web.domain.AccountProfileProviderFileEntity;
import eu.opertusmundi.web.domain.ActivationTokenEntity;
import eu.opertusmundi.web.domain.AddressEntity;
import eu.opertusmundi.web.domain.ProfileConsumerEmbeddable;
import eu.opertusmundi.web.domain.ProfileProviderEmbeddable;
import eu.opertusmundi.web.domain.ProviderRegistrationEntity;

@Repository
@Transactional(readOnly = true)
public interface AccountRepository extends JpaRepository<AccountEntity, Integer> {

    @Query("SELECT a FROM Account a LEFT OUTER JOIN FETCH a.profile p WHERE a.key in :keys")
    List<AccountEntity> findAllByKey(@Param("keys") UUID[] keys);

    @Query("SELECT a FROM Account a "
            + "LEFT OUTER JOIN FETCH a.profile p "
            + "LEFT OUTER JOIN FETCH p.addresses addr "
            + "WHERE a.key = :key")
    Optional<AccountEntity> findOneByKey(@Param("key") UUID key);

    @Query("SELECT a FROM Account a "
            + "LEFT OUTER JOIN FETCH a.profile p "
            + "LEFT OUTER JOIN FETCH p.addresses addr "
            + "WHERE a.username = :username")
    Optional<AccountEntity> findOneByUsername(@Param("username") String username);

    @Query("SELECT a FROM Account a "
            + "LEFT OUTER JOIN FETCH a.profile p "
            + "LEFT OUTER JOIN FETCH p.addresses addr "
            + "WHERE a.email = :email")
    Optional<AccountEntity> findOneByEmail(@Param("email") String email);

    @Query("SELECT a FROM Account a "
            + "LEFT OUTER JOIN FETCH a.profile p "
            + "LEFT OUTER JOIN FETCH p.addresses addr "
            + "WHERE a.email = :email and a.idpName = :provider")
    Optional<AccountEntity> findOneByEmailAndProvider(@Param("email") String email, @Param("provider") EnumAuthProvider provider);

    @Query("SELECT t FROM ActivationToken t WHERE t.redeemedAt IS NULL AND t.email = :email")
    Optional<ActivationTokenEntity> findActiveActivationTokensForEmail(@Param("email") String email);

    Optional<AccountEntity> findOneByEmailAndIdNot(String email, Integer id);

    @Query("SELECT a FROM Address a WHERE a.key = :key")
    Optional<AddressEntity> findAddressByKey(@Param("key") UUID key);

    @Transactional(readOnly = false)
    default AccountDto updateProfile(AccountProfileUpdateCommandDto command) {
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
    default AccountDto create(AccountCreateCommandDto command) {
        final AccountEntity        account = new AccountEntity();
        final AccountProfileEntity profile = new AccountProfileEntity();

        // Set account
        account.setActivationStatus(EnumActivationStatus.PENDING);
        account.setActive(true);
        account.setBlocked(false);
        account.setEmail(command.getEmail());
        account.setEmailVerified(false);
        account.setFirstName(command.getProfile().getFirstName());
        account.setIdpName(command.getIdpName());
        account.setIdpUserAlias(command.getIdpUserAlias());
        account.setIdpUserImage(command.getIdpUserImage());
        account.setLastName(command.getProfile().getLastName());
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

        final ZonedDateTime createdOn = account.getRegisteredAt();

        // Set profile
        profile.setAccount(account);
        profile.setAddresses(command.getProfile().getAddresses());
        profile.setCreatedOn(createdOn);
        profile.setImage(command.getProfile().getImage());
        profile.setImageMimeType(command.getProfile().getImageMimeType());
        profile.setMobile(command.getProfile().getMobile());
        profile.setModifiedOn(createdOn);
        profile.setPhone(command.getProfile().getPhone());

        // Set provider defaults
        profile.setProvider(new ProfileProviderEmbeddable());
        profile.getProvider().setRatingCount(0);
        profile.getProvider().setRatingTotal(0);
        profile.getProvider().setEmailVerified(false);

        // Set consumer defaults
        profile.setConsumer(new ProfileConsumerEmbeddable());

        // Grant the default role
        if (!account.hasRole(EnumRole.ROLE_USER)) {
            account.grant(EnumRole.ROLE_USER, null);
        }

        this.saveAndFlush(account);

        return account.toDto();
    }

    @Transactional(readOnly = false)
    default AccountDto updateConsumer(AccountProfileConsumerCommandDto command) {
        // Get account
        final AccountEntity account = this.findById(command.getId()).orElse(null);

        // Initialize profile if not already exists
        if (account.getProfile() == null) {
            account.setProfile(new AccountProfileEntity());
        }

        final AccountProfileEntity profile = account.getProfile();

        // Update consumer
        if (profile.getConsumer() == null) {
            // Initialize profile consumer data
            final ProfileConsumerEmbeddable consumer = new ProfileConsumerEmbeddable(command);
            profile.setConsumer(consumer);

            // Add ROLE_CONSUMER to the account
            account.grant(EnumRole.ROLE_CONSUMER, null);
        } else {
            profile.getConsumer().update(command);
        }

        // Set addresses
        if (command.getBillingAddress() != null) {
            final AddressEntity a = this.findAddressByKey(command.getBillingAddress()).orElse(null);
            profile.getConsumer().setBillingAddress(a);
        }
        if (command.getShippingAddress() != null) {
            final AddressEntity a = this.findAddressByKey(command.getShippingAddress()).orElse(null);
            profile.getConsumer().setShippingAddress(a);
        }

        this.save(account);

        return account.toDto();
    }

    @Transactional(readOnly = false)
    default AccountDto updateProviderRegistration(
        AccountProfileProviderCommandDto command, boolean submit
    ) throws IllegalArgumentException {
        final AccountEntity account = this.findById(command.getId()).orElse(null);

        // Initialize profile if not already exists
        if (account.getProfile() == null) {
            account.setProfile(new AccountProfileEntity());
        }

        final AccountProfileEntity profile = account.getProfile();

        if (profile.getProvider() == null) {
            profile.setProvider(new ProfileProviderEmbeddable());
        }

        if (profile.getProviderRegistration() == null ||
            profile.getProviderRegistration().isProcessed()) {
            final ProviderRegistrationEntity registration = new ProviderRegistrationEntity();
            registration.setAccount(account);

            profile.setProviderRegistration(registration);

        }

        final ProviderRegistrationEntity registration = profile.getProviderRegistration();

        // The registration must be already in DRAFT state
        if (registration.getStatus() != EnumProviderRegistrationStatus.DRAFT) {
            throw new IllegalArgumentException("Expected status to be DRAFT");
        }

        // Update registration
        registration.update(command);

        if (submit) {
            registration.setStatus(EnumProviderRegistrationStatus.SUBMITTED);
        }

        this.save(account);

        return account.toDto();
    }

    @Transactional(readOnly = false)
    default AccountDto acceptProviderRegistration(Integer userId) throws EntityNotFoundException, IllegalArgumentException {
        final AccountEntity              account      = this.findById(userId).orElse(null);
        final ProviderRegistrationEntity registration = account.getRegistration();

        // A registration must already exist
        if (registration == null) {
            throw new EntityNotFoundException("A registration record is required");
        }
        // The registration must be already in SUBMITTED state
        if(registration.getStatus() != EnumProviderRegistrationStatus.SUBMITTED) {
            throw new IllegalArgumentException("Expected status to be SUBMITTED");
        }

        registration.setStatus(EnumProviderRegistrationStatus.ACCEPTED);

        this.save(account);

        return account.toDto();
    }

    @Transactional(readOnly = false)
    default AccountDto rejectProviderRegistration(Integer userId) throws IllegalArgumentException {
        final AccountEntity account = this.findById(userId).orElse(null);

        final ProviderRegistrationEntity registration = account.getRegistration();

        // A registration must already exist
        if (registration == null) {
            throw new EntityNotFoundException("A registration record is required");
        }
        // The registration must be already in SUBMITTED state
        if(registration.getStatus() != EnumProviderRegistrationStatus.SUBMITTED) {
            throw new IllegalArgumentException("Expected status to be SUBMITTED");
        }

        registration.setStatus(EnumProviderRegistrationStatus.DRAFT);

        this.save(account);

        return account.toDto();
    }

    @Transactional(readOnly = false)
    default AccountDto cancelProviderRegistration(Integer userId) throws IllegalArgumentException {
        final AccountEntity              account      = this.findById(userId).orElse(null);
        final ProviderRegistrationEntity registration = account.getRegistration();

        // A registration must already exist
        if (registration == null) {
            throw new EntityNotFoundException("A registration record is required");
        }
        // The registration must not be already processed
        if(registration.isProcessed()) {
            throw new IllegalArgumentException("Expected registration status in [DRAFT, SUBMITTED, ACCEPTED]");
        }

        registration.setStatus(EnumProviderRegistrationStatus.CANCELLED);
        registration.setModifiedOn(ZonedDateTime.now());

        this.save(account);

        return account.toDto();
    }

    @Transactional(readOnly = false)
    default AccountDto completeProviderRegistration(Integer userId) throws IllegalArgumentException {
        final AccountEntity              account      = this.findById(userId).orElse(null);
        final AccountProfileEntity       profile      = account.getProfile();
        final ProfileProviderEmbeddable  provider     = profile.getProvider();
        final ProviderRegistrationEntity registration = account.getRegistration();
        final ZonedDateTime              now          = ZonedDateTime.now();

        // A registration must already exist
        if (registration == null) {
            throw new EntityNotFoundException("A registration record is required");
        }
        // The registration must be already in ACCEPTED state
        if(registration.getStatus() != EnumProviderRegistrationStatus.ACCEPTED) {
            throw new IllegalArgumentException("Expected status to be ACCEPTED");
        }

        // Update provider
        provider.update(registration);
        if (provider.getRegisteredOn() == null) {
            provider.setRegisteredOn(now);
        }
        provider.setModifiedOn(now);
        provider.setTermsAccepted(true);
        provider.setTermsAcceptedAt(now);

        // Update registration
        registration.setStatus(EnumProviderRegistrationStatus.COMPLETED);
        registration.setModifiedOn(now);

        // Update profile provider files
        provider.getFiles().clear();

        registration.getFiles().stream()
            .map(AccountProfileProviderFileEntity::new)
            .forEach(provider.getFiles()::add);

        // Update profile
        profile.setModifiedOn(now);

        // Add ROLE_PROVIDER to the account
        if (!account.hasRole(EnumRole.ROLE_PROVIDER)) {
            account.grant(EnumRole.ROLE_PROVIDER, null);
        }

        this.save(account);

        return account.toDto();
    }

}
