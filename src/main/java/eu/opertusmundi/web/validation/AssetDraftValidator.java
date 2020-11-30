package eu.opertusmundi.web.validation;

import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import eu.opertusmundi.common.model.catalogue.client.CatalogueItemCommandDto;
import eu.opertusmundi.common.model.file.FilePathCommand;
import eu.opertusmundi.common.model.file.FileSystemException;
import eu.opertusmundi.common.service.FileManager;

@Component
public class AssetDraftValidator implements Validator {

    @Autowired
    private FileManager fileManager;

    @Override
    public boolean supports(Class<?> clazz) {
        return CatalogueItemCommandDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object o, Errors e) {
        final CatalogueItemCommandDto c = (CatalogueItemCommandDto) o;

        if (c.isIngested()) {
            if (StringUtils.isBlank(c.getSource())) {
                // Source is required
                e.rejectValue("source", "NotEmpty");
            } else {
                // Source must exist
                try {
                    final FilePathCommand fsCommand = FilePathCommand.builder()
                        .userId(c.getUserId())
                        .path(c.getSource())
                        .build();

                    final Path p = this.fileManager.resolveFilePath(fsCommand);
                    if (p == null || !p.toFile().exists()) {
                        e.rejectValue("source", "NotFound");
                    }
                } catch(final FileSystemException ex) {
                    e.rejectValue("source", "NotFound");
                }
            }

        }

    }

}
