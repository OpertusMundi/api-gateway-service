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
import eu.opertusmundi.common.model.asset.BundleAssetResourceDto;
import eu.opertusmundi.common.model.asset.EnumAssetAdditionalResource;
import eu.opertusmundi.common.model.asset.EnumResourceType;
import eu.opertusmundi.common.model.asset.FileResourceDto;
import eu.opertusmundi.common.model.asset.ResourceDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
import eu.opertusmundi.common.model.catalogue.client.EnumDeliveryMethod;
import eu.opertusmundi.common.model.pricing.BasePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.EnumPricingModel;
import eu.opertusmundi.common.model.pricing.QuotationException;
import eu.opertusmundi.common.repository.AssetAdditionalResourceRepository;
import eu.opertusmundi.common.repository.AssetFileTypeRepository;
import eu.opertusmundi.common.repository.AssetResourceRepository;
import eu.opertusmundi.common.repository.DraftRepository;
import eu.opertusmundi.common.repository.contract.ProviderTemplateContractRepository;
import eu.opertusmundi.common.service.AssetDraftException;
import eu.opertusmundi.common.service.CatalogueService;
import eu.opertusmundi.common.service.integration.DataProviderManager;

@Component
public class DraftValidator implements Validator {

    public enum EnumValidationMode {
        UNDEFINED,
        UPDATE,
        SUBMIT,
        ;
    }

    @Autowired
    private DataProviderManager dataProviderManager;

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

    @Autowired
    private CatalogueService catalogueService;

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
        this.validateType(c, e);
        this.validateFormat(c, e);
        this.validateResources(c, e, mode);
        this.validateAdditionalResources(c, e);
        this.validatePricingModels(c, e, mode);
        this.validateDeliveryMethods(c, e, mode);
        this.validateExtensions(c, e, mode);

        // Only one draft may exist for a specific parent
        if (draftKey == null && !StringUtils.isBlank(c.getParentId())) {
            final ProviderAssetDraftEntity parent = draftRepository.findAllByParentId(c.getParentId()).stream()
                .findFirst()
                .orElse(null);

            if (parent != null) {
                e.rejectValue("parentId", EnumValidatorError.NotUnique.name());
            }
        }
        // Cannot change parent id once set
        if (draftKey != null) {
            final ProviderAssetDraftEntity draft = draftRepository.findOneByKey(draftKey).orElse(null);
            if (draft != null && !StringUtils.isBlank(draft.getParentId()) && !StringUtils.equals(draft.getParentId(), c.getParentId())) {
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

    private void validateType(CatalogueItemCommandDto c, Errors e) {
        if (c.getSpatialDataServiceType() != null && c.getType() != EnumAssetType.SERVICE) {
            e.rejectValue("spatialDataServiceType", EnumValidatorError.OperationNotSupported.name());
        }
    }

    private void validateFormat(CatalogueItemCommandDto c, Errors e) {
        if (StringUtils.isBlank(c.getFormat())) {
            return;
        }

        final EnumAssetType       category = c.getType() == EnumAssetType.SERVICE ? EnumAssetType.VECTOR : c.getType();
        final AssetFileTypeEntity format   = this.assetFileTypeRepository.findOneByCategoryAndFormat(category, c.getFormat()).orElse(null);

        if (format == null) {
            e.rejectValue("format", EnumValidatorError.OptionNotFound.name());
        } else if (!format.isEnabled()) {
            e.rejectValue("format", EnumValidatorError.OptionNotEnabled.name());
        } else if (c.isIngested() && format.getCategory() != EnumAssetType.VECTOR) {
            e.rejectValue("ingested", EnumValidatorError.OperationNotSupported.name());
        }
    }

    private void validateResources(CatalogueItemCommandDto c, Errors e, EnumValidationMode mode) {
        final boolean                   resourceRequired = c.getType().isResourceRequired();
        final List<AssetResourceEntity> serverResources  = this.assetResourceRepository.findAllResourcesByDraftKey(c.getDraftKey());
        final List<String>              serverKeys       = serverResources.stream().map(r -> r.getKey()).collect(Collectors.toList());
        final List<String>              fileKeys         = c.getResources().stream()
            .filter(r -> r.getType() == EnumResourceType.FILE)
            .map(r -> r.getId())
            .collect(Collectors.toList());
        final List<String>              assetKeys       = c.getResources().stream()
            .filter(r -> r.getType() == EnumResourceType.ASSET)
            .map(r -> r.getId())
            .collect(Collectors.toList());

        // For submitted drafts, at least one resource must exist
        if (mode == EnumValidationMode.SUBMIT && c.getResources().isEmpty() && resourceRequired) {
            e.rejectValue("resources", EnumValidatorError.NotEmpty.name());
        }

        // All request resource keys must exist at the server
        for (int i = 0; i < fileKeys.size(); i++) {
            if (!serverKeys.contains(fileKeys.get(i))) {
                e.rejectValue(String.format("resources[%d]", i), EnumValidatorError.ResourceNotFound.name());
            }
        }

        if(e.hasErrors()) {
            return;
        }

        // Check bundle resources
        if (c.getType() == EnumAssetType.BUNDLE) {
            for (int i = 0; i < c.getResources().size(); i++) {
                final ResourceDto r = c.getResources().get(i);
                if (r.getType() != EnumResourceType.ASSET) {
                    e.rejectValue(String.format("resources[%d].type", i), EnumValidatorError.NotValid.name());
                }
            }
            // Query catalogue service only if no errors have already found
            if (!e.hasErrors() &&  mode == EnumValidationMode.SUBMIT && !assetKeys.isEmpty()) {
                final List<CatalogueItemDetailsDto> assets = this.catalogueService
                    .findAllById(assetKeys.toArray(new String[assetKeys.size()]));

                if (assets.size() != assetKeys.size()) {
                    for (int i = 0; i < c.getResources().size(); i++) {
                        final ResourceDto             r  = c.getResources().get(i);
                        final BundleAssetResourceDto  br = (BundleAssetResourceDto) r;
                        final CatalogueItemDetailsDto ca = assets.stream().filter(a -> a.getId().equals(br.getId())).findFirst().orElse(null);
                        if (ca == null) {
                            e.rejectValue(String.format("resources[%d].id", i), EnumValidatorError.ReferenceNotFound.name());
                        }
                    }
                }
            }
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
        for (int i = 0; i < fileKeys.size(); i++) {
            final String              key       = fileKeys.get(i);
            final AssetResourceEntity resource  = serverResources.stream().filter(r -> r.getKey().equals(key)).findFirst().orElse(null);
            final String              extension = FilenameUtils.getExtension(resource.getFileName());
            final EnumAssetType       category  = c.getType() == EnumAssetType.SERVICE ? EnumAssetType.VECTOR : c.getType();
            final AssetFileTypeEntity format    = this.assetFileTypeRepository.findOneByCategoryAndFormat(category, resource.getFormat()).orElse(null);

            if (format == null) {
                e.rejectValue(String.format("resources[%d].format", i), EnumValidatorError.OptionNotFound.name());
            } else if (!format.isEnabled()) {
                e.rejectValue(String.format("resources[%d].format", i), EnumValidatorError.OptionNotEnabled.name());
            } else if (c.isIngested() && format.getCategory() != EnumAssetType.VECTOR) {
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
            .findAllResourcesByDraftKey(c.getDraftKey());

        final List<String> keys = resources.stream().map(r -> r.getKey()).collect(Collectors.toList());

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
        final List<BasePricingModelCommandDto> models               = c.getPricingModels();
        final List<EnumPricingModel>           allowedModels        = c.getType().getAllowedPricingModels();
        final boolean                          dynamicPricingModels = c.getType().isDynamicPricingModels();

        // Check if at least one pricing model is required to submit the draft
        if (!dynamicPricingModels && models.isEmpty() && mode == EnumValidationMode.SUBMIT) {
            e.rejectValue("pricingModels", EnumValidatorError.NotEmpty.name());
            return;
        }
        if(dynamicPricingModels && !models.isEmpty()) {
            e.rejectValue("pricingModels", EnumValidatorError.OptionNotSupported.name());
            return;
        }

        // Check if the selected asset type supports all specified pricing
        // models
        if (!allowedModels.isEmpty()) {
            for (int i = 0; i < models.size(); i++) {
                if (!allowedModels.contains(models.get(i).getType())) {
                    e.rejectValue(String.format("pricingModels[%d]", i), EnumValidatorError.OptionNotSupported.name());
                }
            }
        }
        // Validate each pricing model
        for (int i = 0; i < models.size(); i++) {
            try {
                models.get(i).validate();
            } catch (final QuotationException ex) {
                e.rejectValue(String.format("pricingModels[%d]", i), ex.getCode().toString());
            }
        }
        // For open datasets, only a single pricing model of type FREE is
        // allowed
        if (c.isOpenDataset()) {
            if (models.size() != 1) {
                e.rejectValue("pricingModels", EnumValidatorError.Size.name());
            } else if (models.get(0).getType() != EnumPricingModel.FREE) {
                e.rejectValue("pricingModels[0]", EnumValidatorError.OptionNotSupported.name());
            }
        }
        // If FREE pricing model is selected, delivery method must be DIGITAL_PLATFORM
        final BasePricingModelCommandDto freeModel = models.stream()
            .filter(m -> m.getType() == EnumPricingModel.FREE)
            .findFirst()
            .orElse(null);

        if (freeModel != null && c.getDeliveryMethod() != EnumDeliveryMethod.DIGITAL_PLATFORM) {
            e.rejectValue("deliveryMethod", EnumValidatorError.OperationNotSupported.name());
        }
    }

    private void validateDeliveryMethods(CatalogueItemCommandDto c, Errors e, EnumValidationMode mode) {
        final EnumDeliveryMethod       method         = c.getDeliveryMethod();
        final List<EnumDeliveryMethod> allowedMethods = c.getType().getAllowedDeliveryMethods();

        if (method == null || allowedMethods.isEmpty()) {
            return;
        }

        // Check if the selected asset type and delivery method are compatible
        if (!allowedMethods.contains(method)) {
            e.rejectValue("deliveryMethod", EnumValidatorError.OptionNotSupported.name());
        }
    }

    private void validateExtensions(CatalogueItemCommandDto c, Errors e, EnumValidationMode mode) {
        if (c.getExtensions() == null) {
            return;
        }

        dataProviderManager.validateCatalogueItem(c, e);
    }

}
