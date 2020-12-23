package eu.opertusmundi.web.validation;

import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.opertusmundi.common.domain.AssetFileTypeEntity;
import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import eu.opertusmundi.common.model.file.FileDto;
import eu.opertusmundi.common.model.file.FileSystemException;
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
        final CatalogueItemCommandDto c      = (CatalogueItemCommandDto) o;
        final List<FileDto>           files  = this.fileManager.getFiles(c.getAssetKey());
        AssetFileTypeEntity           format = null;

        // Validate format
        if (!StringUtils.isBlank(c.getFormat())) {
            format = this.assetFileTypeRepository.findOneByFormat(c.getFormat()).orElse(null);
            if (format == null) {
                e.rejectValue("format", "NotFound");
            } else if (!format.isEnabled()) {
                e.rejectValue("format", "NotEnabled");
            }
        }

        // Validate files
        if (format != null && format.isEnabled()) {
            for (final FileDto f : files) {
                final String ext = FilenameUtils.getExtension(f.getName());

                if (!format.getExtensions().contains(ext)) {
                    if (format.isBundleSupported() && ext.equals("zip")) {
                        continue;
                    }
                    e.reject("NotSupportedExtension", f.getPath());
                }
            }
        }

        // Validate parameters for ingest service
        if (c.isIngested()) {
            if (StringUtils.isBlank(c.getSource())) {
                // Source is required
                e.rejectValue("source", "NotEmpty");
            } else if (c.getAssetKey() != null) {
                // Source must exist. Validate only saved instances
                try {
                    final Path p = this.fileManager.resolveFilePath(c.getAssetKey(), c.getSource());
                    if (p == null || !p.toFile().exists()) {
                        e.rejectValue("source", "NotFound");
                    }
                } catch (final FileSystemException ex) {
                    e.rejectValue("source", "NotFound");
                }
            }
        }

    }

}
