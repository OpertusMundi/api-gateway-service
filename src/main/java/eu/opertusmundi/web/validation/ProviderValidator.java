package eu.opertusmundi.web.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.opertusmundi.common.model.dto.AccountProfileProviderCommandDto;
import eu.opertusmundi.web.domain.AccountEntity;
import eu.opertusmundi.web.repository.AccountRepository;

@Component
public class ProviderValidator implements Validator {

    @Autowired
    AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return AccountProfileProviderCommandDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object o, Errors e) {
        final AccountProfileProviderCommandDto c = (AccountProfileProviderCommandDto) o;

        // Email must be unique
        AccountEntity entity = this.accountRepository.findOneByEmailAndIdNot(c.getEmail(), c.getId()).orElse(null);

        entity = this.accountRepository.findOneByEmail(c.getEmail()).orElse(null);

        if (entity != null) {
            // A user with the same email already exists
            e.rejectValue("email", "not-unique");
        }
    }

}
