package eu.opertusmundi.web.controller.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.dto.AccountDto;
import eu.opertusmundi.common.model.dto.AccountProfileConsumerCommandDto;
import eu.opertusmundi.common.model.dto.AccountProfileDto;
import eu.opertusmundi.web.service.ConsumerService;
import eu.opertusmundi.web.validation.ConsumerValidator;

@RestController
public class ConsumerControllerImpl extends BaseController implements ConsumerController {

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private ConsumerValidator consumerValidator;

    @Override
    public RestResponse<AccountProfileDto> update(AccountProfileConsumerCommandDto command, BindingResult validationResult) {
        final Integer id = this.authenticationFacade.getCurrentUserId();

        // Inject user id (id property is always ignored during serialization)
        command.setId(id);

        this.consumerValidator.validate(command, validationResult);

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        try {
            final AccountDto account = this.consumerService.updateConsumer(command);

            return RestResponse.result(account.getProfile());
        } catch (final Exception ex) {
            return RestResponse.error(BasicMessageCode.InternalServerError, "An unknown error has occurred");
        }
    }


}
