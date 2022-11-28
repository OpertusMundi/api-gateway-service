package eu.opertusmundi.web.controller.api;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.EnumRole;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.web.controller.action.BaseController;
import eu.opertusmundi.web.security.UserService;

/**
 * Actions for querying and updating user data
 */
@RestController("ApiProfileControllerImpl")
public class ProfileControllerImpl extends BaseController implements ProfileController {

    @Autowired
    private UserService userService;

    @Override
    public RestResponse<AccountDto> getProfile() {
        final String email = this.currentUserEmail();

        // Refresh profile for each request since the account object stored in
        // the security context may have stale data
        final AccountDto account = this.userService.findOneByUserName(email).get();
        // Override roles
        account.setRoles(Set.of(EnumRole.ROLE_API));
        // Remove draft data
        final var consumer = account.getProfile().getConsumer();
        if (consumer != null) {
            consumer.setDraft(null);
        }
        final var provider = account.getProfile().getProvider();
        if (provider != null) {
            provider.setDraft(null);
        }

        return RestResponse.result(account);
    }

}
