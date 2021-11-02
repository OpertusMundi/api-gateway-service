package eu.opertusmundi.web.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.opertusmundi.common.model.EnumValidatorError;
import eu.opertusmundi.common.model.account.JoinVendorCommandDto;

@Component
public class JoinVendorCommandValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return JoinVendorCommandDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object obj, Errors e) {
        final JoinVendorCommandDto c = (JoinVendorCommandDto) obj;

        // Check password
        if (!StringUtils.isBlank(c.getPassword()) && !c.getPassword().equals(c.getVerifyPassword())) {
            e.rejectValue("verifyPassword", EnumValidatorError.NotEqual.name());
        }
    }

}
