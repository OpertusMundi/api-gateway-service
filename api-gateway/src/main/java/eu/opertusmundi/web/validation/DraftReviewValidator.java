package eu.opertusmundi.web.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.opertusmundi.common.domain.ProviderAssetDraftEntity;
import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.EnumValidatorError;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemProviderCommandDto;
import eu.opertusmundi.common.repository.DraftRepository;

@Component
public class DraftReviewValidator implements Validator {


    @Autowired
    private DraftRepository draftRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return CatalogueItemProviderCommandDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object o, Errors e) {
        final CatalogueItemProviderCommandDto c = (CatalogueItemProviderCommandDto) o;

        final ProviderAssetDraftEntity draft = draftRepository.findOneByPublisherAndKey(c.getProviderKey(), c.getDraftKey()).orElse(null);
        if (draft == null) {
            e.reject(BasicMessageCode.RecordNotFound.key(), "Draft not found");
        }

        final ArrayNode metadataArray = (ArrayNode) draft.getCommand().getAutomatedMetadata();
        ObjectNode      metadata      = null;

        if (metadataArray == null || c.getResourceKey() == null) {
            e.rejectValue("resourceKey", EnumValidatorError.ResourceNotFound.name());
        }

        for (int i = 0; i < metadataArray.size(); i++) {
            if (metadataArray.get(i).get("key").asText().equals(c.getResourceKey().toString())) {
                metadata = (ObjectNode) metadataArray.get(i);
                break;
            }
        }

        if (metadata == null) {
            e.rejectValue("resourceKey", EnumValidatorError.ResourceNotFound.name());
        } else {
            for (int i = 0; i < c.getVisibility().size(); i++) {
                if (metadata == null || metadata.get(c.getVisibility().get(i)) == null) {
                    e.rejectValue(String.format("visibility[%d]", i), EnumValidatorError.OptionNotSupported.name());
                }
            }
        }
    }

}