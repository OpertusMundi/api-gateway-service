package eu.opertusmundi.web.security;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
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
import eu.opertusmundi.common.domain.HelpdeskAccountEntity;
import eu.opertusmundi.common.feign.client.EmailServiceFeignClient;
import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.EnumAuthProvider;
import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.ServiceResponse;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.AccountProfileCommandDto;
import eu.opertusmundi.common.model.account.ActivationTokenCommandDto;
import eu.opertusmundi.common.model.account.ActivationTokenDto;
import eu.opertusmundi.common.model.account.EnumActivationStatus;
import eu.opertusmundi.common.model.account.EnumActivationTokenType;
import eu.opertusmundi.common.model.account.JoinVendorCommandDto;
import eu.opertusmundi.common.model.account.PlatformAccountCommandDto;
import eu.opertusmundi.common.model.account.VendorAccountCommandDto;
import eu.opertusmundi.common.model.analytics.ProfileRecord;
import eu.opertusmundi.common.model.email.EmailAddressDto;
import eu.opertusmundi.common.model.email.EnumMailType;
import eu.opertusmundi.common.model.email.MailMessageCode;
import eu.opertusmundi.common.model.email.MessageDto;
import eu.opertusmundi.common.model.file.QuotaDto;
import eu.opertusmundi.common.model.workflow.EnumProcessInstanceVariable;
import eu.opertusmundi.common.repository.AccountRecentSearchRepository;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.ActivationTokenRepository;
import eu.opertusmundi.common.repository.HelpdeskAccountRepository;
import eu.opertusmundi.common.service.ElasticSearchService;
import eu.opertusmundi.common.service.UserFileManager;
import eu.opertusmundi.common.service.messaging.MailMessageHelper;
import eu.opertusmundi.common.service.messaging.MailModelBuilder;
import eu.opertusmundi.common.util.BpmEngineUtils;
import eu.opertusmundi.common.util.BpmInstanceVariablesBuilder;
import eu.opertusmundi.common.util.ImageUtils;
import eu.opertusmundi.web.model.security.CreateAccountResult;
import eu.opertusmundi.web.model.security.PasswordChangeCommandDto;
import feign.FeignException;

@Service
public class DefaultUserService implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultUserService.class);

    private static final String WORKFLOW_ACCOUNT_REGISTRATION = "account-registration";

    private static final String MESSAGE_EMAIL_VERIFIED = "email-verified-message";

    private static final String WORKFLOW_VENDOR_ACCOUNT_REGISTRATION = "vendor-account-registration";

    /**
     * Activation token duration in hours
     */
    @Value("${user.service.token-duration:360}")
    private int tokenDuration;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private HelpdeskAccountRepository helpdeskAccountRepository;

    @Autowired
    private ActivationTokenRepository activationTokenRepository;

    @Autowired
    private AccountRecentSearchRepository recentSearchRepository;

    @Autowired
    private UserFileManager fileManager;

    @Autowired
    private BpmEngineUtils bpmEngine;

    @Autowired
    private ImageUtils imageUtils;

    @Autowired(required = false)
    private ElasticSearchService elasticSearchService;

    @Autowired
    private MailMessageHelper messageHelper;

    @Autowired
    private ObjectProvider<EmailServiceFeignClient> mailClient;

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

        // Get user file system quota
        final QuotaDto quota = fileManager.getQuota(username);
        account.getProfile().setQuota(quota);

        // Get user recent search keywords
        final List<String> recentSearches = recentSearchRepository.findAllObjectsByAccount(account.getId()).stream()
            .map(r -> r.getValue())
            .collect(Collectors.toList());
        account.getProfile().setRecentSearches(recentSearches);

        return Optional.ofNullable(account == null ? null : account);
    }

    @Override
    @Transactional
    public Optional<AccountDto> findOneByUserName(String username, EnumAuthProvider provider) throws UsernameNotFoundException {
        final AccountEntity account = this.accountRepository.findOneByEmailAndProvider(username, provider).orElse(null);

        return Optional.ofNullable(account == null ? null : account.toDto());
    }

    @Override
    @Transactional
    public ServiceResponse<CreateAccountResult> createPlatformAccount(PlatformAccountCommandDto command) {
        command.getProfile().setImage(imageUtils.resizeImage(
            command.getProfile().getImage(), command.getProfile().getImageMimeType()
        ));

        final CreateAccountResult result = this.createPlatformAccountRecord(command);

        this.startPlatformAccountRegistrationWorkflow(result);

        return ServiceResponse.result(result);
    }

    private CreateAccountResult createPlatformAccountRecord(PlatformAccountCommandDto command) {
        // Create account
        final AccountDto account = this.accountRepository.create(command);

        // Create activation token for account email
        final ActivationTokenCommandDto tokenCommand = ActivationTokenCommandDto.of(command.getEmail());

        // Do not send the token by mail. If a workflow is started, it will also
        // send a mail with the token
        final ServiceResponse<ActivationTokenDto> tokenResponse = this.createToken(EnumActivationTokenType.ACCOUNT, tokenCommand, false);

        // Update account profile
        if (elasticSearchService != null) {
            elasticSearchService.addProfile(ProfileRecord.from(account));
        }

        return CreateAccountResult.of(account, tokenResponse.getResult());
    }

    private void startPlatformAccountRegistrationWorkflow(CreateAccountResult result) {
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
                    .variableAsString("tokenType", result.getToken().getType().toString())
                    .build();

                instance = bpmEngine.startProcessDefinitionByKey(
                    WORKFLOW_ACCOUNT_REGISTRATION, accountKey, variables
                );
            }

            this.accountRepository.setRegistrationWorkflowInstance(accountId, instance.getDefinitionId(), instance.getId());
        } catch (final Exception ex) {
            // Allow workflow instance initialization to fail
            logger.warn(String.format("Failed to start account registration workflow instance [accountKey=%s]", accountKey), ex);
        }
    }

    @Override
    @Transactional
    public ServiceResponse<AccountDto> createVendorAccount(VendorAccountCommandDto command) {
        command.getProfile().setImage(imageUtils.resizeImage(command.getProfile().getImage(), command.getProfile().getImageMimeType()));

        final AccountDto account = this.accountRepository.create(command);

        // Update account profile
        if (elasticSearchService != null) {
            elasticSearchService.addProfile(ProfileRecord.from(account));
        }

        return ServiceResponse.result(account);
    }

    @Override
    @Transactional
    public ServiceResponse<AccountDto> updateVendorAccount(VendorAccountCommandDto command) {
        command.getProfile().setImage(imageUtils.resizeImage(command.getProfile().getImage(), command.getProfile().getImageMimeType()));

        final AccountDto account = this.accountRepository.update(command);

        // Update account profile
        if (elasticSearchService != null) {
            elasticSearchService.addProfile(ProfileRecord.from(account));
        }

        return ServiceResponse.result(account);
    }

    private ActivationTokenDto startVendorAccountInvitationWorkflow(Integer userId, UUID userKey, String email) {
        try {
            // Create activation token for account email
            final ActivationTokenCommandDto tokenCommand = ActivationTokenCommandDto.of(email);

            // Do not send token by mail. The workflow will send the token by
            // mail
            final ServiceResponse<ActivationTokenDto> tokenResponse = this.createToken(EnumActivationTokenType.VENDOR_ACCOUNT, tokenCommand, false);

            ProcessInstanceDto instance = this.bpmEngine.findInstance(userKey);

            if (instance == null) {
                final Map<String, VariableValueDto> variables = BpmInstanceVariablesBuilder.builder()
                    .variableAsString(EnumProcessInstanceVariable.START_USER_KEY.getValue(), userKey.toString())
                    .variableAsString("userKey", userKey.toString())
                    .variableAsString("activationToken", tokenResponse.getResult().getToken().toString())
                    .variableAsString("tokenType", tokenResponse.getResult().getType().toString())
                    .build();

                instance = bpmEngine.startProcessDefinitionByKey(
                    WORKFLOW_VENDOR_ACCOUNT_REGISTRATION, userKey.toString(), variables
                );
            }

            this.accountRepository.setRegistrationWorkflowInstance(userId, instance.getDefinitionId(), instance.getId());

            return tokenResponse.getResult();
        } catch (final Exception ex) {
            // Allow workflow instance initialization to fail
            logger.warn(String.format("Failed to start vendor account invitation workflow instance [accountKey=%s]", userKey), ex);
        }

        return null;
    }

    @Override
    @Transactional
    public ServiceResponse<ActivationTokenDto> createToken(EnumActivationTokenType type, ActivationTokenCommandDto command, boolean sendMail) {
        final AccountEntity account = this.accountRepository.findOneByEmail(command.getEmail()).orElse(null);

        logger.info("Activation token requested. [email={}]", command.getEmail());

        if (account == null) {
            logger.info("Activation token request has failed. Account was not found. [email={}]", command.getEmail());

            return ServiceResponse.success();
        }

        // By default send an account token
        if (type == null) {
            type = EnumActivationTokenType.ACCOUNT;
        }
        // For organizational accounts, only tokens of type VENDOR_ACCOUNT are
        // supported
        if (account.getParent() != null) {
            type = EnumActivationTokenType.VENDOR_ACCOUNT;
        }

        switch (type) {
            case ACCOUNT :
            case VENDOR_ACCOUNT :
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
        final ActivationTokenDto token = this.activationTokenRepository.create(
            account.getId(), command.getEmail(), this.tokenDuration, type
        );
        // Send by mail
        // TODO: Add support for consumer/provider mails
        if (sendMail) {
            this.sendTokenByMail(
                type == EnumActivationTokenType.VENDOR_ACCOUNT
                    ? EnumMailType.VENDOR_ACCOUNT_INVITATION
                    : EnumMailType.ACCOUNT_ACTIVATION_TOKEN,
                type,
                account.getKey()
            );
        }

        return ServiceResponse.result(token);
    }

    @Override
    @Transactional
    public ServiceResponse<AccountDto> invite(UUID vendorKey, UUID accountKey) {
        final AccountEntity account = this.accountRepository.findOneByParentAndKey(vendorKey, accountKey).orElse(null);

        if (account == null) {
            // The account must exist
            logger.warn("Vendor account invite request has failed. Account was not found. [key={}]", accountKey);

            return ServiceResponse.error(BasicMessageCode.AccountNotFound, "Account was not found");
        }

        logger.info("Invite vendor account. [email={}]", account.getEmail());

        // If a registration workflow instance does not exist, create one;
        // Otherwise, send a new token by email
        if (StringUtils.isBlank(account.getProcessInstance())) {
            this.startVendorAccountInvitationWorkflow(account.getId(), account.getKey(), account.getEmail());
        } else {
            // Create activation token
            this.activationTokenRepository.create(
                account.getId(), account.getEmail(), this.tokenDuration, EnumActivationTokenType.VENDOR_ACCOUNT
            );

            // Send token by mail
            this.sendTokenByMail(EnumMailType.VENDOR_ACCOUNT_INVITATION, EnumActivationTokenType.VENDOR_ACCOUNT, account.getKey());
        }

        return ServiceResponse.result(account.toDto());
    }

    @Override
    @Transactional
    public ServiceResponse<Void> joinOrganization(JoinVendorCommandDto command) {
        // Validate token
        final ActivationTokenEntity tokenEntity = this.activationTokenRepository.findOneByKey(command.getToken()).orElse(null);

        if (tokenEntity == null) {
            return ServiceResponse.error(BasicMessageCode.TokenNotFound, "Token was not found");
        }
        if (tokenEntity.isExpired()) {
            return ServiceResponse.error(BasicMessageCode.TokenIsExpired, "Token has expired");
        }
        if (tokenEntity.getType() != EnumActivationTokenType.VENDOR_ACCOUNT) {
            return ServiceResponse.error(BasicMessageCode.TokenTypeNotSupported, "Token type is not supported");
        }

        // Validate account
        final ZonedDateTime now           = ZonedDateTime.now();
        final AccountEntity accountEntity = tokenEntity.getAccount();
        boolean             sendMessage   = false;

        if (accountEntity == null) {
            return ServiceResponse.error(BasicMessageCode.AccountNotFound, "Account was not found");
        }
        if (!tokenEntity.getEmail().equals(accountEntity.getEmail())) {
            return ServiceResponse.error(BasicMessageCode.EmailNotFound, "Email not registered to the account");
        }

        // Update database
        this.activationTokenRepository.redeem(tokenEntity);
        this.changePassword(command);

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

        this.accountRepository.saveAndFlush(accountEntity);

        if (sendMessage) {
            this.sendTokenToProcessInstance(accountEntity.getKey(), command.getToken());
        }

        return ServiceResponse.success();
    }

    @Override
    @Transactional
    public ServiceResponse<AccountDto> enableVendorAccount(UUID vendorKey, UUID accountKey) {
        AccountDto account = this.accountRepository.findOneByParentAndKey(vendorKey, accountKey)
            .map(AccountEntity::toDto)
            .orElse(null);

        if (account == null) {
            // The account must exist
            logger.warn("Vendor account invite request has failed. Account was not found. [key={}]", accountKey);

            return ServiceResponse.error(BasicMessageCode.AccountNotFound, "Account was not found");
        }

        // If the account registration workflow has not yet been initialized,
        // reject the request
        if (account.getActivationStatus() == EnumActivationStatus.UNDEFINED) {
            return ServiceResponse.error(
                BasicMessageCode.BadRequest,
                String.format("Invalid account status [expected=COMPLETED, found=%s]", account.getActivationStatus())
            );
        }

        // If the account registration is pending, ignore request. When the
        // registration workflow completes, the account will be automatically
        // activated
        if (account.getActivationStatus() == EnumActivationStatus.PENDING ||
            account.getActivationStatus() == EnumActivationStatus.PROCESSING
        ) {
            return ServiceResponse.result(account);
        }

        account = this.accountRepository.setVendorAccountActive(vendorKey, accountKey, true);

        return ServiceResponse.result(account);
    }

    @Override
    @Transactional
    public ServiceResponse<AccountDto> disableVendorAccount(UUID vendorKey, UUID accountKey) {
        AccountDto account = this.accountRepository.findOneByParentAndKey(vendorKey, accountKey)
            .map(AccountEntity::toDto)
            .orElse(null);

        if (account == null) {
            // The account must exist
            logger.warn("Vendor account invite request has failed. Account was not found. [key={}]", accountKey);

            return ServiceResponse.error(BasicMessageCode.AccountNotFound, "Account was not found");
        }

        // If the account registration is pending, ignore request. When the
        // registration workflow completes, the account will be automatically
        // activated
        if (account.getActivationStatus() == EnumActivationStatus.PENDING ||
            account.getActivationStatus() == EnumActivationStatus.PROCESSING
        ) {
            return ServiceResponse.result(account);
        }

        account = this.accountRepository.setVendorAccountActive(vendorKey, accountKey, false);

        return ServiceResponse.result(account);
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
            case VENDOR_ACCOUNT :
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
        command.setImage(imageUtils.resizeImage(command.getImage(), command.getImageMimeType()));

        final AccountDto account = this.accountRepository.updateProfile(command);

        // Update account profile
        if (elasticSearchService != null) {
            elasticSearchService.addProfile(ProfileRecord.from(account));
        }

        return account;
    }

    @Override
    @Transactional
    public void grant(AccountDto account, AccountDto grantedBy, EnumRole... roles) {
        Assert.notNull(account, "Expected a non-null account");
        Assert.notEmpty(roles, "Expected at least 1 role");

        final Optional<AccountEntity> accountEntity = this.accountRepository.findById(account.getId());
        if (!accountEntity.isPresent()) {
            return;
        }

        final Optional<AccountEntity> grantedByEntity = grantedBy != null ? this.accountRepository.findById(grantedBy.getId())
                : Optional.empty();

        for (final EnumRole role : roles) {
            accountEntity.get().grant(role, grantedByEntity.isPresent() ? grantedByEntity.get() : null);
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
        this.changePassword(command.getUserName(), command.getCurrentPassword(), command.getNewPassword());
    }

    @Override
    @Transactional
    public void changePassword(JoinVendorCommandDto command) {
        this.changePassword(command.getEmail(), null, command.getPassword());
    }

    private void changePassword(String email, @Nullable String currentPassword, String newPassword) {
        final AccountEntity account = this.accountRepository.findOneByUsername(email).orElse(null);

        if (account == null) {
            throw new UsernameNotFoundException(email);
        }

        final PasswordEncoder encoder = new BCryptPasswordEncoder();

        if (currentPassword != null && !encoder.matches(currentPassword, account.getPassword())) {
            throw new BadCredentialsException(email);
        }

        account.setPassword(encoder.encode(newPassword));
        this.accountRepository.saveAndFlush(account);

        // TODO: Send mail

        logger.info("Password changed. [user={}]", account.getUserName());
    }


    private void sendTokenByMail(EnumMailType mailType, EnumActivationTokenType tokenType, UUID recipientKey) {
        // Resolve recipient
        final AccountEntity account = accountRepository.findOneByKey(recipientKey).orElse(null);
        if (account == null) {
            throw new ServiceException(
                MailMessageCode.RECIPIENT_NOT_FOUND,
                String.format("Recipient was not found [userKey=%s]", recipientKey)
            );
        }
        // Compose message
        final MailModelBuilder builder = MailModelBuilder.builder()
            .add("userKey", recipientKey.toString())
            .add("tokenType", tokenType.toString());

        final Map<String, Object>             model    = this.messageHelper.createModel(mailType, builder);
        final EmailAddressDto                 sender   = this.messageHelper.getSender(mailType, model);
        final String                          subject  = this.messageHelper.composeSubject(mailType, model);
        final String                          template = this.messageHelper.resolveTemplate(mailType, model);
        final MessageDto<Map<String, Object>> message  = new MessageDto<>();

        message.setSender(sender);
        message.setSubject(subject);
        message.setTemplate(template);
        message.setModel(model);

        message.setRecipients(builder.getAddress());

        try {
            final ResponseEntity<BaseResponse> response = this.mailClient.getObject().sendMail(message);

            if (!response.getBody().getSuccess()) {
                throw new ServiceException(
                    MailMessageCode.SEND_MAIL_FAILED,
                    String.format("Failed to send mail [userKey=%s]", recipientKey)
                );
            }
        } catch (final FeignException fex) {
            throw new ServiceException(
                MailMessageCode.SEND_MAIL_FAILED,
                String.format("Failed to send mail [userKey=%s]", recipientKey),
                fex
            );
        }
    }

}
