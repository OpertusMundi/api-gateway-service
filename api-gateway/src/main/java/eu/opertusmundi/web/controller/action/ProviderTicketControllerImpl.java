package eu.opertusmundi.web.controller.action;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.account.AccountTicketCommandDto;
import eu.opertusmundi.common.model.account.AccountTicketDto;
import eu.opertusmundi.common.model.account.EnumCustomerType;
import eu.opertusmundi.common.model.account.EnumTicketStatus;
import eu.opertusmundi.common.service.TicketService;

@RestController
public class ProviderTicketControllerImpl extends BaseController implements ProviderTicketController {

    private final TicketService ticketService;

    @Autowired
    public ProviderTicketControllerImpl(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Override
    public RestResponse<?> find(Integer pageIndex, Integer pageSize, Set<EnumTicketStatus> status) {
        final var result = this.ticketService.findAll(this.currentUserKey(), pageIndex, pageSize, status, false);

        return RestResponse.result(result);
    }

    @Override
    public RestResponse<?> openTicket(AccountTicketCommandDto command, BindingResult validationResult) {
        command.setUserKey(this.currentUserKey());
        command.setCustomerType(EnumCustomerType.PROVIDER);

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        final AccountTicketDto result = this.ticketService.create(command);

        return RestResponse.result(result);
    }

}
