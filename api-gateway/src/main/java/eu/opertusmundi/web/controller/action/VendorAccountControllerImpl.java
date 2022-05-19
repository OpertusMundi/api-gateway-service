package eu.opertusmundi.web.controller.action;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.EnumSortingOrder;
import eu.opertusmundi.common.model.PageResultDto;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.ServiceResponse;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.EnumAccountSortField;
import eu.opertusmundi.common.model.account.VendorAccountCommandDto;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.web.security.UserService;
import eu.opertusmundi.web.validation.AccountValidator;

@RestController
public class VendorAccountControllerImpl extends BaseController implements VendorAccountController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AccountValidator accountValidator;

    @Override
    public RestResponse<PageResultDto<AccountDto>> find(
        Integer page, Integer size, Boolean active, String email, EnumAccountSortField orderBy, EnumSortingOrder order
    ) {
        final Direction   direction   = order == EnumSortingOrder.DESC ? Direction.DESC : Direction.ASC;
        final PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, orderBy.getValue()));
        final String      filter      = "%" + email + "%";

        final Page<AccountDto> items = this.accountRepository.findAllVendorObjects(this.currentUserKey(), active, filter, pageRequest);

        final long                      count   = items.getTotalElements();
        final List<AccountDto>          records = items.stream().collect(Collectors.toList());
        final PageResultDto<AccountDto> result  = PageResultDto.of(page, size, records, count);

        return RestResponse.result(result);
    }

    @Override
    public BaseResponse create(VendorAccountCommandDto command, BindingResult validationResult) {
        command.setParentKey(this.currentUserKey());

        this.accountValidator.validate(command, validationResult);

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors(), validationResult.getGlobalErrors());
        }

        final AccountDto account = this.userService.createVendorAccount(command).getResult();

        return RestResponse.result(account);
    }

    @Override
    public BaseResponse update(UUID key, VendorAccountCommandDto command, BindingResult validationResult) {
        command.setKey(key);
        command.setParentKey(this.currentUserKey());

        this.accountValidator.validate(command, validationResult);

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors(), validationResult.getGlobalErrors());
        }

        final AccountDto account = this.userService.updateVendorAccount(command).getResult();

        return RestResponse.result(account);
    }

    @Override
    public BaseResponse delete(UUID key) {
        final UUID parentKey = this.currentUserKey();

        this.userService.deleteVendorAccount(parentKey, key);

        return BaseResponse.empty();
    }

    @Override
    public BaseResponse invite(UUID key) {
        final ServiceResponse<AccountDto> response = this.userService.invite(this.currentUserKey(), key);

        if (response.getResult() == null) {
            return RestResponse.error(response.getMessages());
        }

        return RestResponse.result(response.getResult());
    }

    @Override
    public BaseResponse acceptInvite(UUID token) {
        final ServiceResponse<Void> response = this.userService.joinOrganization(token);

        if (response.getResult() == null) {
            return RestResponse.error(response.getMessages());
        }

        return RestResponse.success();
    }

    @Override
    public BaseResponse enable(UUID key) {
        final ServiceResponse<AccountDto> response = this.userService.enableVendorAccount(this.currentUserKey(), key);

        if (response.getResult() == null) {
            return RestResponse.error(response.getMessages());
        }

        return RestResponse.result(response.getResult());
    }

    @Override
    public BaseResponse disable(UUID key) {
        final ServiceResponse<AccountDto> response = this.userService.disableVendorAccount(this.currentUserKey(), key);

        if (response.getResult() == null) {
            return RestResponse.error(response.getMessages());
        }

        return RestResponse.result(response.getResult());
    }

}
