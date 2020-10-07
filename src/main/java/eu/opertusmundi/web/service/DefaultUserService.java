package eu.opertusmundi.web.service;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.EnumActivationStatus;
import eu.opertusmundi.common.model.EnumActivationTokenType;
import eu.opertusmundi.common.model.EnumAuthProvider;
import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.ServiceResponse;
import eu.opertusmundi.common.model.dto.AccountCommandDto;
import eu.opertusmundi.common.model.dto.AccountDto;
import eu.opertusmundi.common.model.dto.AccountProfileCommandDto;
import eu.opertusmundi.common.model.dto.AccountProfileDto;
import eu.opertusmundi.common.model.dto.ActivationTokenCommandDto;
import eu.opertusmundi.common.model.dto.ActivationTokenDto;
import eu.opertusmundi.web.domain.AccountEntity;
import eu.opertusmundi.web.domain.AccountProfileEntity;
import eu.opertusmundi.web.domain.ActivationTokenEntity;
import eu.opertusmundi.web.feign.client.EmailServiceFeignClient;
import eu.opertusmundi.web.model.email.MailActivationModel;
import eu.opertusmundi.web.model.email.MessageDto;
import eu.opertusmundi.web.repository.AccountRepository;
import eu.opertusmundi.web.repository.ActivationTokenRepository;
import feign.FeignException;

@Service
public class DefaultUserService implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultUserService.class);

    @Value("${opertus-mundi.base-url}")
    private String baseUrl;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ActivationTokenRepository activationTokenRepository;

    @Autowired
    private ObjectProvider<EmailServiceFeignClient> emailClient;

    @Override
    public Optional<AccountDto> findOneByUserName(String username) throws UsernameNotFoundException {
        final AccountEntity account = this.accountRepository.findOneByEmail(username).orElse(null);

        return Optional.ofNullable(account == null ? null : account.toDto());
    }

    @Override
    public Optional<AccountDto> findOneByUserName(String username, EnumAuthProvider provider) throws UsernameNotFoundException {
        final AccountEntity account = this.accountRepository.findOneByEmailAndProvider(username, provider).orElse(null);

        return Optional.ofNullable(account == null ? null : account.toDto());
    }

    @Override
    public AccountDto createAccount(AccountCommandDto command) {
        // Create account
        final AccountDto account = this.accountRepository.create(command);

        // Create activation token for account email
        final ActivationTokenDto accountToken = this.activationTokenRepository.create(
            account.getId(), account.getEmail(), 1, EnumActivationTokenType.ACCOUNT
        );
        // Send activation link to client
        this.sendMail(account.getFullName(), accountToken);

        return account;
    }

    @Override
    public ServiceResponse<ActivationTokenDto> createToken(ActivationTokenCommandDto command) {
        final AccountEntity account = this.accountRepository.findOneByEmail(command.getEmail()).orElse(null);

        logger.info("Request token for email {}", command.getEmail());

        if (account == null) {
            return ServiceResponse.success();
        }

        final boolean                 activated = account.getActivationStatus() == EnumActivationStatus.COMPLETED;
        final EnumActivationTokenType type      = activated ? EnumActivationTokenType.MAIL : EnumActivationTokenType.ACCOUNT;

        switch (type) {
            case ACCOUNT :
                if (!command.getEmail().equals(account.getEmail())) {
                    // Invalid email
                    return ServiceResponse.error(BasicMessageCode.EmailNotFound, "Email not registered to the account");
                }
                break;
            case MAIL :
                if (!command.getEmail().equals(account.getProfile().getEmail())) {
                    // Invalid email
                    return ServiceResponse.error(BasicMessageCode.EmailNotFound, "Email not registered to the profile");
                }
                break;
        }

        // Create activation token
        final ActivationTokenDto token = this.activationTokenRepository.create(account.getId(), command.getEmail(), 1, type);
        // Send activation link to client
        this.sendMail(account.getFullName(), token);

        return ServiceResponse.result(token);
    }

    @Override
    @Transactional
    public ServiceResponse<Void> redeemToken(UUID token) {
        final ActivationTokenEntity tokenEntity = this.activationTokenRepository.findOneByToken(token).orElse(null);

        if (tokenEntity == null) {
            return ServiceResponse.error(BasicMessageCode.TokenNotFound, "Token was not found");
        }
        if (tokenEntity.isExpired()) {
            return ServiceResponse.error(BasicMessageCode.TokenIsExpired, "Token has expired");
        }

        final AccountEntity accountEntity = this.accountRepository.findById(tokenEntity.getAccount()).orElse(null);

        if (accountEntity == null) {
            return ServiceResponse.error(BasicMessageCode.AccountNotFound, "Account was not found");
        }

        final ZonedDateTime now = ZonedDateTime.now();

        this.activationTokenRepository.redeem(tokenEntity);

        switch (tokenEntity.getType()) {
            case ACCOUNT :
                if (!tokenEntity.getEmail().equals(accountEntity.getEmail())) {
                    return ServiceResponse.error(BasicMessageCode.EmailNotFound, "Email not registered to the account");
                }
                if (accountEntity.getActivationStatus() != EnumActivationStatus.COMPLETED) {
                    // Activate account only once
                    accountEntity.setActivatedAt(now);
                    accountEntity.setActivationStatus(EnumActivationStatus.COMPLETED);
                }
                // Always verify account
                accountEntity.setEmailVerified(true);
                accountEntity.setEmailVerifiedAt(now);
                break;
            case MAIL :
                final AccountProfileEntity profileEntity = accountEntity.getProfile();

                if (!tokenEntity.getEmail().equals(profileEntity.getEmail())) {
                    return ServiceResponse.error(BasicMessageCode.EmailNotFound, "Email not registered to the profile");
                }
                if (accountEntity.getActivationStatus() == EnumActivationStatus.COMPLETED) {
                    profileEntity.setEmailVerified(true);
                    profileEntity.setEmailVerifiedAt(now);
                }
                break;
        }

        this.accountRepository.save(accountEntity);

        return ServiceResponse.success();
    }

    @Override
    @Transactional
    public AccountDto updateProfile(AccountProfileCommandDto command) {
        final AccountDto account = this.accountRepository.updateProfile(command);
        final AccountProfileDto profile = account.getProfile();

        Assert.isTrue(profile != null, "Profile must not be null");

        // Check if profile (public) email requires validation
        if (!StringUtils.isBlank(profile.getEmail()) && !profile.isEmailVerified()) {
            // Create activation token
            final ActivationTokenDto token = this.activationTokenRepository.create(
                account.getId(), profile.getEmail(), 1, EnumActivationTokenType.MAIL
            );
            // Send activation link to client
            this.sendMail(account.getFullName(), token);
        }

        return account;
    }

    @Override
    @Transactional
    public void grant(AccountDto account, AccountDto grantedby, EnumRole... roles) {
        Assert.notNull(account, "Expected a non-null account");
        Assert.notEmpty(roles, "Expected at least 1 role");

        final Optional<AccountEntity> accountEntity = this.accountRepository.findById(account.getId());
        if (!accountEntity.isPresent())
         {
            return; // no such account
        }

        final Optional<AccountEntity> grantedbyEntity = grantedby != null ? this.accountRepository.findById(grantedby.getId())
                : Optional.empty();

        for (final EnumRole role : roles) {
            accountEntity.get().grant(role, grantedbyEntity.isPresent() ? grantedbyEntity.get() : null);
        }

        this.accountRepository.saveAndFlush(accountEntity.get());
    }

    @Override
    @Transactional
    public void revoke(AccountDto account, EnumRole... roles) {
        Assert.notNull(account, "Expected a non-null account");
        Assert.notEmpty(roles, "Expected at least 1 role");

        final Optional<AccountEntity> accountEntity = this.accountRepository.findById(account.getId());
        if (!accountEntity.isPresent())
         {
            return; // no such account
        }

        for (final EnumRole role : roles) {
            accountEntity.get().revoke(role);
        }

        this.accountRepository.saveAndFlush(accountEntity.get());
    }

    private void sendMail(String name, ActivationTokenDto token) {
        // Compose message
        final MessageDto<MailActivationModel> message = new MessageDto<>();

        final MailActivationModel model = this.createActivationMailModel(name, token);

        // TODO: Create/Render template, create parameters for subject, sender
        // and URLs

        message.setSubject("Activate account");

        message.setSender("hello@OpertusMundi.eu", "OpertusMundi");

        if (StringUtils.isBlank(name)) {
            message.setRecipients(token.getEmail());
        } else {
            message.setRecipients(token.getEmail(), name);
        }

        message.setTemplate("token-request");

        message.setModel(model);

        try {
            final ResponseEntity<BaseResponse> response = this.emailClient.getObject().sendMail(message);

            if (!response.getBody().getSuccess()) {
                // TODO: Add logging ...
                // TODO: Handle error ...
            }
        } catch (final FeignException fex) {
            final BasicMessageCode code = BasicMessageCode.fromStatusCode(fex.status());

            // TODO: Add logging ...
            // TODO: Handle error ...
        }
    }

    private MailActivationModel createActivationMailModel(String name, ActivationTokenDto token) {
        final MailActivationModel model = new MailActivationModel();

        model.setName(name);
        model.setToken(token.getToken());
        model.setUrl(this.baseUrl);

        return model;
    }

}
