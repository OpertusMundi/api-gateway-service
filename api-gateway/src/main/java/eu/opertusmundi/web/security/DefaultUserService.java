package eu.opertusmundi.web.security;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import eu.opertusmundi.common.domain.HelpdeskAccountEntity;
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
import eu.opertusmundi.common.model.workflow.EnumProcessInstanceVariable;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.ActivationTokenRepository;
import eu.opertusmundi.common.repository.HelpdeskAccountRepository;
import eu.opertusmundi.common.service.ElasticSearchService;
import eu.opertusmundi.common.util.BpmEngineUtils;
import eu.opertusmundi.common.util.BpmInstanceVariablesBuilder;
import eu.opertusmundi.web.model.security.CreateAccountResult;
import eu.opertusmundi.web.model.security.PasswordChangeCommandDto;

@Service
public class DefaultUserService implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultUserService.class);

    private static final String WORKFLOW_ACCOUNT_REGISTRATION = "account-registration";

    private static final String MESSAGE_EMAIL_VERIFIED = "email-verified-message";

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private HelpdeskAccountRepository helpdeskAccountRepository;

    @Autowired
    private ActivationTokenRepository activationTokenRepository;

    @Autowired
    protected BpmEngineUtils bpmEngine;

    @Autowired(required = false)
    private ElasticSearchService elasticSearchService;

    @Override
    @Transactional
    public Optional<AccountDto> findOneByUserName(String username) throws UsernameNotFoundException {
        AccountDto account = this.accountRepository.findOneByEmail(username)
            .map(AccountEntity::toDto)
            .orElse(null);

        if (account == null) {
            account = this.helpdeskAccountRepository.findOneByEmail(username)
                .map(HelpdeskAccountEntity::toMarketplaceAccountDto)
                .orElse(null);
        }

        return Optional.ofNullable(account == null ? null : account);
    }

    @Override
    @Transactional
    public Optional<AccountDto> findOneByUserName(String username, EnumAuthProvider provider) throws UsernameNotFoundException {
        final AccountEntity account = this.accountRepository.findOneByEmailAndProvider(username, provider).orElse(null);

        return Optional.ofNullable(account == null ? null : account.toDto());
    }

    /**
     * Creates a new account registration
     * <p>
     * The registration is created in two separate transactions:
     * <p>
     * <ol>
     *  <li>The account is created in the database
     *  <li>The account registration workflow instance is started
     * </ol>
     */
    @Override
    public ServiceResponse<CreateAccountResult> createAccount(AccountCommandDto command) {
        final CreateAccountResult result = this.createAccountRecord(command);

        this.createAccountRegistrationWorkflowInstance(result);

        return ServiceResponse.result(result);
    }

    @Transactional
    private CreateAccountResult createAccountRecord(AccountCommandDto command) {
        // Create account
        final AccountDto account = this.accountRepository.create(command);

        // Create activation token for account email
        final ActivationTokenCommandDto tokenCommand = ActivationTokenCommandDto.of(command.getEmail());

        final ServiceResponse<ActivationTokenDto> tokenResponse = this.createToken(EnumActivationTokenType.ACCOUNT, tokenCommand);

        // Update account profile
        if (elasticSearchService != null) {
            elasticSearchService.addProfile(ProfileRecord.from(account));
        }

        return CreateAccountResult.of(account, tokenResponse.getResult());
    }

    @Transactional
    private void createAccountRegistrationWorkflowInstance(CreateAccountResult result) {
        final Integer accountId       = result.getAccount().getId();
        final String  accountKey      = result.getAccount().getKey().toString();
        final String  activationToken = result.getToken().getToken().toString();

        try {
            ProcessInstanceDto instance = this.bpmEngine.findInstance(accountKey);

            if (instance == null) {
                final Map<String, VariableValueDto> variables = BpmInstanceVariablesBuilder.builder()
                    .variableAsString(EnumProcessInstanceVariable.START_USER_KEY.getValue(), accountKey)
                    .variableAsString("userKey", accountKey)
                    .variableAsString("activationToken", activationToken)
                    .build();

                instance = bpmEngine.startProcessDefinitionByKey(
                    WORKFLOW_ACCOUNT_REGISTRATION, accountKey, variables
                );
            }

            if (StringUtils.isBlank(result.getAccount().getProcessInstance())) {
                this.accountRepository.setRegistrationWorkflowInstance(accountId, instance.getDefinitionId(), instance.getId());
            }
        } catch (final Exception ex) {
            // Allow workflow instance initialization to fail
            logger.warn(String.format("Failed to start account registration workflow instance [accountKey=%s]", accountKey), ex);
        }
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

        return ServiceResponse.result(token);
    }

    @Override
    @Transactional
    public ServiceResponse<Void> redeemToken(UUID token) {
        final ActivationTokenEntity tokenEntity = this.activationTokenRepository.findOneByKey(token).orElse(null);

        if (tokenEntity == null) {
            return ServiceResponse.error(BasicMessageCode.TokenNotFound, "Token was not found");
        }
        if (tokenEntity.isExpired()) {
            return ServiceResponse.error(BasicMessageCode.TokenIsExpired, "Token has expired");
        }

        final ZonedDateTime now           = ZonedDateTime.now();
        final AccountEntity accountEntity = tokenEntity.getAccount();
        boolean             sendMessage   = false;

        if (accountEntity == null) {
            return ServiceResponse.error(BasicMessageCode.AccountNotFound, "Account was not found");
        }

        this.activationTokenRepository.redeem(tokenEntity);

        switch (tokenEntity.getType()) {
            case ACCOUNT :
                if (!tokenEntity.getEmail().equals(accountEntity.getEmail())) {
                    return ServiceResponse.error(BasicMessageCode.EmailNotFound, "Email not registered to the account");
                }
                if (accountEntity.getActivationStatus() == EnumActivationStatus.PENDING) {
                    // Activate account only once
                    accountEntity.setActivatedAt(now);
                    // Workflow will change status to COMPLETED
                    accountEntity.setActivationStatus(EnumActivationStatus.PROCESSING);

                    // Send message to workflow process instance
                    sendMessage = true;
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

        this.accountRepository.saveAndFlush(accountEntity);

        if (sendMessage) {
            this.sendTokenToProcessInstance(accountEntity.getKey(), token);
        }

        return ServiceResponse.success();
    }

    private void sendTokenToProcessInstance(UUID accountKey, UUID token) {
        final String                        businessKey = accountKey.toString();
        final Map<String, VariableValueDto> variables   = BpmInstanceVariablesBuilder.builder()
            .variableAsString("activationToken", token.toString())
            .build();

        this.bpmEngine.correlateMessage(businessKey, MESSAGE_EMAIL_VERIFIED, variables);
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

}
