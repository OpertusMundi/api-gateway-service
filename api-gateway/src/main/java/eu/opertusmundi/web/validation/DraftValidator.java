package eu.opertusmundi.web.validation;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.opertusmundi.common.domain.AssetAdditionalResourceEntity;
import eu.opertusmundi.common.domain.AssetFileTypeEntity;
import eu.opertusmundi.common.domain.AssetResourceEntity;
import eu.opertusmundi.common.domain.ProviderAssetDraftEntity;
import eu.opertusmundi.common.domain.ProviderTemplateContractEntity;
import eu.opertusmundi.common.model.EnumValidatorError;
import eu.opertusmundi.common.model.asset.AssetAdditionalResourceDto;
import eu.opertusmundi.common.model.asset.AssetFileAdditionalResourceDto;
import eu.opertusmundi.common.model.asset.AssetMessageCode;
import eu.opertusmundi.common.model.asset.EnumAssetAdditionalResource;
import eu.opertusmundi.common.model.asset.EnumAssetSourceType;
import eu.opertusmundi.common.model.asset.EnumResourceType;
import eu.opertusmundi.common.model.asset.FileResourceDto;
import eu.opertusmundi.common.model.asset.ResourceDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import eu.opertusmundi.common.model.pricing.EnumPricingModel;
import eu.opertusmundi.common.model.pricing.QuotationException;
import eu.opertusmundi.common.repository.AssetAdditionalResourceRepository;
import eu.opertusmundi.common.repository.AssetFileTypeRepository;
import eu.opertusmundi.common.repository.AssetResourceRepository;
import eu.opertusmundi.common.repository.DraftRepository;
import eu.opertusmundi.common.repository.contract.ProviderTemplateContractRepository;
import eu.opertusmundi.common.service.AssetDraftException;

@Component
public class DraftValidator implements Validator {

    public enum EnumValidationMode {
        UNDEFINED,
        UPDATE,
        SUBMIT,
        ;
    }

    @Autowired
    private ProviderTemplateContractRepository contractRepository;

    @Autowired
    private AssetFileTypeRepository assetFileTypeRepository;

    @Autowired
    private AssetResourceRepository assetResourceRepository;

    @Autowired
    private AssetAdditionalResourceRepository assetAdditionalResourceRepository;

    @Autowired
    private DraftRepository draftRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return CatalogueItemCommandDto.class.isAssignableFrom(clazz);
    }


    @Override
    public void validate(Object o, Errors e) {
        throw new AssetDraftException(AssetMessageCode.VALIDATION, "Operation not supported");
    }

    public void validate(Object o, Errors e, EnumValidationMode mode) {
        this.validate(o, e, mode, null);
    }

    public void validate(Object o, Errors e, EnumValidationMode mode, @Nullable UUID draftKey) {
        final CatalogueItemCommandDto c = (CatalogueItemCommandDto) o;

        this.validateContract(c, e, mode);
        this.validateFormat(c, e);
        this.validateResources(c, e, mode);
        this.validateAdditionalResources(c, e);
        this.validatePricingModels(c, e, mode);

        if (draftKey != null) {
            final ProviderAssetDraftEntity draft = draftRepository.findOneByKey(draftKey).orElse(null);
            if (draft != null && !StringUtils.isBlank(draft.getParentId()) && !StringUtils.equals(draft.getParentId(), c.getParentId())) {
                // Cannot change parent id once set
                e.rejectValue("parentId", EnumValidatorError.NotUpdatable.name());
            }
        }
    }

    private void validateContract(CatalogueItemCommandDto c, Errors e, EnumValidationMode mode) {
        final ProviderTemplateContractEntity contract = contractRepository
            .findOneByKey(c.getPublisherKey(), c.getContractTemplateKey())
            .orElse(null);

        // Provider contract must exist and be active to submit a draft
        if (contract == null && mode == EnumValidationMode.SUBMIT) {
            e.rejectValue("contractTemplateKey", EnumValidatorError.OptionNotFound.name());
        }
    }

    private void validateFormat(CatalogueItemCommandDto c, Errors e) {
        if (StringUtils.isBlank(c.getFormat())) {
            return;
        }

        final AssetFileTypeEntity format = this.assetFileTypeRepository.findOneByFormat(c.getFormat()).orElse(null);

        if (format == null) {
            e.rejectValue("format", EnumValidatorError.OptionNotFound.name());
        } else if (!format.isEnabled()) {
            e.rejectValue("format", EnumValidatorError.OptionNotEnabled.name());
        } else if (c.isIngested() && format.getCategory() != EnumAssetSourceType.VECTOR) {
            e.rejectValue("ingested", EnumValidatorError.OperationNotSupported.name());
        }
    }

    private void validateResources(CatalogueItemCommandDto c, Errors e, EnumValidationMode mode) {
        final List<AssetResourceEntity> serverResources = this.assetResourceRepository.findAllResourcesByDraftKey(c.getAssetKey());
        final List<UUID>                serverKeys      = serverResources.stream().map(r -> r.getKey()).collect(Collectors.toList());
        final List<UUID>                requestKeys     = c.getResources().stream().map(r -> r.getId()).collect(Collectors.toList());

        // For submitted drafts, at least one resource must exist
        if (mode == EnumValidationMode.SUBMIT && c.getResources().isEmpty()) {
            e.rejectValue("resources", EnumValidatorError.NotEmpty.name());
        }

        // All request resource keys must exist at the server
        for (int i = 0; i < requestKeys.size(); i++) {
            if (!serverKeys.contains(requestKeys.get(i))) {
                e.rejectValue(String.format("resources[%d]", i), EnumValidatorError.ResourceNotFound.name());
            }
        }

        if(e.hasErrors()) {
            return;
        }

        // Check read-only properties
        for (int i = 0; i < c.getResources().size(); i++) {
            final ResourceDto requestResource = c.getResources().get(i);
            if (requestResource.getType() != EnumResourceType.FILE) {
                continue;
            }
            final FileResourceDto fileRequestResource = (FileResourceDto) requestResource;

            final AssetResourceEntity serverResource = serverResources.stream()
                .filter(r -> r.getKey().equals(requestResource.getId()))
                .findFirst().get();

            if (!serverResource.getFileName().equals(fileRequestResource.getFileName())) {
                e.rejectValue(String.format("resources[%d].fileName", i), EnumValidatorError.NotUpdatable.name());
            }
            if (!serverResource.getSize().equals(fileRequestResource.getSize())) {
                e.rejectValue(String.format("resources[%d].size", i), EnumValidatorError.NotUpdatable.name());
            }
            if (!serverResource.getCreatedOn().toInstant().equals(fileRequestResource.getModifiedOn().toInstant())) {
                e.rejectValue(String.format("resources[%d].modifiedOn", i), EnumValidatorError.NotUpdatable.name());
            }
        }

        // Check registered resources format
        for (int i = 0; i < requestKeys.size(); i++) {
            final UUID                key       = requestKeys.get(i);
            final AssetResourceEntity resource  = serverResources.stream().filter(r -> r.getKey().equals(key)).findFirst().orElse(null);
            final String              extension = FilenameUtils.getExtension(resource.getFileName());
            final AssetFileTypeEntity format    = this.assetFileTypeRepository.findOneByFormat(resource.getFormat()).orElse(null);

            if (format == null) {
                e.rejectValue(String.format("resources[%d].format", i), EnumValidatorError.OptionNotFound.name());
            } else if (!format.isEnabled()) {
                e.rejectValue(String.format("resources[%d].format", i), EnumValidatorError.OptionNotEnabled.name());
            } else if (c.isIngested() && format.getCategory() != EnumAssetSourceType.VECTOR) {
                e.rejectValue(String.format("resources[%d].ingested", i), EnumValidatorError.OperationNotSupported.name());
            } else if (!format.getExtensions().contains(extension)) {
                if (format.isBundleSupported() && extension.equals("zip")) {
                    continue;
                }
                e.rejectValue(String.format("resources[%d].fileName", i), EnumValidatorError.FileExtensionNotSupported.name());
            }
        }
    }

    private void validateAdditionalResources(CatalogueItemCommandDto c,  Errors e) {
        final List<AssetAdditionalResourceEntity> resources = this.assetAdditionalResourceRepository
            .findAllResourcesByDraftKey(c.getAssetKey());

        final List<UUID> keys = resources.stream().map(r -> r.getKey()).collect(Collectors.toList());

        // All file additional resource keys must exist
        for (int i = 0; i < c.getAdditionalResources().size(); i++) {
            final AssetAdditionalResourceDto r = c.getAdditionalResources().get(i);
            if (r.getType() != EnumAssetAdditionalResource.FILE) {
                continue;
            }

            if (!keys.contains(((AssetFileAdditionalResourceDto) r).getId())) {
                e.rejectValue(String.format("additionalResources[%d]", i), EnumValidatorError.ResourceNotFound.name());
            }
        }
    }

    private void validatePricingModels(CatalogueItemCommandDto c, Errors e, EnumValidationMode mode) {
        // At least one pricing model is required to submit the draft
        if (c.getPricingModels().size() == 0 && mode == EnumValidationMode.SUBMIT) {
            e.rejectValue("pricingModels", EnumValidatorError.NotEmpty.name());
        } else {
            // Validate each pricing model
            for (int i = 0; i < c.getPricingModels().size(); i++) {
                try {
                    c.getPricingModels().get(i).validate();
                } catch (final QuotationException ex) {
                    e.rejectValue(String.format("pricingModels[%d]", i), EnumValidatorError.NotValid.name());
                }
            }
            // For open datasets, only a single pricing model of type FREE is
            // allowed
            if (c.isOpenDataset()) {
                if (c.getPricingModels().size() != 1) {
                    e.rejectValue("pricingModels", EnumValidatorError.Size.name());
                } else if (c.getPricingModels().get(0).getType() != EnumPricingModel.FREE) {
                    e.rejectValue("pricingModels[0]", EnumValidatorError.OptionNotSupported.name());
                }
            }
        }
    }

}
