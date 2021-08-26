package eu.opertusmundi.web.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.opertusmundi.common.domain.ProviderAssetDraftEntity;
import eu.opertusmundi.common.model.EnumValidatorError;
import eu.opertusmundi.common.model.catalogue.client.DraftFromAssetCommandDto;
import eu.opertusmundi.common.repository.DraftRepository;

@Component
public class DraftFromAssetValidator implements Validator {

    @Autowired
    private DraftRepository draftRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return DraftFromAssetCommandDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object o, Errors e) {
        final DraftFromAssetCommandDto c = (DraftFromAssetCommandDto) o;

        if (!StringUtils.isBlank(c.getPid())) {
            final ProviderAssetDraftEntity draft = draftRepository.findAllByParentId(c.getPid()).stream()
                .findFirst()
                .orElse(null);

            if (draft != null) {
                e.rejectValue("pid", EnumValidatorError.NotUnique.name());
            }
        }
    }

}
