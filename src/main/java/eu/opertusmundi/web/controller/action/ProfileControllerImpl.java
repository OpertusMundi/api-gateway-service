package eu.opertusmundi.web.controller.action;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.dto.AccountDto;
import eu.opertusmundi.common.model.dto.AccountProfileDto;
import eu.opertusmundi.common.model.dto.AccountProfileUpdateCommandDto;
import eu.opertusmundi.common.model.dto.AddressCommandDto;
import eu.opertusmundi.web.domain.AccountEntity;
import eu.opertusmundi.web.repository.AccountRepository;
import eu.opertusmundi.web.security.UserService;
import eu.opertusmundi.web.validation.AddressValidator;
import eu.opertusmundi.web.validation.ProfileValidator;


/**
 * Actions for querying and updating user data
 */
@RestController
public class ProfileControllerImpl extends BaseController implements ProfileController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ProfileValidator profileValidator;

    @Autowired
    private AddressValidator addressValidator;

    @Override
    public RestResponse<AccountProfileDto> getProfile() {
        final String email = this.authenticationFacade.getCurrentUserEmail();

        // Refresh profile for each request since the account object stored in the
        // security context may have stale data
        final AccountEntity account = this.accountRepository.findOneByEmail(email).orElse(null);

        return RestResponse.result(account == null || account.getProfile() == null ? null : account.getProfile().toDto());
    }

    @Override
    public RestResponse<AccountProfileDto> updateProfile(AccountProfileUpdateCommandDto command, BindingResult validationResult) {
        final Integer id = this.authenticationFacade.getCurrentUserId();

        // Inject user id (id property is always ignored during serialization)
        command.setId(id);

        this.profileValidator.validate(command, validationResult);

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        try {
            final AccountDto account = this.userService.updateProfile(command);

            return RestResponse.result(account.getProfile());
        } catch (final Exception ex) {
            return RestResponse.error(BasicMessageCode.InternalServerError, "An unknown error has occurred");
        }
    }

    @Override
    public RestResponse<AccountProfileDto> createAddress(AddressCommandDto command, BindingResult validationResult) {
        final Integer id = this.authenticationFacade.getCurrentUserId();

        // Inject user id (id property is always ignored during serialization)
        command.setId(id);

        this.addressValidator.validate(command, validationResult);

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        try {
            final AccountDto account = this.userService.createAddress(command);

            return RestResponse.result(account.getProfile());
        } catch (final Exception ex) {
            return RestResponse.error(BasicMessageCode.InternalServerError, "An unknown error has occurred");
        }
    }

    @Override
    public RestResponse<AccountProfileDto> updateAddress(UUID key, AddressCommandDto command, BindingResult validationResult) {
        final Integer id = this.authenticationFacade.getCurrentUserId();

        // Inject user id (id property is always ignored during serialization)
        command.setId(id);
        command.setKey(key);

        this.addressValidator.validate(command, validationResult);

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        try {
            final AccountDto account = this.userService.updateAddress(command);

            return RestResponse.result(account.getProfile());
        } catch (final Exception ex) {
            return RestResponse.error(BasicMessageCode.InternalServerError, "An unknown error has occurred");
        }
    }

    @Override
    public RestResponse<AccountProfileDto> deleteAddress(UUID key) {
        final Integer id = this.authenticationFacade.getCurrentUserId();

        // Inject user id (id property is always ignored during serialization)
        final AddressCommandDto command = new AddressCommandDto();
        command.setId(id);
        command.setKey(key);

        try {
            final AccountDto account = this.userService.deleteAddress(command);

            return RestResponse.result(account.getProfile());
        } catch (final Exception ex) {
            return RestResponse.error(BasicMessageCode.InternalServerError, "An unknown error has occurred");
        }
    }

}
