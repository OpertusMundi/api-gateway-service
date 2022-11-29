package eu.opertusmundi.web.validation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.opertusmundi.common.domain.AssetAdditionalResourceEntity;
import eu.opertusmundi.common.domain.AssetContractAnnexEntity;
import eu.opertusmundi.common.domain.AssetFileTypeEntity;
import eu.opertusmundi.common.domain.AssetResourceEntity;
import eu.opertusmundi.common.domain.ProviderAssetDraftEntity;
import eu.opertusmundi.common.domain.ProviderTemplateContractEntity;
import eu.opertusmundi.common.model.EnumValidatorError;
import eu.opertusmundi.common.model.asset.AssetAdditionalResourceDto;
import eu.opertusmundi.common.model.asset.AssetContractAnnexDto;
import eu.opertusmundi.common.model.asset.AssetFileAdditionalResourceDto;
import eu.opertusmundi.common.model.asset.AssetMessageCode;
import eu.opertusmundi.common.model.asset.BundleAssetResourceDto;
import eu.opertusmundi.common.model.asset.EnumAssetAdditionalResource;
import eu.opertusmundi.common.model.asset.EnumResourceType;
import eu.opertusmundi.common.model.asset.ExternalUrlResourceDto;
import eu.opertusmundi.common.model.asset.FileResourceDto;
import eu.opertusmundi.common.model.asset.ResourceDto;
import eu.opertusmundi.common.model.catalogue.DeliveryMethodOptions;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemDetailsDto;
import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
import eu.opertusmundi.common.model.catalogue.client.EnumContractType;
import eu.opertusmundi.common.model.catalogue.client.EnumDeliveryMethod;
import eu.opertusmundi.common.model.contract.ContractMessageCode;
import eu.opertusmundi.common.model.file.FileSystemException;
import eu.opertusmundi.common.model.pricing.BasePricingModelCommandDto;
import eu.opertusmundi.common.model.pricing.EnumPricingModel;
import eu.opertusmundi.common.model.pricing.QuotationException;
import eu.opertusmundi.common.repository.AssetAdditionalResourceRepository;
import eu.opertusmundi.common.repository.AssetContractAnnexRepository;
import eu.opertusmundi.common.repository.AssetFileTypeRepository;
import eu.opertusmundi.common.repository.AssetResourceRepository;
import eu.opertusmundi.common.repository.DraftRepository;
import eu.opertusmundi.common.repository.contract.ProviderTemplateContractRepository;
import eu.opertusmundi.common.service.AssetDraftException;
import eu.opertusmundi.common.service.CatalogueService;
import eu.opertusmundi.common.service.ProviderAssetService;
import eu.opertusmundi.common.service.integration.DataProviderManager;

@Component
public class DraftValidator implements Validator {

    public enum EnumValidationMode {
        UNDEFINED,
        UPDATE,
        SUBMIT,
        ;
    }

    private static List<EnumAssetType> BUNDLE_ALLOWED_ASSET_TYPES = List.of(
        EnumAssetType.NETCDF,
        EnumAssetType.RASTER,
        EnumAssetType.TABULAR,
        EnumAssetType.VECTOR
    );

    private final AssetAdditionalResourceRepository  assetAdditionalResourceRepository;
    private final AssetContractAnnexRepository       assetContractAnnexRepository;
    private final AssetFileTypeRepository            assetFileTypeRepository;
    private final AssetResourceRepository            assetResourceRepository;
    private final CatalogueService                   catalogueService;
    private final DataProviderManager                dataProviderManager;
    private final DraftRepository                    draftRepository;
    private final ProviderAssetService               providerAssetService;
    private final ProviderTemplateContractRepository contractRepository;

    @Autowired
    public DraftValidator(
         AssetAdditionalResourceRepository  assetAdditionalResourceRepository,
         AssetContractAnnexRepository       assetContractAnnexRepository,
         AssetFileTypeRepository            assetFileTypeRepository,
         AssetResourceRepository            assetResourceRepository,
         CatalogueService                   catalogueService,
         DataProviderManager                dataProviderManager,
         DraftRepository                    draftRepository,
         ProviderAssetService               providerAssetService,
         ProviderTemplateContractRepository contractRepository
    ) {
        this.assetAdditionalResourceRepository = assetAdditionalResourceRepository;
        this.assetContractAnnexRepository      = assetContractAnnexRepository;
        this.assetFileTypeRepository           = assetFileTypeRepository;
        this.assetResourceRepository           = assetResourceRepository;
        this.catalogueService                  = catalogueService;
        this.dataProviderManager               = dataProviderManager;
        this.draftRepository                   = draftRepository;
        this.providerAssetService              = providerAssetService;
        this.contractRepository                = contractRepository;
    }

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
        switch (c.getContractTemplateType()) {
            case UPLOADED_CONTRACT :
                try {
                    if (c.getDraftKey() != null) {
                        providerAssetService.resolveDraftContractPath(c.getOwnerKey(), c.getPublisherKey(), c.getDraftKey());
                    }
                } catch (final FileSystemException ex) {
                    // Uploaded contract must exist to submit a draft
                    if (mode == EnumValidationMode.SUBMIT) {
                        e.rejectValue("contractTemplateType", EnumValidatorError.FileNotFound.name());
                    }
                }

                // Validate contract annexes
                this.validateContractAnnexes(c, e);
                break;

            case MASTER_CONTRACT :
                final ProviderTemplateContractEntity contract = contractRepository
                    .findOneByKey(c.getPublisherKey(), c.getContractTemplateKey())
                    .orElse(null);

                // Provider contract must exist and be active to submit a draft
                if (contract == null && mode == EnumValidationMode.SUBMIT) {
                    e.rejectValue("contractTemplateKey", EnumValidatorError.OptionNotFound.name());
                }
                // If the provider's default contract is selected, it must be
                // also accepted by the provider
                if (contract != null &&
                    contract.isDefaultContract() &&
                    !contract.isDefaultContractAccepted() &&
                    mode == EnumValidationMode.SUBMIT
                ) {
                    e.rejectValue("contractTemplateKey", ContractMessageCode.DEFAULT_PROVIDER_CONTRACT_NOT_ACCEPTED.key());
                }

                // Contract annexes are supported only for uploaded contracts
                if (c.getContractAnnexes().size() != 0) {
                    e.rejectValue("contractAnnexes", EnumValidatorError.OperationNotSupported.name());
                }
                break;

            case OPEN_DATASET :
                if (!c.isOpenDataset()) {
                    e.rejectValue("openDataset", EnumValidatorError.NotValid.name());
                }
                if (c.getContractTemplateKey() != null) {
                    e.rejectValue("contractTemplateKey", EnumValidatorError.OptionNotSupported.name());
                }
                break;
        }

        // For open datasets, contract type must be OPEN_DATASET
        if (c.isOpenDataset() && c.getContractTemplateType() != EnumContractType.OPEN_DATASET) {
            e.rejectValue("contractTemplateType", EnumValidatorError.NotValid.name());
        }
    }

    private void validateContractAnnexes(CatalogueItemCommandDto c, Errors e) {
        final List<AssetContractAnnexEntity> annexes = this.assetContractAnnexRepository
            .findAllAnnexesByDraftKey(c.getDraftKey());

        final List<String> keys = annexes.stream().map(r -> r.getKey()).collect(Collectors.toList());

        // All contract annex keys must exist
        for (int i = 0; i < c.getContractAnnexes().size(); i++) {
            final AssetContractAnnexDto r = c.getContractAnnexes().get(i);
            if (!keys.contains(r.getId())) {
                e.rejectValue(String.format("contractAnnexes[%d]", i), EnumValidatorError.ResourceNotFound.name());
            }
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
        if (mode == EnumValidationMode.SUBMIT &&
            c.getResources().isEmpty() &&
            resourceRequired &&
            c.getDeliveryMethod() != EnumDeliveryMethod.PHYSICAL_PROVIDER &&
            c.getDeliveryMethod() != EnumDeliveryMethod.DIGITAL_PROVIDER
        ) {
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
            // Bundles allow only ASSET resources
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

                for (int i = 0; i < c.getResources().size(); i++) {
                    final ResourceDto             r  = c.getResources().get(i);
                    final BundleAssetResourceDto  br = (BundleAssetResourceDto) r;
                    final CatalogueItemDetailsDto ca = assets.stream().filter(a -> a.getId().equals(br.getId())).findFirst().orElse(null);
                    if (ca == null) {
                        e.rejectValue(String.format("resources[%d].id", i), EnumValidatorError.ReferenceNotFound.name());
                    } else if (!ca.getPublisherId().equals(c.getPublisherKey())) {
                        e.rejectValue(String.format("resources[%d].id", i), EnumValidatorError.NotAuthorized.name());
                    } else if(!BUNDLE_ALLOWED_ASSET_TYPES.contains(ca.getType())) {
                        e.rejectValue(String.format("resources[%d].id", i), EnumValidatorError.OptionNotSupported.name());
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
            } else if (!format.getExtensions().contains(extension)) {
                if (format.isBundleSupported() && extension.equals("zip")) {
                    continue;
                }
                e.rejectValue(String.format("resources[%d].fileName", i), EnumValidatorError.FileExtensionNotSupported.name());
            }
        }

        // Resource file names must be unique
        final var fileNames = c.getResources().stream()
            .map(r -> {
                if (r instanceof final FileResourceDto f) {
                    return f.getFileName();
                }
                if (r instanceof final ExternalUrlResourceDto u) {
                    return u.getFileName();
                }
                return null;
            })
            .filter(f -> f != null)
            .collect(Collectors.groupingBy(f -> f, Collectors.counting()));

        fileNames.entrySet().stream().filter(p -> p.getValue() > 1).forEach(p -> {
            e.reject(EnumValidatorError.FileNameNotUnique.name(), String.format("Resource filename %s is not unique", p.getKey()));
        });
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
        if (dynamicPricingModels && !models.isEmpty()) {
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
        final EnumDeliveryMethod       method         = Optional.ofNullable(c.getDeliveryMethod()).orElse(EnumDeliveryMethod.NONE);
        final DeliveryMethodOptions    options        = c.getDeliveryMethodOptions();
        final List<EnumDeliveryMethod> allowedMethods = c.getType().getAllowedDeliveryMethods();

        if (method == EnumDeliveryMethod.NONE && mode == EnumValidationMode.SUBMIT) {
            e.rejectValue("deliveryMethod", EnumValidatorError.NotNull.name());
        }

        switch (method) {
            case PHYSICAL_PROVIDER :
                if (options == null) {
                    e.rejectValue("deliveryMethodOptions", EnumValidatorError.NotNull.name());
                } else {
                    if (StringUtils.isEmpty(options.getMediaType())) {
                        e.rejectValue("deliveryMethodOptions.mediaType", EnumValidatorError.NotEmpty.name());
                    }
                    if (options.getNumberOfItems() == null) {
                        e.rejectValue("deliveryMethodOptions.numberOfItems", EnumValidatorError.NotNull.name());
                    } else if (options.getNumberOfItems() < 1) {
                        e.rejectValue("deliveryMethodOptions.numberOfItems", EnumValidatorError.Min.name());
                    }
                }
                break;

            case DIGITAL_PLATFORM :
            case DIGITAL_PROVIDER :
                if (options != null) {
                    if (!StringUtils.isEmpty(options.getMediaType())) {
                        e.rejectValue("deliveryMethodOptions.mediaType", EnumValidatorError.OperationNotSupported.name());
                    }
                    if (options.getNumberOfItems() != null) {
                        e.rejectValue("deliveryMethodOptions.numberOfItems", EnumValidatorError.OperationNotSupported.name());
                    }
                }
                break;

            case NONE :
                break;
        }

        if (method == EnumDeliveryMethod.NONE || allowedMethods.isEmpty()) {
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
