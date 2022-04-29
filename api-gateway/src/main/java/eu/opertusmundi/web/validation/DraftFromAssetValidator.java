package eu.opertusmundi.web.validation;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.opertusmundi.common.model.catalogue.client.DraftFromAssetCommandDto;

@Component
public class DraftFromAssetValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return DraftFromAssetCommandDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object o, Errors e) {

    }

}
