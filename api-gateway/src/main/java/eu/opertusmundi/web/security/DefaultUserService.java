package eu.opertusmundi.web.security;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.ActivationTokenEntity;
import eu.opertusmundi.common.domain.CustomerEntity;
import eu.opertusmundi.common.domain.CustomerProfessionalEntity;
import eu.opertusmundi.common.feign.client.EmailServiceFeignClient;
import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.EnumAuthProvider;
import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.ServiceResponse;
import eu.opertusmundi.common.model.account.AccountCommandDto;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.AccountProfileCommandDto;
import eu.opertusmundi.common.model.account.ActivationTokenCommandDto;
import eu.opertusmundi.common.model.account.ActivationTokenDto;
import eu.opertusmundi.common.model.account.EnumActivationStatus;
import eu.opertusmundi.common.model.account.EnumActivationTokenType;
import eu.opertusmundi.common.model.analytics.ProfileRecord;
import eu.opertusmundi.common.model.email.EmailAddressDto;
import eu.opertusmundi.common.model.email.EnumMailType;
import eu.opertusmundi.common.model.email.MessageDto;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.ActivationTokenRepository;
import eu.opertusmundi.common.service.ElasticSearchService;
import eu.opertusmundi.common.service.messaging.MailMessageHelper;
import eu.opertusmundi.web.model.security.CreateAccountResult;
import eu.opertusmundi.web.model.security.PasswordChangeCommandDto;

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
    private MailMessageHelper messageHelper;

    @Autowired
    private ObjectProvider<EmailServiceFeignClient> emailClient;

    @Autowired(required = false)
    private ElasticSearchService elasticSearchService;

    @Override
    @Transactional
    public Optional<AccountDto> findOneByUserName(String username) throws UsernameNotFoundException {
        final AccountEntity account = this.accountRepository.findOneByEmail(username).orElse(null);

        return Optional.ofNullable(account == null ? null : account.toDto());
    }

    @Override
    @Transactional
    public Optional<AccountDto> findOneByUserName(String username, EnumAuthProvider provider) throws UsernameNotFoundException {
        final AccountEntity account = this.accountRepository.findOneByEmailAndProvider(username, provider).orElse(null);

        return Optional.ofNullable(account == null ? null : account.toDto());
    }

    @Override
    @Transactional
    public ServiceResponse<CreateAccountResult> createAccount(AccountCommandDto command) {
        // Create account
        final AccountDto account = this.accountRepository.create(command);

        // Create activation token for account email
        final ActivationTokenCommandDto tokenCommand = new ActivationTokenCommandDto();
        tokenCommand.setEmail(command.getEmail());

        final ServiceResponse<ActivationTokenDto> tokenResponse = this.createToken(EnumActivationTokenType.ACCOUNT, tokenCommand);

        // Update account profile
        if (elasticSearchService != null) {
            elasticSearchService.addProfile(ProfileRecord.from(account));
        }

        return ServiceResponse.result(CreateAccountResult.of(account, tokenResponse.getResult()));
    }

    @Override
    @Transactional
    public ServiceResponse<ActivationTokenDto> createToken(EnumActivationTokenType type, ActivationTokenCommandDto command) {
        final AccountEntity account = this.accountRepository.findOneByEmail(command.getEmail()).orElse(null);

        logger.info("Request activation token. [email={}]", command.getEmail());

        if (account == null) {
            logger.info("Request activation has failed. Account was not found. [email={}]", command.getEmail());

            return ServiceResponse.success();
        }

        switch (type) {
            case ACCOUNT :
                if (!command.getEmail().equals(account.getEmail())) {
                    // Invalid email
                    return ServiceResponse.error(BasicMessageCode.EmailNotFound, "Email not registered to the account");
                }
                break;
            case CONSUMER :
                if (!command.getEmail().equals(account.getProfile().getConsumer().getEmail())) {
                    // Invalid email
                    return ServiceResponse.error(BasicMessageCode.EmailNotFound, "Email not registered to the consumer profile");
                }
                break;
            case PROVIDER :
                if (!command.getEmail().equals(account.getProfile().getProvider().getEmail())) {
                    // Invalid email
                    return ServiceResponse.error(BasicMessageCode.EmailNotFound, "Email not registered to the provider profile");
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
            case CONSUMER:
                final CustomerEntity consumerEntity = accountEntity.getProfile().getConsumer();

                if (!tokenEntity.getEmail().equals(consumerEntity.getEmail())) {
                    return ServiceResponse.error(BasicMessageCode.EmailNotFound, "Email not registered to the consumer");
                }
                // Verify email only for registered accounts
                if (accountEntity.getActivationStatus() == EnumActivationStatus.COMPLETED) {
                    consumerEntity.setEmailVerified(true);
                    consumerEntity.setEmailVerifiedAt(now);
                }
                break;
            case PROVIDER :
                final CustomerProfessionalEntity providerEntity = accountEntity.getProfile().getProvider();

                if (!tokenEntity.getEmail().equals(providerEntity.getEmail())) {
                    return ServiceResponse.error(BasicMessageCode.EmailNotFound, "Email not registered to the provider");
                }
                // Verify email only for registered accounts
                if (accountEntity.getActivationStatus() == EnumActivationStatus.COMPLETED) {
                    providerEntity.setEmailVerified(true);
                    providerEntity.setEmailVerifiedAt(now);
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

        // Update account profile
        if (elasticSearchService != null) {
            elasticSearchService.addProfile(ProfileRecord.from(account));
        }

        return account;
    }

    @Override
    @Transactional
    public void grant(AccountDto account, AccountDto grantedby, EnumRole... roles) {
        Assert.notNull(account, "Expected a non-null account");
        Assert.notEmpty(roles, "Expected at least 1 role");

        final Optional<AccountEntity> accountEntity = this.accountRepository.findById(account.getId());
        if (!accountEntity.isPresent()) {
            return;
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
        if (!accountEntity.isPresent()) {
            return;
        }

        for (final EnumRole role : roles) {
            accountEntity.get().revoke(role);
        }

        this.accountRepository.saveAndFlush(accountEntity.get());
    }

    @Override
    @Transactional
    public void changePassword(PasswordChangeCommandDto command) {
        final AccountEntity account = this.accountRepository.findOneByUsername(command.getUserName()).orElse(null);

        if (account == null) {
            throw new UsernameNotFoundException(command.getUserName());
        }

        final PasswordEncoder encoder = new BCryptPasswordEncoder();

        if (!encoder.matches(command.getCurrentPassword(), account.getPassword())) {
            throw new BadCredentialsException(command.getUserName());
        }

        account.setPassword(encoder.encode(command.getNewPassword()));
        this.accountRepository.saveAndFlush(account);

        // TODO: Send mail

        logger.info("Password changed. [user={}]", account.getUserName());
    }

    private void sendMail(String name, ActivationTokenDto token) {
        try {
            final EnumMailType        type  = EnumMailType.ACCOUNT_ACTIVATION_TOKEN;
            final Map<String, Object> model = this.messageHelper.createModel(type);
            model.put("name", name);
            model.put("token", token.getToken());
            model.put("url", this.baseUrl);

            final EmailAddressDto                 sender   = this.messageHelper.getSender(type, model);
            final String                          subject  = this.messageHelper.composeSubject(type, model);
            final String                          template = this.messageHelper.resolveTemplate(type, model);
            final MessageDto<Map<String, Object>> message  = new MessageDto<>();

            message.setSender(sender);
            message.setSubject(subject);
            message.setTemplate(template);
            message.setModel(model);

            if (StringUtils.isBlank(name)) {
                message.setRecipients(token.getEmail());
            } else {
                message.setRecipients(token.getEmail(), name);
            }

            final ResponseEntity<BaseResponse> response = this.emailClient.getObject().sendMail(message);

            if (!response.getBody().getSuccess()) {
                logger.error(String.format("Failed to send mail [recipient=%s]", token.getEmail()));
            }
        } catch (final Exception ex) {
            logger.error(String.format("Failed to send mail [recipient=%s]", token.getEmail()), ex);
        }
    }

}
