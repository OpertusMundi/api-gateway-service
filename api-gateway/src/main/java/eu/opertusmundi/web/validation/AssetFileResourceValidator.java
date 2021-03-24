package eu.opertusmundi.web.validation;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.opertusmundi.common.domain.AssetFileTypeEntity;
import eu.opertusmundi.common.domain.ProviderAssetDraftEntity;
import eu.opertusmundi.common.model.asset.EnumAssetSourceType;
import eu.opertusmundi.common.model.asset.FileResourceCommandDto;
import eu.opertusmundi.common.repository.AssetFileTypeRepository;
import eu.opertusmundi.common.repository.ProviderAssetDraftRepository;

@Component
public class AssetFileResourceValidator implements Validator {

    @Autowired
    private AssetFileTypeRepository assetFileTypeRepository;
    
    @Autowired
    private ProviderAssetDraftRepository providerAssetDraftRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return FileResourceCommandDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object o, Errors e) {
        final FileResourceCommandDto c = (FileResourceCommandDto) o;

        final ProviderAssetDraftEntity draft = this.providerAssetDraftRepository.findOneByPublisherAndKey(
            c.getPublisherKey(), c.getDraftKey()
        ).orElse(null);

        String              extension = FilenameUtils.getExtension(c.getFileName());
        AssetFileTypeEntity format    = this.assetFileTypeRepository.findOneByFormat(c.getFormat()).orElse(null);

        if (format == null) {
            e.rejectValue("format", "NotFound");
        } else if (!format.isEnabled()) {
            e.rejectValue("format", "NotEnabled");
        } else if (draft != null && draft.isIngested() && format.getCategory() != EnumAssetSourceType.VECTOR) {
            e.rejectValue("format", "IngestNotSupported");
        } else if (StringUtils.isBlank(extension)) {
            e.rejectValue("format", "NotSupportedExtension");
        } else if (!format.getExtensions().contains(extension)) {
            if (format.isBundleSupported() && extension.equals("zip")) {
                return;
            }
            e.rejectValue("format", "NotSupportedExtension");
        }
    }

}
