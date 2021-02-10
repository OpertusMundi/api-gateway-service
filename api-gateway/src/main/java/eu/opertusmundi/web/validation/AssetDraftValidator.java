package eu.opertusmundi.web.validation;

import java.util.List;
import java.util.ListIterator;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.opertusmundi.common.domain.AssetFileTypeEntity;
import eu.opertusmundi.common.domain.AssetResourceEntity;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import eu.opertusmundi.common.model.profiler.EnumDataProfilerSourceType;
import eu.opertusmundi.common.repository.AssetFileTypeRepository;
import eu.opertusmundi.common.repository.AssetResourceRepository;

@Component
public class AssetDraftValidator implements Validator {

    @Autowired
    private AssetFileTypeRepository assetFileTypeRepository;
    
    @Autowired
    private AssetResourceRepository assetResourceRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return CatalogueItemCommandDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object o, Errors e) {
        final CatalogueItemCommandDto c = (CatalogueItemCommandDto) o;

        this.validateFormat(c, e);
        
        this.validateResources(c, e);
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
        } else if (c.isIngested() && format.getCategory() != EnumDataProfilerSourceType.VECTOR) {
            e.rejectValue("format", "IngestNotSupported");
        }
    }

    private void validateResources(CatalogueItemCommandDto c,  Errors e) {
        final List<AssetResourceEntity>         resources = this.assetResourceRepository.findAllResourcesByDraftKey(c.getAssetKey());
        final ListIterator<AssetResourceEntity> iter      = resources.listIterator();
        
        while(iter.hasNext()) {
            final int                 i         = iter.nextIndex();
            final AssetResourceEntity r         = iter.next();
            String                    extension = FilenameUtils.getExtension(r.getFileName());
            AssetFileTypeEntity       format    = this.assetFileTypeRepository.findOneByFormat(r.getFormat()).orElse(null);;
            
            if (format == null) {
                e.rejectValue(String.format("resources[%d].format", i), "NotFound");
            } else if (!format.isEnabled()) {
                e.rejectValue(String.format("resources[%d].format", i), "NotEnabled");
            } else if (c.isIngested() && format.getCategory() != EnumDataProfilerSourceType.VECTOR) {
                e.rejectValue(String.format("resources[%d].format", i), "IngestNotSupported");
            } else if (!format.getExtensions().contains(extension)) {
                if (format.isBundleSupported() && extension.equals("zip")) {
                    continue;
                }
                e.rejectValue(String.format("resources[%d].format", i), "NotSupportedExtension");
            }
        }
    }
    
}
