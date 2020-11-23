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
import eu.opertusmundi.common.model.dto.CustomerCommandDto;
import eu.opertusmundi.common.service.ConsumerRegistrationService;
import eu.opertusmundi.web.validation.ConsumerValidator;

@RestController
public class ConsumerRegistrationControllerImpl extends BaseController implements ConsumerRegistrationController {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerRegistrationControllerImpl.class);

    @Autowired
    private ConsumerRegistrationService consumerService;

    @Autowired
    private ConsumerValidator consumerValidator;

    @Override
    public RestResponse<AccountProfileDto> updateRegistration(CustomerCommandDto command, BindingResult validationResult) {
        return this.update(command, validationResult, true);
    }

    @Override
    public RestResponse<AccountProfileDto> submitRegistration(CustomerCommandDto command, BindingResult validationResult) {
        return this.update(command, validationResult, false);
    }

    @Override
    public RestResponse<AccountProfileDto> cancelRegistration() {
        final UUID userKey = this.authenticationFacade.getCurrentUserKey();

        try {
            final AccountDto account = this.consumerService.cancelRegistration(userKey);

            return RestResponse.result(account.getProfile());
        } catch (final IllegalArgumentException argEx) {
            return RestResponse.error(BasicMessageCode.InternalServerError, argEx.getMessage());
        } catch (final Exception ex) {
            logger.error("Consumer update has failed", ex);

            return RestResponse.error(BasicMessageCode.InternalServerError, "An unknown error has occurred");
        }
    }

    private RestResponse<AccountProfileDto> update(CustomerCommandDto command, BindingResult validationResult, boolean draft) {
        final Integer id = this.authenticationFacade.getCurrentUserId();

        // Inject user id (id property is always ignored during serialization)
        command.setUserId(id);

        this.consumerValidator.validate(command, validationResult);

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        try {
            final AccountDto account = draft ?
                this.consumerService.updateRegistration(command) :
                this.consumerService.submitRegistration(command);

            return RestResponse.result(account.getProfile());
        } catch (final IllegalArgumentException argEx) {
            return RestResponse.error(BasicMessageCode.InternalServerError, argEx.getMessage());
        } catch (final Exception ex) {
            logger.error("Consumer update has failed", ex);

            return RestResponse.error(BasicMessageCode.InternalServerError, "An unknown error has occurred");
        }
    }

}
