package eu.opertusmundi.web.validation;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.opertusmundi.common.domain.MasterContractHistoryEntity;
import eu.opertusmundi.common.domain.MasterSectionHistoryEntity;
import eu.opertusmundi.common.domain.ProviderTemplateContractDraftEntity;
import eu.opertusmundi.common.model.EnumValidatorError;
import eu.opertusmundi.common.model.contract.provider.ProviderTemplateContractCommandDto;
import eu.opertusmundi.common.model.contract.provider.ProviderTemplateSectionDto;
import eu.opertusmundi.common.repository.contract.MasterContractHistoryRepository;
import eu.opertusmundi.common.repository.contract.ProviderTemplateContractDraftRepository;

@Component
public class ProviderTemplateContractValidator implements Validator {

    @Autowired
    private ProviderTemplateContractDraftRepository draftRepository;

    @Autowired
    private MasterContractHistoryRepository masterRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return ProviderTemplateContractCommandDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object o, Errors e) {
        final ProviderTemplateContractCommandDto  c        = (ProviderTemplateContractCommandDto) o;
        final MasterContractHistoryEntity         template = this.masterRepository.findOneByKey(c.getTemplateKey()).orElse(null);
        final ProviderTemplateContractDraftEntity draft    = draftRepository.findOneByKey(c.getUserId(), c.getDraftKey()).orElse(null);

        if (draft != null && !draft.getTemplate().getKey().equals(c.getTemplateKey())) {
            // Template key cannot be updated
            e.rejectValue("templateKey", EnumValidatorError.NotUpdatable.name());
        }

        this.validateTemplate(c, template, e);
        this.validateSections(c, template, e);
    }

    private void validateTemplate(ProviderTemplateContractCommandDto c, MasterContractHistoryEntity template, Errors e) {
        if (template == null) {
            e.rejectValue("templateKey", EnumValidatorError.ReferenceNotFound.name());
        }
    }

    private void validateSections(ProviderTemplateContractCommandDto c, MasterContractHistoryEntity template, Errors e) {
        // Contract must exist
        if (template == null) {
            return;
        }
        // Every master contract section is required
        final List<MasterSectionHistoryEntity> requiredSections = template.getSections().stream().collect(Collectors.toList());

        if (!c.getSections().isEmpty()) {
            for (int sIndex = 0; sIndex < c.getSections().size(); sIndex++) {
                final ProviderTemplateSectionDto templateSection = c.getSections().get(sIndex);
                final MasterSectionHistoryEntity masterSection   = template.findSectionById(templateSection.getMasterSectionId());

                if (masterSection == null) {
                    // Master section was not found
                    e.rejectValue(String.format("sections[%d].masterSectionId", sIndex), EnumValidatorError.ReferenceNotFound.name());
                } else {
                    // Required master section exists
                    if (requiredSections.contains(masterSection)) {
                        requiredSections.remove(masterSection);
                    } else {
                        // Duplicate template section
                        e.rejectValue(String.format("sections[%d].masterSectionId", sIndex), EnumValidatorError.NotUnique.name());
                    }
                    // Check optional flag
                    if (templateSection.isOptional() && !masterSection.isOptional()) {
                        e.rejectValue(String.format("sections[%d].optional", sIndex), EnumValidatorError.NotValid.name());
                    }
                    // Check option
                    if (templateSection.getOption() != null && masterSection.findOptionByIndex(templateSection.getOption()) == null) {
                        e.rejectValue(String.format("sections[%d].option", sIndex), EnumValidatorError.ReferenceNotFound.name());
                    }
                    // Check sub options
                    if (!CollectionUtils.isEmpty(templateSection.getSubOption())) {
                        if (templateSection.getOption() == null) {
                            // Option index must be set
                            e.rejectValue(String.format("sections[%d].option", sIndex), EnumValidatorError.NotValid.name());
                        } else {
                            // Check list size for mutually exclusive sub options
                            final Boolean mutexSubOptions = masterSection.getOptions()
                                .get(templateSection.getOption())
                                .getMutexSuboptions();

                            if (mutexSubOptions != null && mutexSubOptions.equals(true) && templateSection.getSubOption().size() > 1) {
                                e.rejectValue(String.format("sections[%d].subOption", sIndex), EnumValidatorError.NotValid.name());
                            }
                            // Check that all referenced sub-options exist
                            for (int oIndex = 0; oIndex < templateSection.getSubOption().size(); oIndex++) {
                                final int subOption = templateSection.getSubOption().get(oIndex);
                                if (masterSection.findSubOptionByIndex(templateSection.getOption(), subOption) == null) {
                                    // Sub option must exist
                                    e.rejectValue(String.format("sections[%d].subOption[%d]", sIndex, oIndex), EnumValidatorError.OptionNotFound.name());
                                }
                            }
                        }
                    }
                }
            }
        }
        // Required sections collection must be empty after validating all
        // provider template sections
        for (final MasterSectionHistoryEntity r : requiredSections) {
            e.reject("MasterSectionNotFound", r.getId().toString());
        }
    }


}
