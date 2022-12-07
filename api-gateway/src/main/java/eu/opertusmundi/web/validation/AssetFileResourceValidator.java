package eu.opertusmundi.web.validation;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.opertusmundi.common.domain.AssetFileTypeEntity;
import eu.opertusmundi.common.domain.ProviderAssetDraftEntity;
import eu.opertusmundi.common.model.BasicMessageCode;
import eu.opertusmundi.common.model.EnumValidatorError;
import eu.opertusmundi.common.model.ServiceException;
import eu.opertusmundi.common.model.asset.ExternalUrlFileResourceCommandDto;
import eu.opertusmundi.common.model.asset.FileResourceCommandDto;
import eu.opertusmundi.common.model.asset.ResourceCommandDto;
import eu.opertusmundi.common.model.asset.UserFileResourceCommandDto;
import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
import eu.opertusmundi.common.repository.AssetFileTypeRepository;
import eu.opertusmundi.common.repository.DraftRepository;

@Component
public class AssetFileResourceValidator implements Validator {

    private final Tika tika;

    @Value("${opertusmundi.asset.allow-not-secure-url:false}")
    private boolean allowNotSecureUrl;

    private final AssetFileTypeRepository assetFileTypeRepository;
    private final DraftRepository         draftRepository;
    private final MimeTypes               mimeTypes;

    @Autowired
    public AssetFileResourceValidator(
        AssetFileTypeRepository assetFileTypeRepository,
        DraftRepository         draftRepository
    ) {
        this.assetFileTypeRepository = assetFileTypeRepository;
        this.draftRepository         = draftRepository;

        this.tika      = new Tika();
        this.mimeTypes = MimeTypes.getDefaultMimeTypes();
    }

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
        if (o instanceof ExternalUrlFileResourceCommandDto) {
            final ExternalUrlFileResourceCommandDto c = (ExternalUrlFileResourceCommandDto) o;
            this.validate(c, e);
        }
    }

    private void validate(FileResourceCommandDto c, Errors e) {
        this.validateFormat(c, c.getFileName(), c.getFormat(), e);
    }

    private void validate(UserFileResourceCommandDto c, Errors e) {
        this.validateFormat(c, c.getFileName(), c.getFormat(), e);
    }

    private void validate(ExternalUrlFileResourceCommandDto c, Errors e) {
        this.validateFormat(c, c.getFileName(), c.getFormat(), e);
        this.validateUrl(c, e);
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

    private void validateUrl(ExternalUrlFileResourceCommandDto command, Errors e) {
        final var validator = new UrlValidator();
        final var url       = command.getUrl();
        final var fileName  = command.getFileName();

        if (!validator.isValid(url)) {
            e.rejectValue("url", EnumValidatorError.NotValid.name());
        } else if (!url.startsWith("https") && !allowNotSecureUrl) {
            e.rejectValue("url", EnumValidatorError.UrlNotSecure.name());
        } else {
            try {
                final var extension = FilenameUtils.getExtension(StringUtils.isBlank(fileName) ? url : fileName);
                final var mimeType  = tika.detect(new URL(url));
                if (mimeType != null) {
                    final var extensions = mimeTypes.forName(mimeType).getExtensions();
                    if (!extensions.contains("." + extension)) {
                        e.rejectValue("url", EnumValidatorError.FileExtensionNotSupported.name());
                    }
                }
            } catch (final IOException | MimeTypeException ex) {
                throw new ServiceException(BasicMessageCode.IOError, "Failed to detect mime type from URL", ex);
            }
        }
    }

}
