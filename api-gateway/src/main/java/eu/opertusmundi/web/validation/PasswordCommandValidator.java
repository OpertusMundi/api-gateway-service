package eu.opertusmundi.web.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.opertusmundi.web.model.security.PasswordChangeCommandDto;

@Component
public class PasswordCommandValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return PasswordChangeCommandDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object obj, Errors e) {
        final PasswordChangeCommandDto a = (PasswordChangeCommandDto) obj;

        // Check password
        if (!StringUtils.isBlank(a.getNewPassword()) && !a.getNewPassword().equals(a.getVerifyNewPassword())) {
            e.rejectValue("verifyNewPassword", "NotEqual");
        }
    }

}
