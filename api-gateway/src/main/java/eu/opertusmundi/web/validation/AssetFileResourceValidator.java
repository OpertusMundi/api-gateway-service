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
import eu.opertusmundi.common.model.asset.ResourceCommandDto;
import eu.opertusmundi.common.model.asset.UserFileResourceCommandDto;
import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
import eu.opertusmundi.common.repository.AssetFileTypeRepository;
import eu.opertusmundi.common.repository.DraftRepository;

@Component
public class AssetFileResourceValidator implements Validator {

    @Autowired
    private AssetFileTypeRepository assetFileTypeRepository;

    @Autowired
    private DraftRepository draftRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return FileResourceCommandDto.class.isAssignableFrom(clazz) ||
               UserFileResourceCommandDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object o, Errors e) {
        if (o instanceof FileResourceCommandDto) {
            final FileResourceCommandDto c = (FileResourceCommandDto) o;
            this.validate(c, e);
        }
        if (o instanceof UserFileResourceCommandDto) {
            final UserFileResourceCommandDto c = (UserFileResourceCommandDto) o;
            this.validate(c, e);
        }
    }

    private void validate(FileResourceCommandDto c, Errors e) {
        this.validateFormat(c, c.getFileName(), c.getFormat(), e);
    }

    private void validate(UserFileResourceCommandDto c, Errors e) {
        this.validateFormat(c, c.getFileName(), c.getFormat(), e);
    }

    private void validateFormat(ResourceCommandDto command, String fileName, String format, Errors e) {
        final ProviderAssetDraftEntity draft = this.draftRepository.findOneByPublisherAndKey(
            command.getPublisherKey(), command.getDraftKey()
        ).orElse(null);

        if (draft.getType() == EnumAssetType.BUNDLE) {
            e.reject(EnumValidatorError.OperationNotSupported.name());
            return;
        }

        final String              extension = FilenameUtils.getExtension(fileName);
        final AssetFileTypeEntity fileType  = this.assetFileTypeRepository.findOneByCategoryAndFormat(draft.getType(), format).orElse(null);

        if (fileType == null) {
            e.rejectValue("format", EnumValidatorError.OptionNotFound.name());
        } else if (!fileType.isEnabled()) {
            e.rejectValue("format", EnumValidatorError.OptionNotEnabled.name());
        } else if (StringUtils.isBlank(extension)) {
            e.rejectValue("fileName", EnumValidatorError.FileExtensionNotSupported.name());
        } else if (!fileType.getExtensions().contains(extension)) {
            if (fileType.isBundleSupported() && extension.equals("zip")) {
                return;
            }
            e.rejectValue("fileName", EnumValidatorError.FileExtensionNotSupported.name());
        }
    }

}
