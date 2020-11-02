package eu.opertusmundi.web.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.EnumActivationTokenType;
import eu.opertusmundi.common.model.dto.AccountDto;
import eu.opertusmundi.common.model.dto.AccountProfileProviderCommandDto;
import eu.opertusmundi.common.model.dto.AccountProfileProviderDto;
import eu.opertusmundi.common.model.dto.ActivationTokenDto;
import eu.opertusmundi.web.feign.client.EmailServiceFeignClient;
import eu.opertusmundi.web.model.email.MailActivationModel;
import eu.opertusmundi.web.model.email.MessageDto;
import eu.opertusmundi.web.repository.AccountProfileHistoryRepository;
import eu.opertusmundi.web.repository.AccountRepository;
import eu.opertusmundi.web.repository.ActivationTokenRepository;
import feign.FeignException;

@Service
public class DefaultProviderService implements ProviderService {

    @Value("${opertus-mundi.base-url}")
    private String baseUrl;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountProfileHistoryRepository accountProfileHistoryRepository;

    @Autowired
    private ActivationTokenRepository activationTokenRepository;

    @Autowired
    private ObjectProvider<EmailServiceFeignClient> emailClient;

    @Override
    @Transactional
    public AccountDto updateProviderRegistration(AccountProfileProviderCommandDto command, boolean submit) {
        Assert.notNull(command, "Expected a non-null command");

        final AccountDto account = this.accountRepository.updateProviderRegistration(command, submit);

        return account;
    }

    @Override
    @Transactional
    public AccountDto rejectProviderRegistration(Integer userId) {
        final AccountDto account = this.accountRepository.rejectProviderRegistration(userId);

        return account;
    }

    @Override
    @Transactional
    public AccountDto acceptProviderRegistration(Integer userId) {
        final AccountDto account = this.accountRepository.acceptProviderRegistration(userId);

        return account;
    }

    @Override
    @Transactional
    public AccountDto cancelProviderRegistration(Integer userId) {
        final AccountDto account = this.accountRepository.cancelProviderRegistration(userId);

        return account;
    }

    @Override
    @Transactional
    public AccountDto completeProviderRegistration(Integer userId) {
        // Create history record
        this.accountProfileHistoryRepository.createSnapshot(userId);

        // Update profile provider data
        final AccountDto account = this.accountRepository.completeProviderRegistration(userId);

        // Check if provider (public) email requires validation
        final AccountProfileProviderDto provider = account.getProfile().getProvider().getCurrent();

        if (!StringUtils.isBlank(provider.getEmail()) && !provider.isEmailVerified()) {
            // Create activation token
            final ActivationTokenDto token = this.activationTokenRepository.create(
                account.getId(), provider.getEmail(), 24, EnumActivationTokenType.PROVIDER
            );
            // Send activation link to client
            this.sendMail(account.getProfile().getFullName(), token);
        }

        return account;
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
            // final BasicMessageCode code = BasicMessageCode.fromStatusCode(fex.status());

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
