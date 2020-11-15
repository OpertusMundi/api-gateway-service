package eu.opertusmundi.web.validation;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.opertusmundi.common.model.dto.ProviderProfessionalCommandDto;

@Component
public class ConsumerProfessionalValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return ProviderProfessionalCommandDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object o, Errors e) {

    }

}
