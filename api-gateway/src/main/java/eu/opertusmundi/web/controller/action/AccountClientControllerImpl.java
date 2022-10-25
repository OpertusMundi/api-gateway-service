package eu.opertusmundi.web.controller.action;

import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.account.AccountClientCommandDto;
import eu.opertusmundi.common.model.account.AccountClientDto;
import eu.opertusmundi.common.model.account.EnumAccountClientStatus;
import eu.opertusmundi.common.service.AccountClientService;

@RestController
public class AccountClientControllerImpl extends BaseController implements AccountClientController {

    private final AccountClientService accountClientService;

    @Autowired
    public AccountClientControllerImpl(AccountClientService accountClientService) {
        this.accountClientService = accountClientService;
    }

    @Override
    public RestResponse<PageResultDto<AccountClientDto>> find(
        Integer page, @Max(100) @Min(1) Integer size, EnumAccountClientStatus status
    ) {
        final PageResultDto<AccountClientDto> result = this.accountClientService.findAll(this.currentUserKey(), page, size, status);

        return RestResponse.result(result);
    }

    @Override
    public BaseResponse create(@Valid AccountClientCommandDto command, BindingResult validationResult) {
        try {
            command.setAccountId(this.currentUserId());

            if (validationResult.hasErrors()) {
                return RestResponse.invalid(validationResult.getFieldErrors());
            }

            final AccountClientDto result = this.accountClientService.create(command);

            return RestResponse.result(result);
        } catch (final ServiceException ex) {
            return RestResponse.failure(ex);
        }
    }

    @Override
    public BaseResponse revoke(UUID key) {
        try {
            this.accountClientService.revoke(this.currentUserId(), key);

            return RestResponse.success();
        } catch (final ServiceException ex) {
            return RestResponse.failure(ex);
        }
    }

}
