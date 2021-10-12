package eu.opertusmundi.web.validation;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.opertusmundi.common.domain.AssetFileTypeEntity;
import eu.opertusmundi.common.domain.ProviderAssetDraftEntity;
import eu.opertusmundi.common.model.EnumValidatorError;
import eu.opertusmundi.common.model.asset.FileResourceCommandDto;
import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
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

        if (draft.getType() == EnumAssetType.BUNDLE) {
            e.reject(EnumValidatorError.OperationNotSupported.name());
            return;
        }

        final String              extension = FilenameUtils.getExtension(c.getFileName());
        final AssetFileTypeEntity format    = this.assetFileTypeRepository.findOneByCategoryAndFormat(draft.getType(), c.getFormat()).orElse(null);

        if (format == null) {
            e.rejectValue("format", EnumValidatorError.OptionNotFound.name());
        } else if (!format.isEnabled()) {
            e.rejectValue("format", EnumValidatorError.OptionNotEnabled.name());
        } else if (draft != null && draft.isIngested() && format.getCategory() != EnumAssetType.VECTOR) {
            e.rejectValue("ingested", EnumValidatorError.OperationNotSupported.name());
        } else if (StringUtils.isBlank(extension)) {
            e.rejectValue("fileName", EnumValidatorError.FileExtensionNotSupported.name());
        } else if (!format.getExtensions().contains(extension)) {
            if (format.isBundleSupported() && extension.equals("zip")) {
                return;
            }
            e.rejectValue("fileName", EnumValidatorError.FileExtensionNotSupported.name());
        }
    }

}
