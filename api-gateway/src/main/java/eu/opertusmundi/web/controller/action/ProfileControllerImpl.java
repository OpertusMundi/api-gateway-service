package eu.opertusmundi.web.controller.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.AccountProfileCommandDto;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.web.security.UserService;
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

    @Override
    public RestResponse<AccountDto> getProfile() {
        if (!this.hasRole(EnumRole.ROLE_USER)) {
            return RestResponse.accessDenied();
        }

        final String email = this.currentUserEmail();

        // Refresh profile for each request since the account object stored in the
        // security context may have stale data
        final AccountEntity account = this.accountRepository.findOneByEmail(email).get();

        return RestResponse.result(account.toDto());
    }

    @Override
    public RestResponse<AccountDto> updateProfile(AccountProfileCommandDto command, BindingResult validationResult) {
        final Integer id = this.currentUserId();

        // Inject user id (id property is always ignored during serialization)
        command.setId(id);

        this.profileValidator.validate(command, validationResult);

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        final AccountDto account = this.userService.updateProfile(command);

        return RestResponse.result(account);
    }

}
