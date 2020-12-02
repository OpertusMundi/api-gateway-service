package eu.opertusmundi.web.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.model.dto.AccountCommandDto;
import eu.opertusmundi.common.repository.AccountRepository;

@Component
public class AccountValidator implements Validator {

    @Autowired
    AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return AccountCommandDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object obj, Errors e) {
        final AccountCommandDto a = (AccountCommandDto) obj;

        AccountEntity account;

        // Email must be unique
        account = this.accountRepository.findOneByEmail(a.getEmail()).orElse(null);

        if (account != null) {
            e.rejectValue("email", "not-unique");
        }

        // Check password
        if (!a.getPassword().equals(a.getVerifyPassword())) {
            e.rejectValue("password", "not-equal");
        }
    }

}
