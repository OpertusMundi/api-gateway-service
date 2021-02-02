package eu.opertusmundi.web.validation;

import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.opertusmundi.common.domain.AssetFileTypeEntity;
import eu.opertusmundi.common.model.asset.AssetResourceDto;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import eu.opertusmundi.common.model.profiler.EnumDataProfilerSourceType;
import eu.opertusmundi.common.repository.AssetFileTypeRepository;
import eu.opertusmundi.common.service.AssetFileManager;

@Component
public class AssetDraftValidator implements Validator {

    @Autowired
    private AssetFileManager fileManager;

    @Autowired
    private AssetFileTypeRepository assetFileTypeRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return CatalogueItemCommandDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object o, Errors e) {
        final CatalogueItemCommandDto c         = (CatalogueItemCommandDto) o;
        final List<AssetResourceDto>  resources = this.fileManager.getResources(c.getAssetKey());
        AssetFileTypeEntity           format    = null;

        // Validate format
        if (!StringUtils.isBlank(c.getFormat())) {
            format = this.assetFileTypeRepository.findOneByFormat(c.getFormat()).orElse(null);
            if (format == null) {
                e.rejectValue("format", "NotFound");
            } else if (!format.isEnabled()) {
                e.rejectValue("format", "NotEnabled");
            }
        }
        
        // Validate ingest options
        if (c.isIngested() && format.getCategory() != EnumDataProfilerSourceType.VECTOR) {
            e.rejectValue("ingested", "NotSupported");
        }

        // Validate files
        if (format != null && format.isEnabled()) {
            for (final AssetResourceDto r : resources) {
                final String ext = FilenameUtils.getExtension(r.getName());

                if (!format.getExtensions().contains(ext)) {
                    if (format.isBundleSupported() && ext.equals("zip")) {
                        continue;
                    }
                    e.reject("NotSupportedExtension", r.getName());
                }
            }
        }
    }

}
