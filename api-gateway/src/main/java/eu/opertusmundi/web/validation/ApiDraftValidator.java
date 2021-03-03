package eu.opertusmundi.web.validation;

import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.opertusmundi.common.domain.AssetFileTypeEntity;
import eu.opertusmundi.common.model.asset.EnumAssetSourceType;
import eu.opertusmundi.common.model.catalogue.client.DraftApiCommandDto;
import eu.opertusmundi.common.model.catalogue.client.DraftApiFromFileCommandDto;
import eu.opertusmundi.common.model.catalogue.client.EnumSpatialDataServiceType;
import eu.opertusmundi.common.model.file.FilePathCommand;
import eu.opertusmundi.common.repository.AssetFileTypeRepository;
import eu.opertusmundi.common.service.UserFileManager;

@Component
public class ApiDraftValidator implements Validator {

    @Autowired
    private AssetFileTypeRepository assetFileTypeRepository;

    @Autowired
    private UserFileManager userFileManager;
    
    @Override
    public boolean supports(Class<?> clazz) {
        return DraftApiCommandDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object o, Errors e) {
        final DraftApiCommandDto c = (DraftApiCommandDto) o;

        if (c.getServiceType() != null) {
            final EnumSpatialDataServiceType serviceType = EnumSpatialDataServiceType.fromString(c.getServiceType());

            if (serviceType == null) {
                e.rejectValue("serviceType", "NotSupported");
            } else {
                switch (serviceType) {
                    case WMS :
                    case WFS :
                    case DATA_API :
                        // No action
                        break;
                    default :
                        e.rejectValue("serviceType", "NotSupported");
                        break;
                }
            }
        }

        switch (c.getType()) {
            case ASSET :
                validateAssetCommand(c, e);
                break;
            case FILE :
                validateFileCommand(c, e);
                break;
        }
    }

    private void validateAssetCommand(DraftApiCommandDto c, Errors e) {
        // final DraftApiFromAssetCommandDto command = (DraftApiFromAssetCommandDto) c;

    }

    private void validateFileCommand(DraftApiCommandDto c, Errors e) {
        final DraftApiFromFileCommandDto command = (DraftApiFromFileCommandDto) c;

        // Resolve resource file
        final FilePathCommand fileCommand = FilePathCommand.builder()
            .path(command.getPath())
            .userId(command.getUserId())
            .build();

        final Path resourcePath = this.userFileManager.resolveFilePath(fileCommand);
        if (!resourcePath.toFile().exists()) {
            e.rejectValue("path", "NotFound");
        }
        
        // Validate format
        if (StringUtils.isBlank(command.getFormat())) {
            return;
        }

        AssetFileTypeEntity format = this.assetFileTypeRepository
            .findOneByFormat(command.getFormat())
            .orElse(null);
        
        if (format == null) {
            e.rejectValue("format", "NotFound");
        } else if (!format.isEnabled()) {
            e.rejectValue("format", "NotEnabled");
        } else if (format.getCategory() != EnumAssetSourceType.VECTOR) {
            e.rejectValue("format", "IngestNotSupported");
        }
    }

}