package eu.opertusmundi.web.validation;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.opertusmundi.common.domain.AssetAdditionalResourceEntity;
import eu.opertusmundi.common.domain.AssetFileTypeEntity;
import eu.opertusmundi.common.domain.AssetResourceEntity;
import eu.opertusmundi.common.model.asset.AssetAdditionalResourceDto;
import eu.opertusmundi.common.model.asset.AssetFileAdditionalResourceDto;
import eu.opertusmundi.common.model.asset.EnumAssetAdditionalResource;
import eu.opertusmundi.common.model.asset.EnumAssetSourceType;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import eu.opertusmundi.common.model.pricing.QuotationException;
import eu.opertusmundi.common.repository.AssetAdditionalResourceRepository;
import eu.opertusmundi.common.repository.AssetFileTypeRepository;
import eu.opertusmundi.common.repository.AssetResourceRepository;

@Component
public class DraftValidator implements Validator {

    @Autowired
    private AssetFileTypeRepository assetFileTypeRepository;
    
    @Autowired
    private AssetResourceRepository assetResourceRepository;

    @Autowired
    private AssetAdditionalResourceRepository assetAdditionalResourceRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return CatalogueItemCommandDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object o, Errors e) {
        final CatalogueItemCommandDto c = (CatalogueItemCommandDto) o;

        this.validateFormat(c, e);
        this.validateResources(c, e);
        this.validateAdditionalResources(c, e);
        this.validatePricingModels(c, e);
    }

    private void validateFormat(CatalogueItemCommandDto c, Errors e) {
        if (StringUtils.isBlank(c.getFormat())) {
            return;
        }
        
        AssetFileTypeEntity format = this.assetFileTypeRepository.findOneByFormat(c.getFormat()).orElse(null);
        
        if (format == null) {
            e.rejectValue("format", "NotFound");
        } else if (!format.isEnabled()) {
            e.rejectValue("format", "NotEnabled");
        } else if (c.isIngested() && format.getCategory() != EnumAssetSourceType.VECTOR) {
            e.rejectValue("format", "IngestNotSupported");
        }
    }

    private void validateResources(CatalogueItemCommandDto c, Errors e) {
        final List<AssetResourceEntity> serverResources = this.assetResourceRepository.findAllResourcesByDraftKey(c.getAssetKey());
        final List<UUID>                serverKeys      = serverResources.stream().map(r -> r.getKey()).collect(Collectors.toList());
        final List<UUID>                requestKeys     = c.getResources().stream().map(r -> r.getId()).collect(Collectors.toList());

        // All request resource keys must exist at the server
        for (int i = 0; i < requestKeys.size(); i++) {
            if (!serverKeys.contains(requestKeys.get(i))) {
                e.rejectValue(String.format("resources[%d]", i), "NotFound");
            }
        }
        
        if(e.hasErrors()) {
            return;
        }
        
        // Check registered resources format
        for (int i = 0; i < requestKeys.size(); i++) {
            final UUID                key       = requestKeys.get(i);
            final AssetResourceEntity resource  = serverResources.stream().filter(r -> r.getKey().equals(key)).findFirst().orElse(null);
            final String              extension = FilenameUtils.getExtension(resource.getFileName());
            final AssetFileTypeEntity format    = this.assetFileTypeRepository.findOneByFormat(resource.getFormat()).orElse(null);
            
            if (format == null) {
                e.rejectValue(String.format("resources[%d].format", i), "NotFound");
            } else if (!format.isEnabled()) {
                e.rejectValue(String.format("resources[%d].format", i), "NotEnabled");
            } else if (c.isIngested() && format.getCategory() != EnumAssetSourceType.VECTOR) {
                e.rejectValue(String.format("resources[%d].format", i), "IngestNotSupported");
            } else if (!format.getExtensions().contains(extension)) {
                if (format.isBundleSupported() && extension.equals("zip")) {
                    continue;
                }
                e.rejectValue(String.format("resources[%d].format", i), "NotSupportedExtension");
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
                e.rejectValue(String.format("additionalResources[%d]", i), "NotFound");
            }
        }  
    }

    private void validatePricingModels(CatalogueItemCommandDto c, Errors e) {
        for (int i = 0; i < c.getPricingModels().size(); i++) {
            try {
                c.getPricingModels().get(i).validate();
            } catch (QuotationException ex) {
                e.rejectValue(String.format("pricingModels[%d]", i), ex.getMessage());
            }
        }
    }
    
}
