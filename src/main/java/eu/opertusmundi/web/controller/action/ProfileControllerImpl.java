package eu.opertusmundi.web.controller.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.dto.AccountDto;
import eu.opertusmundi.common.model.dto.AccountProfileCommandDto;
import eu.opertusmundi.common.model.dto.AccountProfileDto;
import eu.opertusmundi.web.domain.AccountEntity;
import eu.opertusmundi.web.repository.AccountRepository;
import eu.opertusmundi.web.service.UserService;
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
    public RestResponse<AccountProfileDto> getProfile() {
        final String email = this.authenticationFacade.getCurrentUserEmail();

        // Refresh profile for each request since the account object stored in the
        // security context may have stale data
        final AccountEntity account = this.accountRepository.findOneByEmail(email).orElse(null);

        return RestResponse.result(account == null || account.getProfile() == null ? null : account.getProfile().toDto());
    }

    @Override
    public RestResponse<AccountProfileDto> updateProfile(AccountProfileCommandDto command, BindingResult validationResult) {
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

}
