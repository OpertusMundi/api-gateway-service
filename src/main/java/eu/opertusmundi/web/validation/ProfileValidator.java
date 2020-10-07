package eu.opertusmundi.web.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.opertusmundi.common.model.dto.AccountProfileCommandDto;
import eu.opertusmundi.web.domain.AccountEntity;
import eu.opertusmundi.web.repository.AccountRepository;

@Component
public class ProfileValidator implements Validator {

    @Autowired
    AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return AccountProfileCommandDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object o, Errors e) {
        final AccountProfileCommandDto c = (AccountProfileCommandDto) o;

        // Email must be unique
        AccountEntity entity = this.accountRepository.findOneByEmailAndIdNot(c.getEmail(), c.getId()).orElse(null);

        entity = this.accountRepository.findOneByEmail(c.getEmail()).orElse(null);

        if (entity != null) {
            // A user with the same email already exists
            e.rejectValue("email", "not-unique");
        }
    }

}
