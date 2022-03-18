package eu.opertusmundi.web.controller.action;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.domain.CustomerEntity;
import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.AccountProfileDto;
import eu.opertusmundi.common.model.account.ProviderProfessionalCommandDto;
import eu.opertusmundi.common.model.account.ProviderProfileCommandDto;
import eu.opertusmundi.common.repository.CustomerRepository;
import eu.opertusmundi.common.service.ProviderRegistrationService;
import eu.opertusmundi.common.util.TextUtils;
import eu.opertusmundi.common.util.ViesVatClient;
import eu.opertusmundi.web.validation.ProviderValidator;

@RestController
public class ProviderRegistrationControllerImpl extends BaseController implements ProviderRegistrationController {

    private static final Logger logger = LoggerFactory.getLogger(ProviderRegistrationControllerImpl.class);

    private final ProviderRegistrationService providerService;

    private final ProviderValidator providerValidator;

    private final CustomerRepository customerRepository;

    private final ViesVatClient viesVatClient;

    @Autowired
    public ProviderRegistrationControllerImpl(
        CustomerRepository customerRepository,
        ProviderValidator providerValidator,
        ProviderRegistrationService providerService,
        ViesVatClient viesVatClient
    ) {
        this.customerRepository = customerRepository;
        this.providerValidator  = providerValidator;
        this.providerService    = providerService;
        this.viesVatClient      = viesVatClient;
    }

    @Override
    public RestResponse<AccountProfileDto> updateRegistration(
        ProviderProfessionalCommandDto command, BindingResult validationResult
    ) {
        this.ensureRegistered();

        return this.update(command, validationResult, true);
    }

    @Override
    public RestResponse<AccountProfileDto> submitRegistration(
        ProviderProfessionalCommandDto command, BindingResult validationResult
    ) {
        this.ensureRegistered();

        return this.update(command, validationResult, false);
    }

    @Override
    public RestResponse<AccountProfileDto> cancelRegistration() {
        this.ensureRegistered();

        final UUID userKey = this.currentUserKey();

        try {
            final AccountDto account = this.providerService.cancelRegistration(userKey);

            return RestResponse.result(account.getProfile());
        } catch (final IllegalArgumentException argEx) {
            return RestResponse.error(BasicMessageCode.InternalServerError, argEx.getMessage());
        } catch (final Exception ex) {
            logger.error(String.format("Provider update has failed. [userKey=%s]", userKey), ex);

            return RestResponse.error(BasicMessageCode.InternalServerError, "An unknown error has occurred");
        }
    }

    @Override
    public RestResponse<AccountProfileDto> updateProfile(ProviderProfileCommandDto command, BindingResult validationResult) {
        this.ensureRegistered();

        final Integer id = this.currentUserId();

        // Inject user id (id property is always ignored during serialization)
        command.setUserId(id);

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        try {
            final AccountDto account = this.providerService.updateProfile(command);

            return RestResponse.result(account.getProfile());
        } catch (final IllegalArgumentException argEx) {
            return RestResponse.error(BasicMessageCode.InternalServerError, argEx.getMessage());
        } catch (final Exception ex) {
            logger.error(String.format("Provider profile update has failed. [userId=%d]", id), ex);

            return RestResponse.error(BasicMessageCode.InternalServerError, "An unknown error has occurred");
        }
    }

    @Override
    public RestResponse<Boolean> validateCompanyName(String name) {
        // Name must be unique
        final CustomerEntity customerWithSameNamespace = this.customerRepository
            .findProviderByNamespaceAndAccountIdNot(TextUtils.slugify(name), this.currentUserId())
            .orElse(null);

        return RestResponse.result(Boolean.valueOf(customerWithSameNamespace == null));
    }

    @Override
    public RestResponse<Boolean> validateVatNumber(String vat, boolean vies) {
        final CustomerEntity customerWithSameCompanyNumber = this.customerRepository
            .findProviderByCompanyNumberAndAccountIdNot(vat, this.currentUserId())
            .orElse(null);

        if (customerWithSameCompanyNumber != null) {
            return RestResponse.result(Boolean.valueOf(false));
        }

        if (!vies) {
            return RestResponse.result(Boolean.valueOf(true));
        }

        final boolean result = this.viesVatClient.checkVatNumber(vat);

        return RestResponse.result(Boolean.valueOf(result));
    }

    private RestResponse<AccountProfileDto> update(
        ProviderProfessionalCommandDto command, BindingResult validationResult, boolean draft
    ) {
        final Integer id = this.currentUserId();

        // Inject user id (id property is always ignored during serialization)
        command.setUserId(id);

        this.providerValidator.validate(command, validationResult);

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        try {
            final AccountDto account = draft ?
                this.providerService.updateRegistration(command) :
                this.providerService.submitRegistration(command);

            return RestResponse.result(account.getProfile());
        } catch (final IllegalArgumentException argEx) {
            return RestResponse.error(BasicMessageCode.InternalServerError, argEx.getMessage());
        } catch (final Exception ex) {
            logger.error(String.format("Provider update has failed. [userId=%d]", id), ex);

            return RestResponse.error(BasicMessageCode.InternalServerError, "An unknown error has occurred");
        }
    }

}
