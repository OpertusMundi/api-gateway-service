package eu.opertusmundi.web.model.filemanager;

import java.nio.file.Path;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.opertusmundi.common.model.EnumOwningEntityType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "File upload options")
@Getter
@Setter
public class FileUploadCommand {

    @Schema(description = "File comments", required = false)
    private String comment;

    @Schema(description = "Optional file name", required = false)
    private String filename;

    @JsonIgnore
    private UUID owningEntityKey;

    @Schema(description = "True if an existing file should be overwritten", required = false, defaultValue = "false")
    private boolean overwrite;

    @JsonIgnore
    private EnumOwningEntityType owningEntityType;

    @JsonIgnore
    private UUID userKey;

    @JsonIgnore
    private Path path;

}
