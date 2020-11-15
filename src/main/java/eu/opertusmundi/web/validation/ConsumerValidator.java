package eu.opertusmundi.web.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.opertusmundi.common.domain.AccountEntity;
import eu.opertusmundi.common.domain.CustomerEntity;
import eu.opertusmundi.common.model.dto.CustomerCommandDto;
import eu.opertusmundi.common.repository.AccountRepository;
import eu.opertusmundi.common.repository.CustomerRepository;

@Component
public class ConsumerValidator implements Validator {

    @Autowired
    ConsumerIndividualValidator individualValidator;

    @Autowired
    ConsumerProfessionalValidator professionalValidator;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    CustomerRepository customerRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return CustomerCommandDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object o, Errors e) {
        final CustomerCommandDto c = (CustomerCommandDto) o;

        final AccountEntity  account  = this.accountRepository.findById(c.getUserId()).orElse(null);
        final CustomerEntity consumer = account == null ? null : account.getProfile().getConsumer();

        // Consumer type cannot be modified once set
        if (consumer != null && consumer.getType() != c.getType()) {
            e.rejectValue("type", "not-updatable");
        }

        // Email must be unique for all consumers
        final CustomerEntity otherConsumer = this.customerRepository
            .findConsumerByEmailAndAccountIdNot(c.getEmail(), c.getUserId())
            .orElse(null);

        if (otherConsumer != null) {
            e.rejectValue("email", "not-unique");
        }

        switch (c.getType()) {
            case INDIVIDUAL :
                this.individualValidator.validate(o, e);
                break;
            case PROFESSIONAL :
                this.professionalValidator.validate(o, e);
                break;
            default :
                // No operation
                break;
        }
    }

}
