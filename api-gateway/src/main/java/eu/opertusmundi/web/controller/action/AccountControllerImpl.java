package eu.opertusmundi.web.controller.action;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import eu.opertusmundi.common.model.BaseResponse;
import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.RestResponse;
import eu.opertusmundi.common.model.ServiceResponse;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.ActivationTokenCommandDto;
import eu.opertusmundi.common.model.account.ActivationTokenDto;
import eu.opertusmundi.common.model.account.PlatformAccountCommandDto;
import eu.opertusmundi.web.model.security.PasswordChangeCommandDto;
import eu.opertusmundi.web.model.security.Token;
import eu.opertusmundi.web.security.UserService;
import eu.opertusmundi.web.validation.AccountValidator;
import eu.opertusmundi.web.validation.PasswordCommandValidator;

@RestController
public class AccountControllerImpl extends BaseController implements AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountControllerImpl.class);

    @Autowired
    private UserService userService;

    @Autowired
    private AccountValidator accountValidator;

    @Autowired
    private PasswordCommandValidator passwordCommandValidator;

    @Override
    public RestResponse<Token> loggedIn(HttpSession session, CsrfToken token) {
        return RestResponse.result(new Token(token));
    }

    @Override
    public RestResponse<Token> loggedOut(HttpSession session, CsrfToken token) {
        return RestResponse.result(new Token(token));
    }

    @Override
    public BaseResponse register(PlatformAccountCommandDto command, BindingResult validationResult) {
        this.accountValidator.validate(command, validationResult);

        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        final AccountDto account = this.userService.createPlatformAccount(command).getResult().getAccount();

        return RestResponse.result(account);
    }

    @Override
    public BaseResponse requestActivationToken(ActivationTokenCommandDto command, BindingResult validationResult) {
        if (validationResult.hasErrors()) {
            return RestResponse.invalid(validationResult.getFieldErrors());
        }

        final ServiceResponse<ActivationTokenDto> response = this.userService.createToken(command, true);

        if (response.getResult() == null) {
            return RestResponse.error(response.getMessages());
        }

        return RestResponse.success();
    }

    @Override
    public BaseResponse changePassword(PasswordChangeCommandDto command, BindingResult validationResult) {
        logger.info("Password change request. [key={}, email={}]", this.currentUserKey(), this.currentUserEmail());

        try {
            command.setUserName(this.currentUserEmail());

            this.passwordCommandValidator.validate(command, validationResult);

            if (validationResult.hasErrors()) {
                return RestResponse.invalid(validationResult.getFieldErrors());
            }

            this.userService.changePassword(command);

            return RestResponse.success();
        } catch (UsernameNotFoundException | BadCredentialsException ex) {
            return RestResponse.failure(BasicMessageCode.Forbidden, "Access Denied");
        }
    }

}
