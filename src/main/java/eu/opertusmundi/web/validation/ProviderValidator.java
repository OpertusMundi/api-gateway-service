package eu.opertusmundi.web.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.opertusmundi.common.domain.CustomerEntity;
import eu.opertusmundi.common.model.dto.AccountProfileProviderCommandDto;
import eu.opertusmundi.common.model.dto.ProviderProfessionalCommandDto;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.CustomerRepository;

@Component
public class ProviderValidator implements Validator {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return AccountProfileProviderCommandDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object o, Errors e) {
        final ProviderProfessionalCommandDto c = (ProviderProfessionalCommandDto) o;

        // Email must be unique for all providers
        final CustomerEntity otherConsumer = this.customerRepository
            .findProviderByEmailAndAccountIdNot(c.getEmail(), c.getUserId())
            .orElse(null);

        if (otherConsumer != null) {
            e.rejectValue("email", "not-unique");
        }
    }

}
