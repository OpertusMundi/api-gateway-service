package eu.opertusmundi.web.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.opertusmundi.common.model.dto.AccountProfileConsumerCommandDto;
import eu.opertusmundi.web.repository.AccountRepository;

@Component
public class ConsumerValidator implements Validator {

    @Autowired
    AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return AccountProfileConsumerCommandDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object o, Errors e) {

    }

}
