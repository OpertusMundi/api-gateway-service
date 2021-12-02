package eu.opertusmundi.web.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.opertusmundi.common.domain.ProviderAssetDraftEntity;
import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.EnumValidatorError;
import eu.opertusmundi.common.model.asset.EnumProviderAssetDraftStatus;
import eu.opertusmundi.common.model.catalogue.CatalogueServiceMessageCode;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemMetadataCommandDto;
import eu.opertusmundi.common.repository.DraftRepository;

@Component
public class DraftReviewValidator implements Validator {


    @Autowired
    private DraftRepository draftRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return CatalogueItemMetadataCommandDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object o, Errors e) {
        final CatalogueItemMetadataCommandDto c = (CatalogueItemMetadataCommandDto) o;

        final ProviderAssetDraftEntity draft = c.getOwnerKey().equals(c.getPublisherKey())
            ? draftRepository.findOneByPublisherAndKey(c.getPublisherKey(), c.getDraftKey()).orElse(null)
            : draftRepository.findOneByOwnerAndPublisherAndKey(c.getOwnerKey(), c.getPublisherKey(), c.getDraftKey()).orElse(null);

        if (draft == null) {
            e.reject(BasicMessageCode.RecordNotFound.key(), "Draft not found");
        }

        if (draft.getStatus() != EnumProviderAssetDraftStatus.PENDING_PROVIDER_REVIEW) {
            e.reject(CatalogueServiceMessageCode.DRAFT_INVALID_STATUS.key(), "Status must be PENDING_PROVIDER_REVIEW");
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
        } else if (!CollectionUtils.isEmpty(c.getVisibility())) {
            for (int i = 0; i < c.getVisibility().size(); i++) {
                if (metadata == null || metadata.get(c.getVisibility().get(i)) == null) {
                    e.rejectValue(String.format("visibility[%d]", i), EnumValidatorError.OptionNotSupported.name());
                }
            }
        }
    }

}
