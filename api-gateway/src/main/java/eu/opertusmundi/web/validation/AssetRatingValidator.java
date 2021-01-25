package eu.opertusmundi.web.validation;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.opertusmundi.web.model.rating.client.ClientRatingCommandDto;

@Component
public class AssetRatingValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return ClientRatingCommandDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object o, Errors e) {

    }

}
