package eu.opertusmundi.web.controller.action;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.dto.AccountDto;
import eu.opertusmundi.common.model.dto.AccountProfileDto;
import eu.opertusmundi.common.model.dto.ProviderProfessionalCommandDto;
import eu.opertusmundi.common.service.ProviderRegistrationService;
import eu.opertusmundi.web.validation.ProviderValidator;

@RestController
public class ProviderRegistrationControllerImpl extends BaseController implements ProviderRegistrationController {

    private static final Logger logger = LoggerFactory.getLogger(ProviderRegistrationControllerImpl.class);

    @Autowired
    private ProviderRegistrationService providerService;

    @Autowired
    private ProviderValidator providerValidator;

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

        final UUID userKey = this.authenticationFacade.getCurrentUserKey();

        try {
            final AccountDto account = this.providerService.cancelRegistration(userKey);

            return RestResponse.result(account.getProfile());
        } catch (final IllegalArgumentException argEx) {
            return RestResponse.error(BasicMessageCode.InternalServerError, argEx.getMessage());
        } catch (final Exception ex) {
            logger.error("Provider update has failed", ex);

            return RestResponse.error(BasicMessageCode.InternalServerError, "An unknown error has occurred");
        }
    }

    private RestResponse<AccountProfileDto> update(
        ProviderProfessionalCommandDto command, BindingResult validationResult, boolean draft
    ) {
        final Integer id = this.authenticationFacade.getCurrentUserId();

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
            logger.error("Provider update has failed", ex);

            return RestResponse.error(BasicMessageCode.InternalServerError, "An unknown error has occurred");
        }
    }

}
