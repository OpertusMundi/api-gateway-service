package eu.opertusmundi.web.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.opertusmundi.common.domain.AssetFileTypeEntity;
import eu.opertusmundi.common.model.EnumValidatorError;
import eu.opertusmundi.common.model.asset.service.UserServiceCommandDto;
import eu.opertusmundi.common.model.catalogue.client.EnumAssetType;
import eu.opertusmundi.common.model.file.FilePathCommand;
import eu.opertusmundi.common.model.file.FileSystemException;
import eu.opertusmundi.common.repository.AssetFileTypeRepository;
import eu.opertusmundi.common.service.UserFileManager;

@Component
public class UserServiceCommandValidator implements Validator {

    private final AssetFileTypeRepository assetFileTypeRepository;

    private final UserFileManager userFileManager;

    @Autowired
    public UserServiceCommandValidator(AssetFileTypeRepository assetFileTypeRepository, UserFileManager userFileManager) {
        this.assetFileTypeRepository = assetFileTypeRepository;
        this.userFileManager         = userFileManager;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return UserServiceCommandDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object o, Errors e) {
        if (e.hasErrors()) {
            return;
        }

        final UserServiceCommandDto command = (UserServiceCommandDto) o;

        // Resolve resource file
        final FilePathCommand fileCommand = FilePathCommand.builder()
            .path(command.getPath())
            .userName(command.getUserName())
            .build();

        try {
            this.userFileManager.resolveFilePath(fileCommand);
        } catch (final FileSystemException ex) {
            e.rejectValue("path", EnumValidatorError.FileNotFound.name());
        }

        // Validate format
        if (StringUtils.isBlank(command.getFormat())) {
            return;
        }

        final AssetFileTypeEntity format = this.assetFileTypeRepository
            .findOneByCategoryAndFormat(EnumAssetType.VECTOR, command.getFormat())
            .orElse(null);

        if (format == null) {
            e.rejectValue("format", EnumValidatorError.OptionNotFound.name());
        } else if (!format.isEnabled()) {
            e.rejectValue("format", EnumValidatorError.OptionNotEnabled.name());
        } else if (format.getCategory() != EnumAssetType.VECTOR) {
            e.rejectValue("format", EnumValidatorError.OptionNotSupported.name());
        }
    }

}
