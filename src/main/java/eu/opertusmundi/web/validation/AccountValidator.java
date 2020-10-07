package eu.opertusmundi.web.validation;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.opertusmundi.common.model.dto.AccountCommandDto;
import eu.opertusmundi.web.domain.AccountEntity;
import eu.opertusmundi.web.repository.AccountRepository;

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
        if (a.getId() == null) {
            account = this.accountRepository.findOneByEmail(a.getEmail()).orElse(null);

            if (account != null) {
                e.rejectValue("email", "not-unique");
            }
        }
        // Email cannot be updated
        if (a.getId() != null) {
            account = this.accountRepository.findById(a.getId()).orElse(null);

            if (account == null) {
                throw new EntityNotFoundException();
            }
            if (!a.getEmail().equals(account.getEmail())) {
                e.rejectValue("email", "read-only");
            }
        }
        // Check password
        if (!a.getPassword().equals(a.getVerifyPassword())) {
            e.rejectValue("password", "not-equal");
        }
    }

}
