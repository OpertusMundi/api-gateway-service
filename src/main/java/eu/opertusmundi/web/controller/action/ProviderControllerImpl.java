package eu.opertusmundi.web.controller.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.dto.AccountDto;
import eu.opertusmundi.common.model.dto.AccountProfileDto;
import eu.opertusmundi.common.model.dto.AccountProfileProviderCommandDto;
import eu.opertusmundi.web.service.ProviderService;
import eu.opertusmundi.web.validation.ProviderValidator;

@RestController
public class ProviderControllerImpl extends BaseController implements ProviderController {

    private static final Logger logger = LoggerFactory.getLogger(ProviderControllerImpl.class);

    @Autowired
    private ProviderService providerService;

    @Autowired
    private ProviderValidator providerValidator;

    @Override
    public RestResponse<AccountProfileDto> submitRegistration(
        AccountProfileProviderCommandDto command, BindingResult validationResult
    ) {
        return this.updateRegistration(command, validationResult, true);
    }

    @Override
    public RestResponse<AccountProfileDto> updateRegistration(
        AccountProfileProviderCommandDto command, BindingResult validationResult
    ) {
        return this.updateRegistration(command, validationResult, false);
    }

    @Override
    public RestResponse<AccountProfileDto> cancelRegistration() {
        final Integer id = this.authenticationFacade.getCurrentUserId();

        try {
            final AccountDto account = this.providerService.cancelProviderRegistration(id);

            return RestResponse.result(account.getProfile());
        } catch (final Exception ex) {
            logger.error("Cancel operation has failed", ex);

            return RestResponse.error(BasicMessageCode.InternalServerError, "An unknown error has occurred");
        }
    }

    @Override
    public RestResponse<AccountProfileDto> completeRegistration() {
        final Integer id = this.authenticationFacade.getCurrentUserId();

        try {
            final AccountDto account = this.providerService.completeProviderRegistration(id);

            return RestResponse.result(account.getProfile());
        } catch (final IllegalArgumentException argEx) {
            return RestResponse.error(BasicMessageCode.InternalServerError, argEx.getMessage());
        } catch (final Exception ex) {
            logger.error("Complete operation has failed", ex);

            return RestResponse.error(BasicMessageCode.InternalServerError, "An unknown error has occurred");
        }
    }

    private RestResponse<AccountProfileDto> updateRegistration(
        AccountProfileProviderCommandDto command, BindingResult validationResult, boolean submit
    ) {
        final Integer id = this.authenticationFacade.getCurrentUserId();

        // Inject user id (id property is always ignored during serialization)
        command.setId(id);

        this.providerValidator.validate(command, validationResult);

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        try {
            final AccountDto account = this.providerService.updateProviderRegistration(command, submit);

            return RestResponse.result(account.getProfile());
        } catch (final IllegalArgumentException argEx) {
            return RestResponse.error(BasicMessageCode.InternalServerError, argEx.getMessage());
        } catch (final Exception ex) {
            logger.error("Provider update has failed", ex);

            return RestResponse.error(BasicMessageCode.InternalServerError, "An unknown error has occurred");
        }
    }

}
