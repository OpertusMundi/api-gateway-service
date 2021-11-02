package eu.opertusmundi.web.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.EnumValidatorError;
import eu.opertusmundi.common.model.account.AccountDto;
import eu.opertusmundi.common.model.account.PlatformAccountCommandDto;
import eu.opertusmundi.common.model.account.VendorAccountCommandDto;
import eu.opertusmundi.common.repository.AccountRepository;

@Component
public class AccountValidator implements Validator {

    @Autowired
    AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return PlatformAccountCommandDto.class.isAssignableFrom(clazz) || VendorAccountCommandDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object obj, Errors e) {
        if (obj instanceof PlatformAccountCommandDto) {
            this.validate((PlatformAccountCommandDto) obj, e);
        }

        if (obj instanceof VendorAccountCommandDto) {
            this.validate((VendorAccountCommandDto) obj, e);
        }
    }

    private void validate(PlatformAccountCommandDto command, Errors e) {
        validateEmail(command.getEmail(), e);

        if (!StringUtils.isBlank(command.getPassword()) && !command.getPassword().equals(command.getVerifyPassword())) {
            e.rejectValue("password", EnumValidatorError.NotEqual.name());
        }
    }

    private void validate(VendorAccountCommandDto command, Errors e) {
        if (command.getKey() == null) {
            validateEmail(command.getEmail(), e);
        } else {
            final AccountDto account = this.accountRepository.findOneByKeyObject(command.getKey()).orElse(null);

            if (account == null) {
                // Account must exist
                e.reject(BasicMessageCode.RecordNotFound.key(), "Account not found");
            } else if (account.getParentKey() == null || !account.getParentKey().equals(command.getParentKey())) {
                // Parent property cannot be updated
                e.rejectValue("parentKey", EnumValidatorError.NotUpdatable.name());
            } else if (!account.getEmail().equals(command.getEmail())) {
                // Email property cannot be updated
                e.rejectValue("email", EnumValidatorError.NotUpdatable.name());
            }
        }
    }

    private void validateEmail(String email, Errors e) {
        // Email must be unique
        final AccountEntity account = this.accountRepository.findOneByEmail(email).orElse(null);

        if (account != null) {
            e.rejectValue("email", EnumValidatorError.NotUnique.name());
        }
    }

}
